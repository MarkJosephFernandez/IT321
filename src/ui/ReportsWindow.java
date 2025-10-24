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
import java.util.List;

public class ReportsWindow extends JFrame {

    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JButton exportPDFButton, refreshButton;
    private JTextField startDateField, endDateField;

    // DateTimeFormatter for display and potentially for SQL querying
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportsWindow() {
        // --- Frame Setup ---
        setTitle("📊 Sales Reports and Analysis");
        setSize(1000, 600);
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
            // Make cells non-editable
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesTable = new JTable(tableModel);

        formatTableColumns();

        JScrollPane scrollPane = new JScrollPane(salesTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. Button Panel (SOUTH) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        refreshButton = new JButton("🔄 Refresh Data");
        exportPDFButton = new JButton("📄 Export to PDF");

        buttonPanel.add(refreshButton);
        buttonPanel.add(exportPDFButton);

        add(buttonPanel, BorderLayout.SOUTH);

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

        panel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        startDateField = new JTextField(10);
        panel.add(startDateField);

        panel.add(new JLabel("End Date (YYYY-MM-DD):"));
        endDateField = new JTextField(10);
        panel.add(endDateField);

        JButton filterButton = new JButton("Apply Filter");
        filterButton.addActionListener(this::refreshData);
        panel.add(filterButton);

        return panel;
    }

    private void formatTableColumns() {
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        salesTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        salesTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
    }

    // ----------------------------------------------------------------------------------
    // ACTION METHODS
    // ----------------------------------------------------------------------------------

    private void refreshData(ActionEvent e) {
        tableModel.setRowCount(0);
        String startDateStr = startDateField.getText().trim();
        String endDateStr = endDateField.getText().trim();

        if ((!startDateStr.isEmpty() && startDateStr.length() < 10) || (!endDateStr.isEmpty() && endDateStr.length() < 10)) {
            JOptionPane.showMessageDialog(this, "Please enter dates in YYYY-MM-DD format.", "Date Format Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            SaleDAO saleDAO = new SaleDAO();

            List<Sale> sales = saleDAO.getSalesByDateRange(startDateStr, endDateStr);

            for (Sale s : sales) {
                // Format total amount to currency string
                String formattedTotal = String.format("$%.2f", s.getTotalAmount());

                // FIX 1: The getSaleDatetime() method must return LocalDateTime (as per SaleDAO enhancement)
                // Use .format() directly on the LocalDateTime object.
                String formattedDate = s.getSaleDatetime() != null ?
                        s.getSaleDatetime().toGMTString() :
                        "N/A";

                tableModel.addRow(new Object[]{
                        s.getSaleId(),
                        s.getAccountId(),
                        formattedDate,
                        formattedTotal,
                        s.getPaymentMethod(),
                        s.getRemarks()
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading sales data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPDF(ActionEvent e) {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export. Refresh the report first.", "Export Failed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            JFileChooser fileChooser = new JFileChooser();
            String defaultFileName = "sales_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf";
            fileChooser.setSelectedFile(new File(defaultFileName));

            int option = fileChooser.showSaveDialog(this);

            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();

                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    filePath += ".pdf";
                }

                // FIX 2: Calling PDFExporter with 4 arguments is now correct,
                // assuming PDFExporter has been updated with the 4-argument signature.
                PDFExporter.exportSalesToPDF(
                        salesTable,
                        filePath,
                        startDateField.getText().trim(),
                        endDateField.getText().trim()
                );

                JOptionPane.showMessageDialog(this, "PDF Exported Successfully!\n" + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting to PDF: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}