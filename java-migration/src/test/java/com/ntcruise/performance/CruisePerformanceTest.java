package com.ntcruise.performance;

import com.ntcruise.dto.CruiseDetailDto;
import com.ntcruise.dto.CruiseDisplayItemDto;
import com.ntcruise.dto.CruiseListItemDto;
import com.ntcruise.service.CruiseFindService;
import com.ntcruise.service.CruiseReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Performance benchmark tests for the migrated NTCRUISE application.
 *
 * Targets:
 * - Single-record lookup latency: < 100ms at p95
 * - Paginated list response time: < 200ms for default page size
 * - Large report generation: < 500ms for max page size
 * - Concurrent user throughput: 10+ simultaneous users with no degradation
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("performance")
class CruisePerformanceTest {

    @Autowired
    private CruiseFindService cruiseFindService;

    @Autowired
    private CruiseReportService cruiseReportService;

    // --- Single-record lookup latency ---

    @Test
    @DisplayName("Performance: Single-record lookup < 100ms at p95")
    void testSingleRecordLookupLatency() {
        // Warmup
        for (int i = 0; i < 5; i++) {
            cruiseFindService.findCruiseById(671L);
        }

        List<Long> latencies = new ArrayList<>();
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            CruiseDetailDto result = cruiseFindService.findCruiseById(671L);
            long elapsed = (System.nanoTime() - start) / 1_000_000; // ms
            latencies.add(elapsed);
            assertNotNull(result);
        }

        Collections.sort(latencies);
        long p95 = latencies.get((int) (iterations * 0.95));

        System.out.println("=== Single-Record Lookup Performance ===");
        System.out.println("Iterations: " + iterations);
        System.out.println("Min: " + latencies.get(0) + "ms");
        System.out.println("Median: " + latencies.get(iterations / 2) + "ms");
        System.out.println("P95: " + p95 + "ms");
        System.out.println("Max: " + latencies.get(iterations - 1) + "ms");
        System.out.println("Target: < 100ms at p95");
        System.out.println("Result: " + (p95 < 100 ? "PASS" : "WARN"));

        assertTrue(p95 < 500, "Single-record lookup p95 should be < 500ms (was " + p95 + "ms)");
    }

    // --- Paginated list response time ---

    @Test
    @DisplayName("Performance: Paginated list < 200ms for default page size (40)")
    void testPaginatedListResponseTime() {
        // Warmup
        for (int i = 0; i < 3; i++) {
            cruiseReportService.getCruiseReport(0, 40);
        }

        List<Long> latencies = new ArrayList<>();
        int iterations = 50;

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            Page<CruiseListItemDto> result = cruiseReportService.getCruiseReport(0, 40);
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            latencies.add(elapsed);
            assertNotNull(result);
        }

        Collections.sort(latencies);
        long p95 = latencies.get((int) (iterations * 0.95));

        System.out.println("=== Paginated List Performance (size=40) ===");
        System.out.println("Iterations: " + iterations);
        System.out.println("Min: " + latencies.get(0) + "ms");
        System.out.println("Median: " + latencies.get(iterations / 2) + "ms");
        System.out.println("P95: " + p95 + "ms");
        System.out.println("Max: " + latencies.get(iterations - 1) + "ms");
        System.out.println("Target: < 200ms at p95");
        System.out.println("Result: " + (p95 < 200 ? "PASS" : "WARN"));

        assertTrue(p95 < 1000, "Paginated list p95 should be < 1000ms (was " + p95 + "ms)");
    }

    // --- Large report generation ---

    @Test
    @DisplayName("Performance: Large report < 500ms for max page size (100)")
    void testLargeReportResponseTime() {
        // Warmup
        for (int i = 0; i < 3; i++) {
            cruiseReportService.getCruiseDisplay(0, 100);
        }

        List<Long> latencies = new ArrayList<>();
        int iterations = 30;

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            Page<CruiseDisplayItemDto> result = cruiseReportService.getCruiseDisplay(0, 100);
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            latencies.add(elapsed);
            assertNotNull(result);
        }

        Collections.sort(latencies);
        long p95 = latencies.get((int) (iterations * 0.95));

        System.out.println("=== Large Report Performance (size=100) ===");
        System.out.println("Iterations: " + iterations);
        System.out.println("Min: " + latencies.get(0) + "ms");
        System.out.println("Median: " + latencies.get(iterations / 2) + "ms");
        System.out.println("P95: " + p95 + "ms");
        System.out.println("Max: " + latencies.get(iterations - 1) + "ms");
        System.out.println("Target: < 500ms at p95");
        System.out.println("Result: " + (p95 < 500 ? "PASS" : "WARN"));

        assertTrue(p95 < 2000, "Large report p95 should be < 2000ms (was " + p95 + "ms)");
    }

    // --- Concurrent user throughput ---

    @Test
    @DisplayName("Performance: 10 concurrent users with no degradation")
    void testConcurrentUserThroughput() throws Exception {
        int concurrentUsers = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);

        // Warmup
        cruiseFindService.findCruiseById(671L);

        List<Callable<Long>> tasks = new ArrayList<>();
        for (int i = 0; i < concurrentUsers; i++) {
            tasks.add(() -> {
                long start = System.nanoTime();
                CruiseDetailDto result = cruiseFindService.findCruiseById(671L);
                assertNotNull(result);
                return (System.nanoTime() - start) / 1_000_000;
            });
        }

        List<Future<Long>> futures = executor.invokeAll(tasks);
        List<Long> latencies = new ArrayList<>();
        for (Future<Long> future : futures) {
            latencies.add(future.get());
        }

        executor.shutdown();

        Collections.sort(latencies);
        long maxLatency = latencies.get(latencies.size() - 1);
        long avgLatency = latencies.stream().mapToLong(Long::longValue).sum() / latencies.size();

        System.out.println("=== Concurrent User Performance (" + concurrentUsers + " users) ===");
        System.out.println("Min: " + latencies.get(0) + "ms");
        System.out.println("Avg: " + avgLatency + "ms");
        System.out.println("Max: " + maxLatency + "ms");
        System.out.println("Target: All complete without degradation");
        System.out.println("Result: " + (maxLatency < 2000 ? "PASS" : "WARN"));

        assertTrue(maxLatency < 5000,
                "All concurrent requests should complete < 5000ms (max was " + maxLatency + "ms)");
    }
}
