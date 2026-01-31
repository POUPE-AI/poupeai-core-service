package io.github.poupeai.core.audit;

import java.util.UUID;

public final class AuditContext {

    private AuditContext() {
    }

    private static final ThreadLocal<String> SOURCE_IP = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CORRELATION_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_AGENT = new ThreadLocal<>();

    public static void setSourceIp(String sourceIp) {
        SOURCE_IP.set(sourceIp);
    }

    public static String getSourceIp() {
        return SOURCE_IP.get();
    }

    public static void setCorrelationId(UUID correlationId) {
        CORRELATION_ID.set(correlationId);
    }

    public static UUID getCorrelationId() {
        return CORRELATION_ID.get();
    }

    public static void setUserAgent(String userAgent) {
        USER_AGENT.set(userAgent);
    }

    public static String getUserAgent() {
        return USER_AGENT.get();
    }

    public static void clear() {
        SOURCE_IP.remove();
        CORRELATION_ID.remove();
        USER_AGENT.remove();
    }
}
