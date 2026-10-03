package com.zurrtum.create.foundation.storage;

import org.slf4j.Logger;

/**
 * Backport shim for {@code net.minecraft.util.ErrorReporter}, which gained a
 * {@code Logging} AutoCloseable subtype and a richer Context/Error shape after 1.21.1.
 * This mimics only the surface Create Fly actually uses: a simple string-message sink.
 */
public interface ErrorReporter {
    interface Context {
        String getName();
    }

    ErrorReporter makeChild(Context context);

    void report(String message);

    class Logging implements ErrorReporter, AutoCloseable {
        private final String path;
        private final Logger logger;

        public Logging(Logger logger) {
            this((Context) null, logger);
        }

        public Logging(Context context, Logger logger) {
            this.path = context != null ? context.getName() : "";
            this.logger = logger;
        }

        @Override
        public ErrorReporter makeChild(Context context) {
            return new Logging(context, logger);
        }

        @Override
        public void report(String message) {
            logger.warn("Data error: {}{}", path.isEmpty() ? "" : path + ": ", message);
        }

        @Override
        public void close() {
        }
    }
}
