package io.prada.listener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.prada.listener.testUtils.TestFileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SkipEventServiceTest {
    final SkipEventService unit = new SkipEventService(new ObjectMapper());

    @Test
    void isImportantEvent() {
        String fundingFeeEvent = TestFileUtils.load("data/skip-event/fundingFeeEvent.json");
        Assertions.assertFalse(unit.isImportantEvent(fundingFeeEvent));
        String importantEvent = TestFileUtils.load("data/skip-event/importantEvent.json");
        Assertions.assertTrue(unit.isImportantEvent(importantEvent));
    }
}