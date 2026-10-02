package com.example.erp_service_master_data.config;

public final class TenantContext {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();
    private TenantContext() {}
    public static void set(String tenant) { CURRENT.set(tenant); }
    public static String require() {
        String tenant = CURRENT.get();
        if (tenant == null || tenant.isBlank()) throw new IllegalStateException("X-Tenant-ID is required");
        return tenant;
    }
    public static void clear() { CURRENT.remove(); }
}
