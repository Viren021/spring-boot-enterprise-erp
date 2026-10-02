package com.example.erp_service_inventory.dto;

import java.math.BigDecimal;

public final class InventoryRequests {
    private InventoryRequests() {}
    public record WarehouseRequest(String code, String name, String address) {}
    public record LocationRequest(Long warehouseId, String code, String name) {}
    public record MovementRequest(Long productId, Long warehouseId, Long locationId,
                                  String movementType, BigDecimal quantity, String reference) {}
    public record TransferRequest(Long productId, Long fromWarehouseId, Long fromLocationId,
                                  Long toWarehouseId, Long toLocationId, BigDecimal quantity, String reference) {}
    public record ReservationRequest(Long productId, Long warehouseId, Long locationId,
                                     BigDecimal quantity, String reference) {}
    public record ProductPolicyRequest(Integer reorderLevel, Integer reorderQuantity) {}
}
