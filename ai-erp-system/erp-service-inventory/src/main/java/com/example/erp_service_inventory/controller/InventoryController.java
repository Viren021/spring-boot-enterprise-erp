package com.example.erp_service_inventory.controller;

import com.example.erp_service_inventory.dto.InventoryRequests.*;
import com.example.erp_service_inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@CrossOrigin(origins = "http://localhost:4200")
public class InventoryController {
    private final InventoryService service;
    public InventoryController(InventoryService service) { this.service = service; }
    @GetMapping("/warehouses") public Object warehouses() { return service.warehouses(); }
    @PostMapping("/warehouses") public Object warehouse(@RequestBody WarehouseRequest r) { return service.createWarehouse(r); }
    @GetMapping("/warehouses/{id}/locations") public Object locations(@PathVariable Long id) { return service.locations(id); }
    @PostMapping("/locations") public Object location(@RequestBody LocationRequest r) { return service.createLocation(r); }
    @GetMapping("/stock/{productId}") public Object stock(@PathVariable Long productId) { return service.stock(productId); }
    @PostMapping("/stock/movements") public Object movement(@RequestBody MovementRequest r) { return service.move(r); }
    @PostMapping("/stock/adjustments") public Object adjustment(@RequestBody MovementRequest r) { return service.move(new MovementRequest(r.productId(), r.warehouseId(), r.locationId(), "ADJUSTMENT", r.quantity(), r.reference())); }
    @PostMapping("/stock/transfers") public ResponseEntity<Void> transfer(@RequestBody TransferRequest r) { service.transfer(r); return ResponseEntity.ok().build(); }
    @GetMapping("/stock/{productId}/movements") public Object movements(@PathVariable Long productId) { return service.movements(productId); }
    @PostMapping("/stock/reservations") public Object reserve(@RequestBody ReservationRequest r) { return service.reserve(r); }
    @GetMapping("/stock/reservations") public Object reservations() { return service.reservations(); }
    @PostMapping("/stock/reservations/{id}/release") public Object release(@PathVariable Long id) { return service.release(id); }
    @PatchMapping("/products/{id}/reorder-policy") public ResponseEntity<?> policy(@PathVariable Long id, @RequestBody ProductPolicyRequest r) { return ResponseEntity.ok(service.updatePolicy(id, r)); }
}
