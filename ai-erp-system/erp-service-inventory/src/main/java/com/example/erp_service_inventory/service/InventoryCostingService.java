package com.example.erp_service_inventory.service;

import com.example.erp_service_inventory.entity.*;
import com.example.erp_service_inventory.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class InventoryCostingService {
    private final StockBalanceRepository balances;
    private final InventoryCostLayerRepository layers;

    public InventoryCostingService(StockBalanceRepository balances, InventoryCostLayerRepository layers) {
        this.balances = balances; this.layers = layers;
    }

    @Transactional
    public void apply(StockBalance balance, StockMovement movement, BigDecimal unitCost, BigDecimal delta) {
        BigDecimal oldValue = balance.getInventoryValue() == null ? BigDecimal.ZERO : balance.getInventoryValue();
        BigDecimal cost = unitCost == null ? BigDecimal.ZERO : unitCost.max(BigDecimal.ZERO);
        BigDecimal newValue = oldValue.add(delta.signum() >= 0 ? delta.multiply(cost) :
                delta.abs().multiply(balance.getAverageUnitCost() == null ? BigDecimal.ZERO : balance.getAverageUnitCost()).negate());
        BigDecimal quantity = balance.getQuantity();
        balance.setInventoryValue(newValue.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        balance.setAverageUnitCost(quantity.signum() > 0
                ? balance.getInventoryValue().divide(quantity, 6, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        if (delta.signum() > 0) {
            InventoryCostLayer layer = new InventoryCostLayer();
            layer.setTenantId(balance.getTenantId()); layer.setProductId(balance.getProductId());
            layer.setWarehouseId(balance.getWarehouseId()); layer.setLocationId(balance.getLocationId());
            layer.setSourceMovementId(movement.getId()); layer.setQuantityReceived(delta);
            layer.setQuantityRemaining(delta); layer.setUnitCost(cost); layers.save(layer);
        }
    }

    /** Rebuilds value from the persisted balance and cost layers without changing quantities. */
    @Scheduled(cron = "${inventory.costing.reconciliation-cron:0 0 * * * *}")
    @Transactional
    public void reconcile() {
        for (StockBalance balance : balances.findAll()) {
            BigDecimal quantity = balance.getQuantity() == null ? BigDecimal.ZERO : balance.getQuantity();
            BigDecimal value = balance.getInventoryValue() == null ? BigDecimal.ZERO : balance.getInventoryValue();
            balance.setAverageUnitCost(quantity.signum() > 0
                    ? value.divide(quantity, 6, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            balances.save(balance);
        }
    }
}
