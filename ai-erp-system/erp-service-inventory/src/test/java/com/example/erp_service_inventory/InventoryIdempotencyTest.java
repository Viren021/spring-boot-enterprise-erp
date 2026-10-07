package com.example.erp_service_inventory;

import com.example.erp_service_inventory.config.TenantContext;
import com.example.erp_service_inventory.dto.InventoryRequests.MovementRequest;
import com.example.erp_service_inventory.entity.StockMovement;
import com.example.erp_service_inventory.kafka.FinanceIntegrationProducer;
import com.example.erp_service_inventory.repository.*;
import com.example.erp_service_inventory.service.InventoryApprovalPolicyService;
import com.example.erp_service_inventory.service.InventoryCostingService;
import com.example.erp_service_inventory.service.InventoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class InventoryIdempotencyTest {
    @Mock WarehouseRepository warehouses;
    @Mock LocationRepository locations;
    @Mock StockBalanceRepository balances;
    @Mock StockMovementRepository movements;
    @Mock StockReservationRepository reservations;
    @Mock ProductRepository products;
    @Mock FinanceIntegrationProducer finance;
    @Mock InventoryCostingService costing;
    @Mock InventoryApprovalPolicyService approvalPolicies;
    @InjectMocks InventoryService service;

    @AfterEach void clearTenant() { TenantContext.clear(); }

    @Test
    void retriesWithTheSameKeyReturnTheOriginalMovementWithoutReapplyingStock() {
        TenantContext.setTenantId("tenant_a");
        StockMovement original = new StockMovement();
        original.setId(42L);
        original.setTenantId("tenant_a");
        original.setIdempotencyKey("shipment-42");
        when(movements.findByTenantIdAndIdempotencyKey("tenant_a", "shipment-42"))
                .thenReturn(Optional.of(original));

        StockMovement result = service.move(new MovementRequest(1L, 2L, null, "IN", BigDecimal.ONE,
                "shipment", "shipment-42"), "warehouse-user");

        assertThat(result).isSameAs(original);
        verify(balances, never()).save(any());
        verify(movements, never()).save(any());
        verifyNoInteractions(finance, costing);
    }
}
