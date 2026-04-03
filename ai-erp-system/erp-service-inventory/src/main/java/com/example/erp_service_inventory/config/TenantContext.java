package com.example.erp_service_inventory.config;


public class TenantContext {

    // This "ThreadLocal" ensures that if 100 users hit the API at the same time,
    // User A's tenant ID doesn't get mixed up with User B's.
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
