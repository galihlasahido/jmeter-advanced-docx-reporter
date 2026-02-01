package com.jmeter.plugin.docx.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

/**
 * Renders HTML content to images using Selenium WebDriver.
 * This supports JavaScript-rendered pages that OpenHTMLToPDF cannot handle.
 */
public class SeleniumHtmlRenderer {

    private static final Logger log = LoggerFactory.getLogger(SeleniumHtmlRenderer.class);

    // Wait time for JavaScript to render (milliseconds)
    private static final int JS_RENDER_WAIT_MS = 2000;

    private static boolean driverInitialized = false;
    private static String browserType = "chrome"; // or "firefox"

    /**
     * Renders HTML content to a PNG image using headless browser.
     *
     * @param html  The HTML content to render
     * @param width The viewport width
     * @return byte array containing the PNG image, or null if rendering fails
     */
    public static byte[] renderHtmlToImage(String html, int width) {
        if (html == null || html.isEmpty()) {
            return null;
        }

        WebDriver driver = null;
        File tempFile = null;

        try {
            // Initialize driver manager (only once)
            initializeDriverManager();

            // Create temp HTML file
            tempFile = File.createTempFile("jmeter_html_", ".html");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(html);
            }

            // Create headless browser
            driver = createHeadlessDriver(width);

            // Load the HTML file
            driver.get("file://" + tempFile.getAbsolutePath());

            // Wait for JavaScript to render
            Thread.sleep(JS_RENDER_WAIT_MS);

            // Take screenshot
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            log.info("Successfully captured screenshot using Selenium ({} bytes)", screenshot.length);
            return screenshot;

        } catch (Exception e) {
            log.warn("Failed to render HTML using Selenium: {}", e.getMessage(), e);
            return null;
        } finally {
            // Cleanup
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.debug("Error closing WebDriver", e);
                }
            }
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (Exception e) {
                    log.debug("Error deleting temp file", e);
                }
            }
        }
    }

    /**
     * Initialize WebDriverManager (only once per JVM).
     */
    private static synchronized void initializeDriverManager() {
        if (!driverInitialized) {
            try {
                // Try Chrome first
                WebDriverManager.chromedriver().setup();
                browserType = "chrome";
                driverInitialized = true;
                log.info("Initialized Chrome WebDriver");
            } catch (Exception e) {
                log.debug("Chrome not available, trying Firefox: {}", e.getMessage());
                try {
                    // Fall back to Firefox
                    WebDriverManager.firefoxdriver().setup();
                    browserType = "firefox";
                    driverInitialized = true;
                    log.info("Initialized Firefox WebDriver");
                } catch (Exception e2) {
                    log.warn("Neither Chrome nor Firefox available for Selenium rendering");
                    throw new RuntimeException("No browser available for Selenium", e2);
                }
            }
        }
    }

    /**
     * Creates a headless browser driver.
     */
    private static WebDriver createHeadlessDriver(int width) {
        if ("firefox".equals(browserType)) {
            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("--headless");
            options.addArguments("--width=" + width);
            options.addArguments("--height=900");
            return new FirefoxDriver(options);
        } else {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=" + width + ",900");
            return new ChromeDriver(options);
        }
    }

    /**
     * Check if Selenium rendering is available (browser installed).
     */
    public static boolean isAvailable() {
        try {
            initializeDriverManager();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
