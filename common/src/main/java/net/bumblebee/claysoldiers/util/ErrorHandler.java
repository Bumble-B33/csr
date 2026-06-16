package net.bumblebee.claysoldiers.util;

import org.slf4j.Logger;

public record ErrorHandler(Logger logger, boolean devEnv) {
    public void error(String message) {
        logger.error(message);
        if (devEnv) {
            throw new IllegalStateException(message);
        }
    }

    public void error(String message, RuntimeException e) {
        logger.error(message, e);
        if (devEnv) {
            throw e;
        }
    }

    public void warn(String message) {
        warn(message, new IllegalStateException(message));
    }

    public void warn(String message, RuntimeException e) {
        if (devEnv) {
            logger.error(message);
            throw e;
        }
    }

    public void debug(String message) {
        if (devEnv) {
            logger.warn(message);
        }
    }
}
