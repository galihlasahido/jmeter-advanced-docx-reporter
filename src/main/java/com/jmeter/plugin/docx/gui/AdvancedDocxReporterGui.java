package com.jmeter.plugin.docx.gui;

import com.jmeter.plugin.docx.reporter.AdvancedDocxReporter;
import com.jmeter.plugin.docx.reporter.DocxReportGenerator;
import com.jmeter.plugin.docx.util.SampleVariableResolver;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Map;
import java.util.TreeMap;

/**
 * GUI for the Advanced DOCX Reporter listener.
 */
public class AdvancedDocxReporterGui extends AbstractListenerGui {

    private static final long serialVersionUID = 1L;

    private FilePanel filenamePanel;
    private JCheckBox overwriteCheckbox;
    private JTextField documentTitleField;
    private JTextField pageHeaderField;
    private JComboBox<String> displayModeCombo;
    private JTextField recordHeaderField;
    private JTextArea recordFormatArea;
    private JCheckBox includeSummaryCheckbox;
    private JCheckBox renderHtmlAsImageCheckbox;
    private JTextArea footerSectionArea;

    public AdvancedDocxReporterGui() {
        super();
        init();
    }

    /**
     * Initialize the GUI components.
     */
    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        // Main container
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Add standard title
        add(makeTitlePanel(), BorderLayout.NORTH);

        // File settings panel
        JPanel filePanel = createFileSettingsPanel();
        mainPanel.add(filePanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Document settings panel
        JPanel documentPanel = createDocumentSettingsPanel();
        mainPanel.add(documentPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Record settings panel
        JPanel recordPanel = createRecordSettingsPanel();
        mainPanel.add(recordPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Summary settings panel
        JPanel summaryPanel = createSummarySettingsPanel();
        mainPanel.add(summaryPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // HTML response settings panel
        JPanel htmlPanel = createHtmlResponseSettingsPanel();
        mainPanel.add(htmlPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Footer settings panel
        JPanel footerPanel = createFooterSettingsPanel();
        mainPanel.add(footerPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Available variables panel
        JPanel variablesPanel = createVariablesPanel();
        mainPanel.add(variablesPanel);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Creates the file settings panel.
     */
    private JPanel createFileSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Output File"));

        filenamePanel = new FilePanel("Filename:", ".docx");
        panel.add(filenamePanel, BorderLayout.CENTER);

        overwriteCheckbox = new JCheckBox("Overwrite existing file");
        overwriteCheckbox.setSelected(true);
        panel.add(overwriteCheckbox, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the document settings panel.
     */
    private JPanel createDocumentSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Document Settings"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 5, 2, 5);

        // Document Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Document Title:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        documentTitleField = new JTextField(30);
        documentTitleField.setText("Performance Test Report");
        panel.add(documentTitleField, gbc);

        // Page Header
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Page Header:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        pageHeaderField = new JTextField(30);
        panel.add(pageHeaderField, gbc);

        return panel;
    }

    /**
     * Creates the record settings panel.
     */
    private JPanel createRecordSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Record Settings"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 5, 2, 5);

        // Display Mode
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Record Display Mode:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        displayModeCombo = new JComboBox<>(new String[]{
                DocxReportGenerator.MODE_TABLE,
                DocxReportGenerator.MODE_PARAGRAPH
        });
        panel.add(displayModeCombo, gbc);

        // Record Header
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Record Header:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        recordHeaderField = new JTextField(40);
        recordHeaderField.setText("Sample Label | Response Time (ms) | Status | Response Code");
        panel.add(recordHeaderField, gbc);

        // Record Format
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Record each sample as:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        recordFormatArea = new JTextArea(3, 40);
        recordFormatArea.setText("%sampleLabel% | %responseTime% | %isSuccessful% | %responseCode%");
        recordFormatArea.setLineWrap(true);
        recordFormatArea.setWrapStyleWord(true);
        JScrollPane formatScroll = new JScrollPane(recordFormatArea);
        panel.add(formatScroll, gbc);

        return panel;
    }

    /**
     * Creates the summary settings panel.
     */
    private JPanel createSummarySettingsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Summary Statistics"));

        includeSummaryCheckbox = new JCheckBox("Include Summary Statistics at the end");
        includeSummaryCheckbox.setSelected(true);
        panel.add(includeSummaryCheckbox);

        JLabel infoLabel = new JLabel("(Total Samples, Avg/Min/Max Response Time, Error Rate, Throughput)");
        infoLabel.setForeground(Color.GRAY);
        panel.add(infoLabel);

        return panel;
    }

    /**
     * Creates the HTML response settings panel.
     */
    private JPanel createHtmlResponseSettingsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("HTML Response Rendering"));

        renderHtmlAsImageCheckbox = new JCheckBox("Render HTML responses as images");
        renderHtmlAsImageCheckbox.setSelected(false);
        panel.add(renderHtmlAsImageCheckbox);

        JLabel infoLabel = new JLabel("(Converts HTML response data to screenshot images in the report)");
        infoLabel.setForeground(Color.GRAY);
        panel.add(infoLabel);

        return panel;
    }

    /**
     * Creates the footer settings panel.
     */
    private JPanel createFooterSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Footer Section"));

        footerSectionArea = new JTextArea(2, 40);
        footerSectionArea.setText("Generated by JMeter Advanced DOCX Reporter");
        footerSectionArea.setLineWrap(true);
        footerSectionArea.setWrapStyleWord(true);
        JScrollPane footerScroll = new JScrollPane(footerSectionArea);
        panel.add(footerScroll, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the available variables panel.
     */
    private JPanel createVariablesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Available sample fields (click any button to copy the field to clipboard)"));

        // Get variables sorted by name
        Map<String, String> variables = new TreeMap<>(SampleVariableResolver.getAvailableVariables());

        // Create panel with variable buttons
        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 5, 2, 5);

        int row = 0;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String varName = entry.getKey();
            String description = entry.getValue();

            // Variable button
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            JButton varButton = new JButton(varName);
            varButton.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            varButton.setMargin(new Insets(2, 5, 2, 5));
            varButton.addActionListener(e -> copyToClipboard("%" + varName + "%"));
            varButton.setToolTipText("Click to copy %" + varName + "% to clipboard");
            buttonsPanel.add(varButton, gbc);

            // Description label
            gbc.gridx = 1;
            gbc.weightx = 1;
            JLabel descLabel = new JLabel(description);
            descLabel.setForeground(Color.DARK_GRAY);
            buttonsPanel.add(descLabel, gbc);

            row++;
        }

        JScrollPane scrollPane = new JScrollPane(buttonsPanel);
        scrollPane.setPreferredSize(new Dimension(600, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Copies text to the system clipboard.
     */
    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

        // Show brief feedback
        JOptionPane.showMessageDialog(this,
                "Copied to clipboard: " + text,
                "Copied",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public String getStaticLabel() {
        return "Advanced DOCX Reporter";
    }

    @Override
    public String getLabelResource() {
        return getClass().getCanonicalName();
    }

    @Override
    public TestElement createTestElement() {
        AdvancedDocxReporter reporter = new AdvancedDocxReporter();
        modifyTestElement(reporter);
        return reporter;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);

        if (element instanceof AdvancedDocxReporter) {
            AdvancedDocxReporter reporter = (AdvancedDocxReporter) element;
            reporter.setFilename(filenamePanel.getFilename());
            reporter.setOverwrite(overwriteCheckbox.isSelected());
            reporter.setDocumentTitle(documentTitleField.getText());
            reporter.setPageHeader(pageHeaderField.getText());
            reporter.setDisplayMode((String) displayModeCombo.getSelectedItem());
            reporter.setRecordHeader(recordHeaderField.getText());
            reporter.setRecordFormat(recordFormatArea.getText());
            reporter.setIncludeSummary(includeSummaryCheckbox.isSelected());
            reporter.setRenderHtmlAsImage(renderHtmlAsImageCheckbox.isSelected());
            reporter.setFooterSection(footerSectionArea.getText());
        }
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);

        if (element instanceof AdvancedDocxReporter) {
            AdvancedDocxReporter reporter = (AdvancedDocxReporter) element;
            filenamePanel.setFilename(reporter.getFilename());
            overwriteCheckbox.setSelected(reporter.isOverwrite());
            documentTitleField.setText(reporter.getDocumentTitle());
            pageHeaderField.setText(reporter.getPageHeader());
            displayModeCombo.setSelectedItem(reporter.getDisplayMode());
            recordHeaderField.setText(reporter.getRecordHeader());
            recordFormatArea.setText(reporter.getRecordFormat());
            includeSummaryCheckbox.setSelected(reporter.isIncludeSummary());
            renderHtmlAsImageCheckbox.setSelected(reporter.isRenderHtmlAsImage());
            footerSectionArea.setText(reporter.getFooterSection());
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        filenamePanel.setFilename("report.docx");
        overwriteCheckbox.setSelected(true);
        documentTitleField.setText("Performance Test Report");
        pageHeaderField.setText("");
        displayModeCombo.setSelectedIndex(0);
        recordHeaderField.setText("Sample Label | Response Time (ms) | Status | Response Code");
        recordFormatArea.setText("%sampleLabel% | %responseTime% | %isSuccessful% | %responseCode%");
        includeSummaryCheckbox.setSelected(true);
        renderHtmlAsImageCheckbox.setSelected(false);
        footerSectionArea.setText("Generated by JMeter Advanced DOCX Reporter");
    }
}
