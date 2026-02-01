package com.jmeter.plugin.docx.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to render HTML content to images.
 * Uses Selenium WebDriver with headless Chrome for accurate rendering
 * of modern HTML/CSS/JavaScript pages.
 */
public class HtmlToImageRenderer {

    private static final Logger log = LoggerFactory.getLogger(HtmlToImageRenderer.class);

    /**
     * Checks if the given content appears to be HTML.
     *
     * @param content The content to check
     * @return true if the content appears to be HTML
     */
    public static boolean isHtml(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        String trimmed = content.trim().toLowerCase();
        return trimmed.startsWith("<!doctype html") ||
               trimmed.startsWith("<html") ||
               (trimmed.startsWith("<?xml") && trimmed.contains("<html"));
    }

    /**
     * Renders HTML content to a PNG image.
     *
     * @param html The HTML content to render
     * @return byte array containing the PNG image, or null if rendering fails
     */
    public static byte[] renderHtmlToImage(String html) {
        return renderHtmlToImage(html, 1280);
    }

    /**
     * Renders HTML content to a PNG image with specified width.
     * Uses Selenium WebDriver with headless Chrome for rendering.
     *
     * @param html  The HTML content to render
     * @param width The viewport width for rendering
     * @return byte array containing the PNG image, or null if rendering fails
     */
    public static byte[] renderHtmlToImage(String html, int width) {
        if (html == null || html.isEmpty()) {
            return null;
        }

        try {
            byte[] imageBytes = SeleniumHtmlRenderer.renderHtmlToImage(html, width);
            if (imageBytes != null && imageBytes.length > 0) {
                log.info("Successfully rendered HTML to image ({} bytes)", imageBytes.length);
                return imageBytes;
            }
        } catch (Exception e) {
            log.warn("Failed to render HTML to image: {}", e.getMessage());
            log.debug("HTML rendering error details", e);
        }

        return null;
    }
}
