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
    private final InventoryApprovalPolicyService approvalPolicies;

    public InventoryService(WarehouseRepository warehouses, LocationRepository locations,
                            StockBalanceRepository balances, StockMovementRepository movements,
                            StockReservationRepository reservations, ProductRepository products, FinanceIntegrationProducer finance,
                            InventoryCostingService costing, InventoryApprovalPolicyService approvalPolicies) {
        this.warehouses = warehouses; this.locations = locations; this.balances = balances;
        this.movements = movements; this.reservations = reservations; this.products = products;
        this.finance = finance; this.costing = costing; this.approvalPolicies = approvalPolicies;
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
        return move(r, "system");
    }

    @Transactional
    public StockMovement move(MovementRequest r, String actor) {
        if (r.quantity() == null || r.quantity().signum() <= 0) throw new IllegalArgumentException("Quantity must be positive");
        String t = tenant();
        if (r.idempotencyKey() != null && !r.idempotencyKey().isBlank()) {
            java.util.Optional<StockMovement> existing = movements.findByTenantIdAndIdempotencyKey(
                    t, r.idempotencyKey().trim());
            if (existing.isPresent()) return existing.get();
        }
        StockBalance b = balances.findByTenantIdAndProductIdAndWarehouseIdAndLocationId(t, r.productId(), r.warehouseId(), r.locationId())
                .orElseGet(() -> { StockBalance x = new StockBalance(); x.setTenantId(t); x.setProductId(r.productId()); x.setWarehouseId(r.warehouseId()); x.setLocationId(r.locationId()); return x; });
        BigDecimal delta = r.movementType().equalsIgnoreCase("ADJUSTMENT")
                ? r.quantity() : (r.movementType().equalsIgnoreCase("OUT") || r.movementType().toUpperCase().endsWith("_OUT")
                || r.movementType().equalsIgnoreCase("ISSUE") ? r.quantity().negate() : r.quantity());
        Product product = products.findById(r.productId()).filter(p -> t.equals(p.getTenantId()))
                .orElseThrow(() -> new IllegalStateException("Product not found; inventory value is unavailable"));
        StockMovement m = new StockMovement(); m.setTenantId(t); m.setProductId(r.productId()); m.setWarehouseId(r.warehouseId());
        m.setLocationId(r.locationId()); m.setMovementType(r.movementType()); m.setQuantity(r.quantity()); m.setReference(r.reference());
        m.setIdempotencyKey(r.idempotencyKey() == null || r.idempotencyKey().isBlank() ? null : r.idempotencyKey().trim());
        m.setCreatedBy(actor == null || actor.isBlank() ? "system" : actor);
        BigDecimal amount = r.quantity().multiply(product.getPrice() == null ? BigDecimal.ZERO : BigDecimal.valueOf(product.getPrice()));
        if ("ADJUSTMENT".equalsIgnoreCase(r.movementType())
                && approvalPolicies.requiresApproval("STOCK_ADJUSTMENT", amount, actor)) {
            m.setStatus("PENDING_APPROVAL");
            return movements.save(m);
        }
        if (b.getQuantity().add(delta).compareTo(BigDecimal.ZERO) < 0) throw new IllegalStateException("Insufficient stock");
        b.setQuantity(b.getQuantity().add(delta)); balances.save(b);
        StockMovement saved = movements.save(m);
        applyPostedMovement(b, saved, product, delta, t);
        return saved;
    }

    @Transactional
    public StockMovement approveMovement(Long id, String actor) {
        String t = tenant();
        StockMovement movement = movements.findByIdAndTenantId(id, t)
                .orElseThrow(() -> new IllegalArgumentException("Movement not found"));
        if (!"PENDING_APPROVAL".equals(movement.getStatus()))
            throw new IllegalStateException("Movement is not pending approval");
        if (actor == null || actor.isBlank() || actor.equalsIgnoreCase(movement.getCreatedBy()))
            throw new IllegalStateException("Maker-checker policy prevents self approval");
        StockBalance balance = balances.findByTenantIdAndProductIdAndWarehouseIdAndLocationId(
                t, movement.getProductId(), movement.getWarehouseId(), movement.getLocationId())
                .orElseGet(() -> { StockBalance x = new StockBalance(); x.setTenantId(t);
                    x.setProductId(movement.getProductId()); x.setWarehouseId(movement.getWarehouseId());
                    x.setLocationId(movement.getLocationId()); return x; });
        BigDecimal delta = signedDelta(movement.getMovementType(), movement.getQuantity());
        if (balance.getQuantity().add(delta).compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalStateException("Insufficient stock");
        balance.setQuantity(balance.getQuantity().add(delta));
        Product product = products.findById(movement.getProductId()).filter(p -> t.equals(p.getTenantId()))
                .orElseThrow(() -> new IllegalStateException("Product not found; inventory value is unavailable"));
        movement.setStatus("POSTED"); movement.setApprovedBy(actor);
        StockMovement saved = movements.save(movement);
        applyPostedMovement(balance, saved, product, delta, t);
        return saved;
    }

    private void applyPostedMovement(StockBalance balance, StockMovement movement, Product product,
                                     BigDecimal delta, String tenant) {
        finance.publish("INVENTORY_VALUATION", String.valueOf(movement.getId()), movement.getQuantity(), product,
                delta.signum() < 0 ? "DECREASE" : "INCREASE");
        costing.apply(balance, movement, product.getPrice() == null ? BigDecimal.ZERO : BigDecimal.valueOf(product.getPrice()), delta);
        balances.save(balance);
    }

    private BigDecimal signedDelta(String movementType, BigDecimal quantity) {
        String type = movementType == null ? "" : movementType.toUpperCase();
        return "ADJUSTMENT".equals(type) || (!type.equals("OUT") && !type.endsWith("_OUT") && !type.equals("ISSUE"))
                ? quantity : quantity.negate();
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
