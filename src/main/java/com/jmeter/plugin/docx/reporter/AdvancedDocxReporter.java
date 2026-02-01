package com.jmeter.plugin.docx.reporter;

import com.jmeter.plugin.docx.util.SeleniumHtmlRenderer;
import com.jmeter.plugin.docx.util.SummaryStatistics;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * JMeter Listener that generates DOCX reports with customizable format.
 * <p>
 * This listener collects sample results during test execution and generates
 * a Word document report at the end of the test.
 */
public class AdvancedDocxReporter extends AbstractListenerElement
        implements SampleListener, TestStateListener, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AdvancedDocxReporter.class);

    // Property keys
    public static final String FILENAME = "AdvancedDocxReporter.filename";
    public static final String OVERWRITE = "AdvancedDocxReporter.overwrite";
    public static final String DOCUMENT_TITLE = "AdvancedDocxReporter.documentTitle";
    public static final String PAGE_HEADER = "AdvancedDocxReporter.pageHeader";
    public static final String RECORD_HEADER = "AdvancedDocxReporter.recordHeader";
    public static final String RECORD_FORMAT = "AdvancedDocxReporter.recordFormat";
    public static final String FOOTER_SECTION = "AdvancedDocxReporter.footerSection";
    public static final String DISPLAY_MODE = "AdvancedDocxReporter.displayMode";
    public static final String INCLUDE_SUMMARY = "AdvancedDocxReporter.includeSummary";
    public static final String RENDER_HTML_AS_IMAGE = "AdvancedDocxReporter.renderHtmlAsImage";

    // Static field to share statistics across all instances (JMeter may create multiple instances)
    private static volatile SummaryStatistics sharedStatistics;
    private static final Object lock = new Object();

    public AdvancedDocxReporter() {
        super();
    }

    /**
     * Gets or creates the shared statistics instance.
     */
    private SummaryStatistics getStatistics() {
        synchronized (lock) {
            if (sharedStatistics == null) {
                sharedStatistics = new SummaryStatistics();
            }
            return sharedStatistics;
        }
    }

    /**
     * Called when the test starts.
     */
    @Override
    public void testStarted() {
        testStarted("");
    }

    @Override
    public void testStarted(String host) {
        System.out.println("[Advanced DOCX Reporter] Test started on host: " + host);
        log.info("Advanced DOCX Reporter: Test started on host {}", host);

        synchronized (lock) {
            sharedStatistics = new SummaryStatistics();
        }

        // Pre-initialize Selenium WebDriver in the background to avoid delays during report generation
        if (isRenderHtmlAsImage()) {
            new Thread(() -> {
                try {
                    log.info("Pre-initializing Selenium WebDriver...");
                    SeleniumHtmlRenderer.isAvailable();
                    log.info("Selenium WebDriver pre-initialization complete");
                } catch (Exception e) {
                    log.debug("Selenium pre-initialization failed (will try again during report generation): {}", e.getMessage());
                }
            }, "selenium-init").start();
        }
    }

    /**
     * Called when the test ends.
     */
    @Override
    public void testEnded() {
        testEnded("");
    }

    @Override
    public void testEnded(String host) {
        System.out.println("[Advanced DOCX Reporter] Test ended on host: " + host);
        log.info("Advanced DOCX Reporter: Test ended on host {}", host);

        SummaryStatistics statistics = getStatistics();

        if (statistics.getTotalSamples() == 0) {
            System.out.println("[Advanced DOCX Reporter] WARNING: No samples collected, skipping report generation");
            log.warn("No samples collected, skipping report generation");
            return;
        }

        System.out.println("[Advanced DOCX Reporter] Total samples collected: " + statistics.getTotalSamples());

        String filename = getFilename();
        if (filename == null || filename.isEmpty()) {
            System.out.println("[Advanced DOCX Reporter] ERROR: Filename is not specified");
            log.error("Filename is not specified, cannot generate report");
            return;
        }

        System.out.println("[Advanced DOCX Reporter] Generating report to: " + filename);

        // Check if file exists and overwrite is disabled
        File outputFile = new File(filename);
        if (outputFile.exists() && !isOverwrite()) {
            System.out.println("[Advanced DOCX Reporter] WARNING: File exists and overwrite is disabled");
            log.warn("File {} already exists and overwrite is disabled, skipping report generation", filename);
            return;
        }

        // Create parent directories if needed
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            System.out.println("[Advanced DOCX Reporter] Creating directory: " + parentDir.getAbsolutePath());
            if (!parentDir.mkdirs()) {
                System.out.println("[Advanced DOCX Reporter] ERROR: Failed to create directory");
                log.error("Failed to create directory: {}", parentDir.getAbsolutePath());
                return;
            }
        }

        try {
            DocxReportGenerator reportGenerator = new DocxReportGenerator();

            // Configure report generator from properties
            reportGenerator.setDocumentTitle(getDocumentTitle());
            reportGenerator.setPageHeader(getPageHeader());
            reportGenerator.setRecordHeader(getRecordHeader());
            reportGenerator.setRecordFormat(getRecordFormat());
            reportGenerator.setFooterSection(getFooterSection());
            reportGenerator.setDisplayMode(getDisplayMode());
            reportGenerator.setIncludeSummary(isIncludeSummary());
            reportGenerator.setRenderHtmlAsImage(isRenderHtmlAsImage());

            reportGenerator.generateReport(filename, statistics);
            System.out.println("[Advanced DOCX Reporter] SUCCESS: Report generated at " + filename);
            log.info("DOCX report generated successfully: {}", filename);
        } catch (IOException e) {
            System.out.println("[Advanced DOCX Reporter] ERROR: Failed to generate report - " + e.getMessage());
            e.printStackTrace();
            log.error("Failed to generate DOCX report: {}", e.getMessage(), e);
        } catch (Exception e) {
            System.out.println("[Advanced DOCX Reporter] ERROR: Unexpected error - " + e.getMessage());
            e.printStackTrace();
            log.error("Unexpected error generating DOCX report: {}", e.getMessage(), e);
        }
    }

    /**
     * Called when a sample occurs.
     * Only adds the main sample result, not sub-results (redirect intermediate responses).
     */
    @Override
    public void sampleOccurred(SampleEvent event) {
        SampleResult result = event.getResult();
        SummaryStatistics statistics = getStatistics();

        // Only add the main sample, skip sub-results (redirect intermediate responses)
        // The main sample contains the final response after following redirects
        statistics.addSample(result);
    }

    @Override
    public void sampleStarted(SampleEvent event) {
        // Not used
    }

    @Override
    public void sampleStopped(SampleEvent event) {
        // Not used
    }

    // Property getters and setters

    public String getFilename() {
        return getPropertyAsString(FILENAME, "report.docx");
    }

    public void setFilename(String filename) {
        setProperty(FILENAME, filename);
    }

    public boolean isOverwrite() {
        return getPropertyAsBoolean(OVERWRITE, true);
    }

    public void setOverwrite(boolean overwrite) {
        setProperty(OVERWRITE, overwrite);
    }

    public String getDocumentTitle() {
        return getPropertyAsString(DOCUMENT_TITLE, "Performance Test Report");
    }

    public void setDocumentTitle(String title) {
        setProperty(DOCUMENT_TITLE, title);
    }

    public String getPageHeader() {
        return getPropertyAsString(PAGE_HEADER, "");
    }

    public void setPageHeader(String header) {
        setProperty(PAGE_HEADER, header);
    }

    public String getRecordHeader() {
        return getPropertyAsString(RECORD_HEADER, "Sample Label | Response Time (ms) | Status | Response Code");
    }

    public void setRecordHeader(String header) {
        setProperty(RECORD_HEADER, header);
    }

    public String getRecordFormat() {
        return getPropertyAsString(RECORD_FORMAT, "%sampleLabel% | %responseTime% | %isSuccessful% | %responseCode%");
    }

    public void setRecordFormat(String format) {
        setProperty(RECORD_FORMAT, format);
    }

    public String getFooterSection() {
        return getPropertyAsString(FOOTER_SECTION, "Generated by JMeter Advanced DOCX Reporter");
    }

    public void setFooterSection(String footer) {
        setProperty(FOOTER_SECTION, footer);
    }

    public String getDisplayMode() {
        return getPropertyAsString(DISPLAY_MODE, DocxReportGenerator.MODE_TABLE);
    }

    public void setDisplayMode(String mode) {
        setProperty(DISPLAY_MODE, mode);
    }

    public boolean isIncludeSummary() {
        return getPropertyAsBoolean(INCLUDE_SUMMARY, true);
    }

    public void setIncludeSummary(boolean include) {
        setProperty(INCLUDE_SUMMARY, include);
    }

    public boolean isRenderHtmlAsImage() {
        return getPropertyAsBoolean(RENDER_HTML_AS_IMAGE, false);
    }

    public void setRenderHtmlAsImage(boolean render) {
        setProperty(RENDER_HTML_AS_IMAGE, render);
    }
}
