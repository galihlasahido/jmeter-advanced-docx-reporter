package com.jmeter.plugin.docx.util;

import org.apache.jmeter.samplers.SampleResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to calculate summary statistics from sample results.
 */
public class SummaryStatistics {

    private final List<SampleResult> samples;
    private long totalSamples;
    private long totalErrors;
    private long totalResponseTime;
    private long minResponseTime;
    private long maxResponseTime;
    private long totalBytes;
    private long startTime;
    private long endTime;

    public SummaryStatistics() {
        this.samples = new ArrayList<>();
        this.totalSamples = 0;
        this.totalErrors = 0;
        this.totalResponseTime = 0;
        this.minResponseTime = Long.MAX_VALUE;
        this.maxResponseTime = Long.MIN_VALUE;
        this.totalBytes = 0;
        this.startTime = Long.MAX_VALUE;
        this.endTime = Long.MIN_VALUE;
    }

    /**
     * Adds a sample result to the statistics.
     *
     * @param result The sample result to add
     */
    public synchronized void addSample(SampleResult result) {
        if (result == null) {
            return;
        }

        samples.add(result);
        totalSamples++;

        if (!result.isSuccessful()) {
            totalErrors++;
        }

        long responseTime = result.getTime();
        totalResponseTime += responseTime;

        if (responseTime < minResponseTime) {
            minResponseTime = responseTime;
        }
        if (responseTime > maxResponseTime) {
            maxResponseTime = responseTime;
        }

        totalBytes += result.getBytesAsLong();

        if (result.getStartTime() < startTime) {
            startTime = result.getStartTime();
        }
        if (result.getEndTime() > endTime) {
            endTime = result.getEndTime();
        }
    }

    /**
     * Gets all collected samples.
     *
     * @return List of all sample results
     */
    public List<SampleResult> getSamples() {
        return new ArrayList<>(samples);
    }

    /**
     * Gets the total number of samples.
     *
     * @return Total sample count
     */
    public long getTotalSamples() {
        return totalSamples;
    }

    /**
     * Gets the total number of errors.
     *
     * @return Total error count
     */
    public long getTotalErrors() {
        return totalErrors;
    }

    /**
     * Gets the error rate as a percentage.
     *
     * @return Error rate percentage (0-100)
     */
    public double getErrorRate() {
        if (totalSamples == 0) {
            return 0.0;
        }
        return (totalErrors * 100.0) / totalSamples;
    }

    /**
     * Gets the average response time in milliseconds.
     *
     * @return Average response time
     */
    public double getAverageResponseTime() {
        if (totalSamples == 0) {
            return 0.0;
        }
        return (double) totalResponseTime / totalSamples;
    }

    /**
     * Gets the minimum response time in milliseconds.
     *
     * @return Minimum response time
     */
    public long getMinResponseTime() {
        return minResponseTime == Long.MAX_VALUE ? 0 : minResponseTime;
    }

    /**
     * Gets the maximum response time in milliseconds.
     *
     * @return Maximum response time
     */
    public long getMaxResponseTime() {
        return maxResponseTime == Long.MIN_VALUE ? 0 : maxResponseTime;
    }

    /**
     * Gets the total bytes received.
     *
     * @return Total bytes
     */
    public long getTotalBytes() {
        return totalBytes;
    }

    /**
     * Gets the throughput in requests per second.
     *
     * @return Throughput (requests/sec)
     */
    public double getThroughput() {
        if (totalSamples == 0 || endTime <= startTime) {
            return 0.0;
        }
        double durationSeconds = (endTime - startTime) / 1000.0;
        return totalSamples / durationSeconds;
    }

    /**
     * Gets the test duration in seconds.
     *
     * @return Duration in seconds
     */
    public double getDurationSeconds() {
        if (endTime <= startTime) {
            return 0.0;
        }
        return (endTime - startTime) / 1000.0;
    }

    /**
     * Clears all collected statistics.
     */
    public void clear() {
        samples.clear();
        totalSamples = 0;
        totalErrors = 0;
        totalResponseTime = 0;
        minResponseTime = Long.MAX_VALUE;
        maxResponseTime = Long.MIN_VALUE;
        totalBytes = 0;
        startTime = Long.MAX_VALUE;
        endTime = Long.MIN_VALUE;
    }
}
