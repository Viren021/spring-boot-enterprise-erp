package com.example.erp_service_inventory.service;

import com.example.erp_service_inventory.config.TenantContext;
import com.example.erp_service_inventory.dto.InventoryRequests.*;
import com.example.erp_service_inventory.entity.*;
import com.example.erp_service_inventory.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.erp_service_inventory.kafka.FinanceIntegrationProducer;
import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryService {
    private final WarehouseRepository warehouses;
    private final LocationRepository locations;
    private final StockBalanceRepository balances;
    private final StockMovementRepository movements;
    private final StockReservationRepository reservations;
    private final ProductRepository products;
    private final FinanceIntegrationProducer finance;
    private final InventoryCostingService costing;

    public InventoryService(WarehouseRepository warehouses, LocationRepository locations,
                            StockBalanceRepository balances, StockMovementRepository movements,
                            StockReservationRepository reservations, ProductRepository products, FinanceIntegrationProducer finance,
                            InventoryCostingService costing) {
        this.warehouses = warehouses; this.locations = locations; this.balances = balances;
        this.movements = movements; this.reservations = reservations;                 this.products = products; this.finance = finance; this.costing = costing;
    }

    public String tenant() {
        String tenant = TenantContext.getTenantId();
        if (tenant == null || tenant.isBlank()) throw new IllegalStateException("X-Tenant-ID is required");
        return tenant;
    }
    public List<Warehouse> warehouses() { return warehouses.findByTenantId(tenant()); }
    public Warehouse createWarehouse(WarehouseRequest r) {
        Warehouse w = new Warehouse(); w.setTenantId(tenant()); w.setCode(r.code()); w.setName(r.name()); w.setAddress(r.address());
        return warehouses.save(w);
    }
    public List<Location> locations(Long warehouseId) { return locations.findByTenantIdAndWarehouseId(tenant(), warehouseId); }
    public Location createLocation(LocationRequest r) {
        if (!warehouses.findById(r.warehouseId()).filter(w -> tenant().equals(w.getTenantId())).isPresent())
            throw new IllegalArgumentException("Warehouse not found");
        Location l = new Location(); l.setTenantId(tenant()); l.setWarehouseId(r.warehouseId()); l.setCode(r.code()); l.setName(r.name());
        return locations.save(l);
    }
    public List<StockBalance> stock(Long productId) { return balances.findByTenantIdAndProductId(tenant(), productId); }

    @Transactional
    public StockMovement move(MovementRequest r) {
        if (r.quantity() == null || r.quantity().signum() <= 0) throw new IllegalArgumentException("Quantity must be positive");
        String t = tenant();
        StockBalance b = balances.findByTenantIdAndProductIdAndWarehouseIdAndLocationId(t, r.productId(), r.warehouseId(), r.locationId())
                .orElseGet(() -> { StockBalance x = new StockBalance(); x.setTenantId(t); x.setProductId(r.productId()); x.setWarehouseId(r.warehouseId()); x.setLocationId(r.locationId()); return x; });
        BigDecimal delta = r.movementType().equalsIgnoreCase("ADJUSTMENT")
                ? r.quantity() : (r.movementType().equalsIgnoreCase("OUT") || r.movementType().toUpperCase().endsWith("_OUT")
                || r.movementType().equalsIgnoreCase("ISSUE") ? r.quantity().negate() : r.quantity());
        if (b.getQuantity().add(delta).compareTo(BigDecimal.ZERO) < 0) throw new IllegalStateException("Insufficient stock");
        b.setQuantity(b.getQuantity().add(delta)); balances.save(b);
        StockMovement m = new StockMovement(); m.setTenantId(t); m.setProductId(r.productId()); m.setWarehouseId(r.warehouseId());
        m.setLocationId(r.locationId()); m.setMovementType(r.movementType()); m.setQuantity(r.quantity()); m.setReference(r.reference());
        StockMovement saved = movements.save(m);
        Product product = products.findById(r.productId()).filter(p -> t.equals(p.getTenantId()))
                .orElseThrow(() -> new IllegalStateException("Product not found; inventory value is unavailable"));
        finance.publish("INVENTORY_VALUATION", String.valueOf(saved.getId()), r.quantity(), product,
                delta.signum() < 0 ? "DECREASE" : "INCREASE");
        costing.apply(b, saved, product.getPrice() == null ? BigDecimal.ZERO : BigDecimal.valueOf(product.getPrice()), delta);
        balances.save(b);
        return saved;
    }
    @Transactional
    public void transfer(TransferRequest r) {
        move(new MovementRequest(r.productId(), r.fromWarehouseId(), r.fromLocationId(), "TRANSFER_OUT", r.quantity(), r.reference()));
        move(new MovementRequest(r.productId(), r.toWarehouseId(), r.toLocationId(), "TRANSFER_IN", r.quantity(), r.reference()));
    }
    public List<StockMovement> movements(Long productId) { return movements.findByTenantIdAndProductIdOrderByOccurredAtDesc(tenant(), productId); }

    @Transactional
    public StockReservation reserve(ReservationRequest r) {
        if (r.quantity() == null || r.quantity().signum() <= 0) throw new IllegalArgumentException("Quantity must be positive");
        String t = tenant();
        StockBalance b = balances.findByTenantIdAndProductIdAndWarehouseIdAndLocationId(t, r.productId(), r.warehouseId(), r.locationId())
                .orElseThrow(() -> new IllegalStateException("No stock balance"));
        if (b.getQuantity().subtract(b.getReservedQuantity()).compareTo(r.quantity()) < 0) throw new IllegalStateException("Insufficient available stock");
        b.setReservedQuantity(b.getReservedQuantity().add(r.quantity())); balances.save(b);
        StockReservation x = new StockReservation(); x.setTenantId(t); x.setProductId(r.productId()); x.setWarehouseId(r.warehouseId()); x.setLocationId(r.locationId()); x.setQuantity(r.quantity()); x.setReference(r.reference());
        StockReservation saved = reservations.save(x);
        Product product = products.findById(r.productId()).filter(p -> t.equals(p.getTenantId()))
                .orElseThrow(() -> new IllegalStateException("Product not found; reservation value is unavailable"));
        finance.publish("INVENTORY_RESERVATION", String.valueOf(saved.getId()), r.quantity(), product, "DECREASE");
        return saved;
    }
    @Transactional
    public StockReservation release(Long id) {
        String t = tenant(); StockReservation x = reservations.findById(id).filter(r -> t.equals(r.getTenantId()) && "ACTIVE".equals(r.getStatus()))
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        balances.findByTenantIdAndProductIdAndWarehouseIdAndLocationId(t, x.getProductId(), x.getWarehouseId(), x.getLocationId()).ifPresent(b -> { b.setReservedQuantity(b.getReservedQuantity().subtract(x.getQuantity()).max(BigDecimal.ZERO)); balances.save(b); });
        x.setStatus("RELEASED"); return reservations.save(x);
    }
    public List<StockReservation> reservations() { return reservations.findByTenantIdAndStatus(tenant(), "ACTIVE"); }
    public Product updatePolicy(Long id, ProductPolicyRequest r) {
        Product p = products.findById(id).filter(x -> tenant().equals(x.getTenantId())).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        p.setReorderLevel(r.reorderLevel()); p.setReorderQuantity(r.reorderQuantity()); return products.save(p);
    }
}
