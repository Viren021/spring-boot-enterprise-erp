package com.example.erp_service_inventory.controller;

import com.example.erp_service_inventory.dto.InventoryRequests.*;
import com.example.erp_service_inventory.service.InventoryService;
import com.example.erp_service_inventory.service.InventoryReconciliationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/inventory")
@CrossOrigin(origins = "http://localhost:4200")
public class InventoryController {
    private final InventoryService service;
    private final InventoryReconciliationService reconciliation;
    public InventoryController(InventoryService service, InventoryReconciliationService reconciliation) {
        this.service = service; this.reconciliation = reconciliation;
    }
    @GetMapping("/warehouses") public Object warehouses() { return service.warehouses(); }
    @PostMapping("/warehouses") public Object warehouse(@RequestBody WarehouseRequest r) { return service.createWarehouse(r); }
    @GetMapping("/warehouses/{id}/locations") public Object locations(@PathVariable Long id) { return service.locations(id); }
    @PostMapping("/locations") public Object location(@RequestBody LocationRequest r) { return service.createLocation(r); }
    @GetMapping("/stock/{productId}") public Object stock(@PathVariable Long productId) { return service.stock(productId); }
    @PostMapping("/stock/movements") public Object movement(@RequestBody MovementRequest r, Principal p) { return service.move(r, p == null ? "system" : p.getName()); }
    @PostMapping("/stock/adjustments") public Object adjustment(@RequestBody MovementRequest r, Principal p) { return service.move(new MovementRequest(r.productId(), r.warehouseId(), r.locationId(), "ADJUSTMENT", r.quantity(), r.reference(), r.idempotencyKey()), p == null ? "system" : p.getName()); }
    @PostMapping("/stock/transfers") public ResponseEntity<Void> transfer(@RequestBody TransferRequest r) { service.transfer(r); return ResponseEntity.ok().build(); }
    @GetMapping("/stock/{productId}/movements") public Object movements(@PathVariable Long productId) { return service.movements(productId); }
    @PostMapping("/stock/reservations") public Object reserve(@RequestBody ReservationRequest r) { return service.reserve(r); }
    @GetMapping("/stock/reservations") public Object reservations() { return service.reservations(); }
    @PostMapping("/stock/reservations/{id}/release") public Object release(@PathVariable Long id) { return service.release(id); }
    @PostMapping("/stock/movements/{id}/approve") public Object approveMovement(@PathVariable Long id, Principal p) {
        return service.approveMovement(id, p == null ? "system" : p.getName());
    }
    @PatchMapping("/products/{id}/reorder-policy") public ResponseEntity<?> policy(@PathVariable Long id, @RequestBody ProductPolicyRequest r) { return ResponseEntity.ok(service.updatePolicy(id, r)); }
    @PostMapping("/stock/reconciliations") public Object reconcile(@RequestBody ReconciliationRequest r) { return reconciliation.reconcile(r); }
    @GetMapping("/stock/reconciliations") public Object reconciliationHistory(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long locationId) {
        return productId == null || warehouseId == null
                ? reconciliation.history() : reconciliation.history(productId, warehouseId, locationId);
    }
}
