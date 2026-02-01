package com.jmeter.plugin.docx.util;

import org.apache.jmeter.samplers.SampleResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to resolve sample variables from JMeter SampleResult.
 * Supports placeholders like %responseTime%, %latency%, etc.
 */
public class SampleVariableResolver {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("%([a-zA-Z0-9_<>]+)%");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Resolves all variables in the given text using values from SampleResult.
     *
     * @param text   The text containing variable placeholders
     * @param result The SampleResult to extract values from
     * @return Text with all variables replaced with actual values
     */
    public static String resolve(String text, SampleResult result) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        Map<String, String> variables = extractVariables(result);
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1);
            String value = variables.getOrDefault(varName, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Extracts all available variables from a SampleResult.
     *
     * @param result The SampleResult to extract values from
     * @return Map of variable names to their values
     */
    public static Map<String, String> extractVariables(SampleResult result) {
        Map<String, String> vars = new HashMap<>();

        if (result == null) {
            return vars;
        }

        // Time-related variables
        vars.put("endTime", DATE_FORMAT.format(new Date(result.getEndTime())));
        vars.put("endTimeMillis", String.format("%.3f", result.getEndTime() / 1000.0));
        vars.put("startTime", DATE_FORMAT.format(new Date(result.getStartTime())));
        vars.put("startTimeMillis", String.format("%.3f", result.getStartTime() / 1000.0));

        // Response time variables
        vars.put("responseTime", String.valueOf(result.getTime()));
        vars.put("responseTimeMicros", String.valueOf(result.getTime() * 1000));
        vars.put("latency", String.valueOf(result.getLatency()));
        vars.put("latencyMicros", String.valueOf(result.getLatency() * 1000));
        vars.put("connectTime", String.valueOf(result.getConnectTime()));

        // Success/Failure variables
        vars.put("isSuccessful", String.valueOf(result.isSuccessful()));
        vars.put("isFailed", String.valueOf(!result.isSuccessful()));

        // Size variables
        vars.put("receivedBytes", String.valueOf(result.getBytesAsLong()));
        vars.put("sentBytes", String.valueOf(result.getSentBytes()));
        vars.put("responseSize", String.valueOf(result.getBodySizeAsLong()));
        vars.put("responseHeadersSize", String.valueOf(result.getHeadersSize()));

        // Response variables
        vars.put("responseCode", result.getResponseCode() != null ? result.getResponseCode() : "");
        vars.put("responseMessage", result.getResponseMessage() != null ? result.getResponseMessage() : "");
        vars.put("responseData", result.getResponseDataAsString() != null ? result.getResponseDataAsString() : "");
        vars.put("responseHeaders", result.getResponseHeaders() != null ? result.getResponseHeaders() : "");
        vars.put("responseFileName", result.getResultFileName() != null ? result.getResultFileName() : "");

        // Request variables
        vars.put("requestData", result.getSamplerData() != null ? result.getSamplerData() : "");

        // Sample identification
        vars.put("sampleLabel", result.getSampleLabel() != null ? result.getSampleLabel() : "");
        vars.put("URL", result.getUrlAsString() != null ? result.getUrlAsString() : "");

        // Thread variables
        vars.put("threadName", result.getThreadName() != null ? result.getThreadName() : "");
        vars.put("threadCount", String.valueOf(result.getAllThreads()));
        vars.put("grpThreads", String.valueOf(result.getGroupThreads()));

        // Count variables
        vars.put("sampleCount", String.valueOf(result.getSampleCount()));
        vars.put("errorCount", String.valueOf(result.getErrorCount()));

        return vars;
    }

    /**
     * Gets a list of all available variable names with descriptions.
     *
     * @return Map of variable names to their descriptions
     */
    public static Map<String, String> getAvailableVariables() {
        Map<String, String> vars = new HashMap<>();

        vars.put("endTime", "Epoch time when the request was ended");
        vars.put("endTimeMillis", "Same as endTime, but divided by 1000 (surrogate field)");
        vars.put("startTime", "Epoch time when the request was started");
        vars.put("startTimeMillis", "Same as startTime, but divided by 1000 (surrogate field)");
        vars.put("responseTime", "Response time, time to full response loaded");
        vars.put("responseTimeMicros", "Same as responseTime, but multiplied by 1000 (surrogate field)");
        vars.put("latency", "Latency, time to first response byte received (if available)");
        vars.put("latencyMicros", "Same as latency, but multiplied by 1000 (surrogate field)");
        vars.put("connectTime", "Time to establish connection");
        vars.put("isSuccessful", "If response was marked as successful");
        vars.put("isFailed", "If response was marked as failed (surrogate field)");
        vars.put("receivedBytes", "Number of request bytes received (if available)");
        vars.put("sentBytes", "Number of request bytes sent (if available)");
        vars.put("responseSize", "Size of response body");
        vars.put("responseHeadersSize", "Size of response headers");
        vars.put("responseCode", "Response code (eg. 200, 404, etc.)");
        vars.put("responseMessage", "Response message (eg. OK, Not Found, etc.)");
        vars.put("responseData", "Response data");
        vars.put("responseHeaders", "Response headers (if present in sample)");
        vars.put("responseFileName", "Response file name");
        vars.put("requestData", "Request data from sample");
        vars.put("sampleLabel", "Name of the sampler that made the request");
        vars.put("URL", "The sample URL");
        vars.put("threadName", "Name of thread in Thread Group that processed the request");
        vars.put("threadCount", "Total number of active threads in all groups");
        vars.put("grpThreads", "Number of active threads in this thread group");
        vars.put("sampleCount", "Number of samples (1, unless multiple samples are aggregated)");
        vars.put("errorCount", "Number of errors (0 or 1, unless multiple samples are aggregated)");

        return vars;
    }
}
