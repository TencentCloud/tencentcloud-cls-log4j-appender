package com.tencentcloud.cls;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * @author farmerx
 */
public class LoghubAppenderTest {
    private static final Logger LOGGER = LogManager.getLogger(LoghubAppenderTest.class);
    @Test
    public void testLogCommonMessage() {
        LOGGER.warn("This is a test common message logged by log4j.");
    }

    @Test
    public void testLogThrowable() {
        LOGGER.error("This is a test error message logged by log4j.",
                new UnsupportedOperationException("Log4j UnsupportedOperationException"));
    }

    @Test
    public void testLogLevelInfo() {
        LOGGER.info("This is a test error message logged by log4j, level is info, should not be logged.");
    }
}
