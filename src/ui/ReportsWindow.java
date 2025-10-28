package ui;

import dao.SaleDAO;
import models.Sale;
import pdf_export.PDFExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ReportsWindow extends JFrame {

    // --- Constants ---
    // Use the full format for display/DAO querying.
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // Use a shorter format for user input/filtering.
    private static final String DATE_FORMAT_HINT = "yyyy-MM-dd";

    // --- UI Components ---
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JTextField startDateField, endDateField;
    private JLabel totalSalesLabel; // New component for displaying total amount

    public ReportsWindow() {
        // --- Frame Setup ---
        setTitle("📊 Sales Reports and Analysis");
        setSize(1200, 700); // Increased size
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 1. Filter Panel (NORTH) ---
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);

        // --- 2. Table Setup (CENTER) ---
        String[] cols = {"Sale ID", "Account ID", "Date/Time", "Total Amount", "Payment Method", "Remarks"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            // Use correct column types for sorting/rendering
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 1) return Integer.class; // IDs
                if (columnIndex == 3) return Double.class; // Total Amount for sorting
                return super.getColumnClass(columnIndex);
            }
        };
        salesTable = new JTable(tableModel);
        // Enable row sorting
        salesTable.setAutoCreateRowSorter(true);

        formatTableColumns();

        JScrollPane scrollPane = new JScrollPane(salesTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. Summary and Button Panel (SOUTH) ---
        JPanel southPanel = new JPanel(new BorderLayout());

        // Summary Label
        totalSalesLabel = new JLabel("Total Sales Amount: $0.00", SwingConstants.LEFT);
        totalSalesLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        southPanel.add(totalSalesLabel, BorderLayout.WEST);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        JButton refreshButton = new JButton("🔄 Refresh Data");
        JButton exportPDFButton = new JButton("📄 Export to PDF");

        buttonPanel.add(refreshButton);
        buttonPanel.add(exportPDFButton);

        southPanel.add(buttonPanel, BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);

        // --- 4. Actions ---
        refreshButton.addActionListener(this::refreshData);
        exportPDFButton.addActionListener(this::exportPDF);

        // Initial Load
        refreshData(null);
    }

    // ----------------------------------------------------------------------------------
    // UI HELPER METHODS
    // ----------------------------------------------------------------------------------

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Filter Sales by Date"));

        panel.add(new JLabel("Start Date (" + DATE_FORMAT_HINT + "):"));
        startDateField = new JTextField(10);
        panel.add(startDateField);

        panel.add(new JLabel("End Date (" + DATE_FORMAT_HINT + "):"));
        endDateField = new JTextField(10);
        panel.add(endDateField);

        JButton filterButton = new JButton("Apply Filter");
        filterButton.addActionListener(this::refreshData);
        panel.add(filterButton);

        return panel;
    }

    private void formatTableColumns() {
        // Renderers for alignment
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Currency Renderer
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Double) {
                    value = String.format("$%.2f", (Double) value);
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };

        // Apply Renderers
        salesTable.getColumnModel().getColumn(3).setCellRenderer(currencyRenderer); // Total Amount
        salesTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);  // Date/Time

        // Preferred column widths for better layout
        salesTable.getColumnModel().getColumn(0).setPreferredWidth(50); // Sale ID
        salesTable.getColumnModel().getColumn(1).setPreferredWidth(70); // Account ID
        salesTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Date/Time
        salesTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Total Amount
        salesTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Payment Method
    }

    private void updateSummary(List<Sale> sales) {
        double totalAmount = sales.stream()
                .mapToDouble(Sale::getTotalAmount)
                .sum();
        totalSalesLabel.setText(String.format("Total Sales Amount: $%.2f (Showing %d Records)", totalAmount, sales.size()));
    }

    // ----------------------------------------------------------------------------------
    // ACTION METHODS
    // ----------------------------------------------------------------------------------

    private void refreshData(ActionEvent e) {
        tableModel.setRowCount(0);
        double totalSales = 0.0;
        String startDateStr = startDateField.getText().trim();
        String endDateStr = endDateField.getText().trim();

        // 1. Validate Date Inputs (Basic format check)
        if (!startDateStr.isEmpty() && !isValidDate(startDateStr)) {
            JOptionPane.showMessageDialog(this, "Invalid Start Date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!endDateStr.isEmpty() && !isValidDate(endDateStr)) {
            JOptionPane.showMessageDialog(this, "Invalid End Date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Adjust dates for proper SQL querying (e.g., to cover the entire day)
        String sqlStartDate = startDateStr.isEmpty() ? "" : startDateStr + " 00:00:00";
        String sqlEndDate = endDateStr.isEmpty() ? "" : endDateStr + " 23:59:59";


        try {
            SaleDAO saleDAO = new SaleDAO();
            List<Sale> sales = saleDAO.getSalesByDateRange(sqlStartDate, sqlEndDate);

            for (Sale s : sales) {
                // Sum for summary
                totalSales += s.getTotalAmount();

                // Format total amount: pass as Double for correct sorting, display via Renderer
                Double totalAmount = s.getTotalAmount();

                // Format Date/Time
                String formattedDate = s.getSaleDatetime() != null ?
                        s.getSaleDatetime().toGMTString() :
                        "N/A";

                tableModel.addRow(new Object[]{
                        s.getSaleId(),
                        s.getAccountId(),
                        formattedDate,
                        totalAmount, // Pass Double value
                        s.getPaymentMethod(),
                        s.getRemarks()
                });
            }

            // Update Summary
            updateSummary(sales);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading sales data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            totalSalesLabel.setText("Total Sales Amount: $0.00 (Error Loading)");
        }
    }

    private boolean isValidDate(String dateStr) {
        if (dateStr.length() != 10) return false;
        try {
            // Attempt to parse just the date part (YYYY-MM-DD) to validate format
            java.time.LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void exportPDF(ActionEvent e) {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export. Refresh the report first.", "Export Failed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            JFileChooser fileChooser = new JFileChooser();

            // Set the current directory to user's Documents folder or current directory
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

            String defaultFileName = "sales_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf";
            fileChooser.setSelectedFile(new File(defaultFileName));

            // Add file filter for PDF files
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));

            int option = fileChooser.showSaveDialog(this);

            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();

                // Ensure .pdf extension
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    filePath += ".pdf";
                    file = new File(filePath);
                }

                // Pass the raw filter dates (YYYY-MM-DD) for the PDF header
                PDFExporter.exportSalesToPDF(
                        salesTable,
                        filePath,
                        startDateField.getText().trim(),
                        endDateField.getText().trim()
                );

                // Verify file was created
                if (file.exists()) {
                    // Show success message with clickable path
                    int result = JOptionPane.showConfirmDialog(
                            this,
                            "PDF Exported Successfully!\n\nFile saved to:\n" + filePath + "\n\nWould you like to open the folder?",
                            "Export Success",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    // Open folder if user clicks Yes
                    if (result == JOptionPane.YES_OPTION) {
                        try {
                            Desktop.getDesktop().open(file.getParentFile());
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this,
                                    "File saved but couldn't open folder automatically.\nPath: " + filePath,
                                    "Info",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Export completed but file verification failed.\nExpected location: " + filePath,
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error exporting to PDF: " + ex.getMessage() + "\n\nPlease check:\n" +
                            "1. You have write permissions to the selected folder\n" +
                            "2. The file is not already open in another program\n" +
                            "3. There is enough disk space",
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}