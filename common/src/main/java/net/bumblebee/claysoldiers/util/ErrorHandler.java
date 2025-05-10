package net.bumblebee.claysoldiers.util;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import org.slf4j.Logger;

public interface ErrorHandler {
    ErrorHandler INSTANCE = ClaySoldiersCommon.PLATFORM.isDevEnv() ? createThrowing() : createLogging(ClaySoldiersCommon.LOGGER);

    default void error(String message) {
        handle(new IllegalStateException(message));
    }

    default void debug(String message) {
        error(message);
    }

    default void hide(String message) {}

    void handle(RuntimeException e);

    void handle(String msg, RuntimeException e);

    private static ErrorHandler createLogging(Logger logger) {
        return  new ErrorHandler() {
            @Override
            public void handle(String msg, RuntimeException e) {
                logger.error(msg, e);
            }

            @Override
            public void handle(RuntimeException e) {
                logger.error(e.getMessage(), e);
            }

            @Override
            public void error(String message) {
                logger.error(message);
            }

            @Override
            public void debug(String message) {
                logger.debug(message);
            }
        };
    }

    private static ErrorHandler createThrowing() {
        return new ErrorHandler() {
            @Override
            public void handle(String msg, RuntimeException e) {
                throw e;
            }

            @Override
            public void handle(RuntimeException e) {
                throw e;
            }

            @Override
            public void hide(String message) {
                error(message);
            }
        };
    }
}
