package com.example.erp_service_inventory.service;

import com.example.erp_service_inventory.config.TenantContext;
import com.example.erp_service_inventory.dto.InventoryRequests.ReconciliationRequest;
import com.example.erp_service_inventory.entity.InventoryReconciliation;
import com.example.erp_service_inventory.entity.InventoryCostLayer;
import com.example.erp_service_inventory.entity.StockBalance;
import com.example.erp_service_inventory.entity.StockMovement;
import com.example.erp_service_inventory.repository.InventoryCostLayerRepository;
import com.example.erp_service_inventory.repository.InventoryReconciliationRepository;
import com.example.erp_service_inventory.repository.StockBalanceRepository;
import com.example.erp_service_inventory.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class InventoryReconciliationService {
    private static final BigDecimal QUANTITY_TOLERANCE = new BigDecimal("0.001");
    private static final BigDecimal VALUE_TOLERANCE = new BigDecimal("0.01");

    private final StockBalanceRepository balances;
    private final StockMovementRepository movements;
    private final InventoryCostLayerRepository layers;
    private final InventoryReconciliationRepository reconciliations;

    public InventoryReconciliationService(StockBalanceRepository balances,
                                          StockMovementRepository movements,
                                          InventoryCostLayerRepository layers,
                                          InventoryReconciliationRepository reconciliations) {
        this.balances = balances;
        this.movements = movements;
        this.layers = layers;
        this.reconciliations = reconciliations;
    }

    @Transactional
    public InventoryReconciliation reconcile(ReconciliationRequest request) {
        if (request == null || request.productId() == null || request.warehouseId() == null) {
            throw new IllegalArgumentException("Product and warehouse are required");
        }
        String tenant = TenantContext.getTenantId();
        if (tenant == null || tenant.isBlank()) throw new IllegalStateException("X-Tenant-ID is required");

        List<StockMovement> scope = movements.findByTenantIdAndProductIdAndWarehouseIdAndLocationId(
                tenant, request.productId(), request.warehouseId(), request.locationId());
        BigDecimal expectedQuantity = scope.stream()
                .map(this::signedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expectedValue = layers
                .findByTenantIdAndProductIdAndWarehouseIdAndLocationIdAndQuantityRemainingGreaterThanOrderByCreatedAt(
                        tenant, request.productId(), request.warehouseId(), request.locationId(), BigDecimal.ZERO)
                .stream()
                .map(layer -> layer.getQuantityRemaining().multiply(layer.getUnitCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        StockBalance balance = balances.findByTenantIdAndProductIdAndWarehouseIdAndLocationId(
                tenant, request.productId(), request.warehouseId(), request.locationId()).orElse(null);
        BigDecimal actualQuantity = balance == null || balance.getQuantity() == null
                ? BigDecimal.ZERO : balance.getQuantity();
        BigDecimal actualValue = balance == null || balance.getInventoryValue() == null
                ? BigDecimal.ZERO : balance.getInventoryValue();
        BigDecimal quantityDiscrepancy = actualQuantity.subtract(expectedQuantity);
        BigDecimal valueDiscrepancy = actualValue.subtract(expectedValue);

        InventoryReconciliation result = new InventoryReconciliation();
        result.setTenantId(tenant);
        result.setProductId(request.productId());
        result.setWarehouseId(request.warehouseId());
        result.setLocationId(request.locationId());
        result.setExpectedQuantity(expectedQuantity);
        result.setActualQuantity(actualQuantity);
        result.setQuantityDiscrepancy(quantityDiscrepancy);
        result.setExpectedValue(expectedValue);
        result.setActualValue(actualValue.setScale(2, RoundingMode.HALF_UP));
        result.setValueDiscrepancy(valueDiscrepancy.setScale(2, RoundingMode.HALF_UP));
        result.setStatus(quantityDiscrepancy.abs().compareTo(QUANTITY_TOLERANCE) <= 0
                && valueDiscrepancy.abs().compareTo(VALUE_TOLERANCE) <= 0 ? "MATCHED" : "DISCREPANCY");
        result.setNotes(request.notes());
        return reconciliations.save(result);
    }

    @Transactional(readOnly = true)
    public List<InventoryReconciliation> history() {
        return reconciliations.findByTenantIdOrderByCreatedAtDesc(requiredTenant());
    }

    @Transactional(readOnly = true)
    public List<InventoryReconciliation> history(Long productId, Long warehouseId, Long locationId) {
        return reconciliations.findByTenantIdAndProductIdAndWarehouseIdAndLocationIdOrderByCreatedAtDesc(
                requiredTenant(), productId, warehouseId, locationId);
    }

    private String requiredTenant() {
        String tenant = TenantContext.getTenantId();
        if (tenant == null || tenant.isBlank()) throw new IllegalStateException("X-Tenant-ID is required");
        return tenant;
    }

    private BigDecimal signedQuantity(StockMovement movement) {
        String type = movement.getMovementType() == null ? "" : movement.getMovementType().toUpperCase();
        BigDecimal quantity = movement.getQuantity() == null ? BigDecimal.ZERO : movement.getQuantity();
        return "ADJUSTMENT".equals(type) || (!type.equals("OUT") && !type.endsWith("_OUT") && !type.equals("ISSUE"))
                ? quantity : quantity.negate();
    }
}
