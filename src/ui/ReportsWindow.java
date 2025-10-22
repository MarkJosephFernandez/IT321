package ui;

import dao.SaleDAO;
import models.Sale;
import pdf_export.PDFExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

public class ReportsWindow extends JFrame {

    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JButton exportPDFButton, exportCSVButton, refreshButton;

    public ReportsWindow() {
        setTitle("Sales Reports");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // === Table ===
        String[] cols = {"Sale ID", "Account ID", "Date/Time", "Total Amount", "Payment Method", "Remarks"};
        tableModel = new DefaultTableModel(cols, 0);
        salesTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(salesTable);

        // === Buttons ===
        JPanel buttonPanel = new JPanel();
        refreshButton = new JButton("Refresh");
        exportPDFButton = new JButton("Export PDF");
        exportCSVButton = new JButton("Export CSV");

        buttonPanel.add(refreshButton);
        buttonPanel.add(exportPDFButton);
        buttonPanel.add(exportCSVButton);

        // === Add components ===
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // === Actions ===
        refreshButton.addActionListener(this::refreshData);
        exportPDFButton.addActionListener(this::exportPDF);
        exportCSVButton.addActionListener(this::exportCSV);

        refreshData(null);
    }

    private void refreshData(ActionEvent e) {
        try {
            tableModel.setRowCount(0);
            SaleDAO saleDAO = new SaleDAO();
            List<Sale> sales = saleDAO.getAllSales();

            for (Sale s : sales) {
                tableModel.addRow(new Object[]{
                        s.getSaleId(),
                        s.getAccountId(),
                        s.getSaleDatetime(),
                        s.getTotalAmount(),
                        s.getPaymentMethod(),
                        s.getRemarks()
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading sales data.");
        }
    }

    private void exportPDF(ActionEvent e) {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("sales_report.pdf"));
            int option = fileChooser.showSaveDialog(this);

            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();

                // Ensure .pdf extension
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    filePath += ".pdf";
                }

                PDFExporter.exportSalesToPDF(tableModel, filePath);
                JOptionPane.showMessageDialog(this, "PDF Exported Successfully!\n" + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting to PDF: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCSV(ActionEvent e) {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("sales_report.csv"));
            int option = fileChooser.showSaveDialog(this);

            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();

                // Ensure .csv extension
                if (!filePath.toLowerCase().endsWith(".csv")) {
                    filePath += ".csv";
                }

                PDFExporter.exportSalesToCSV(tableModel, filePath);
                JOptionPane.showMessageDialog(this, "CSV Exported Successfully!\n" + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting to CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}