package io.github.poupeai.core.audit;

import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.Map;

public final class Log {

    private Log() { }

    public static void event(Logger logger, String eventType, String message, Object... args) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable("event.type", eventType)) {
            logger.info(message, args);
        }
    }

    public static void error(Logger logger, String eventType, String message, Throwable t) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable("event.type", eventType)) {
            logger.error(message, t);
        }
    }

    public static void run(Map<String, String> context, Runnable action) {
        if (context == null || context.isEmpty()) {
            action.run();
            return;
        }

        context.forEach(MDC::put);
        try {
            action.run();
        } finally {
            context.keySet().forEach(MDC::remove);
        }
    }
}