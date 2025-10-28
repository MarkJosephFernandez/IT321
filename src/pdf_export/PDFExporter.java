package pdf_export;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PDFExporter {

    /**
     * Export sales data to PDF format (with date range filter info)
     */
    public static void exportSalesToPDF(JTable salesTable, String filePath, String startDate, String endDate) throws Exception {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("Sales Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Add date range filter info if applicable
        if (!startDate.isEmpty() || !endDate.isEmpty()) {
            Font filterFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.DARK_GRAY);
            String filterText = "Filtered by Date: ";
            if (!startDate.isEmpty() && !endDate.isEmpty()) {
                filterText += startDate + " to " + endDate;
            } else if (!startDate.isEmpty()) {
                filterText += "From " + startDate;
            } else {
                filterText += "Until " + endDate;
            }
            Paragraph filter = new Paragraph(filterText, filterFont);
            filter.setAlignment(Element.ALIGN_CENTER);
            filter.setSpacingAfter(15);
            document.add(filter);
        }

        // Add generation date
        Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        Paragraph date = new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dateFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        date.setSpacingAfter(20);
        document.add(date);

        // Get table model
        TableModel tableModel = salesTable.getModel();

        // Create PDF table
        PdfPTable pdfTable = new PdfPTable(tableModel.getColumnCount());
        pdfTable.setWidthPercentage(100);

        // Set column widths (adjust based on your columns)
        float[] columnWidths = {1f, 1.2f, 2.5f, 1.5f, 1.8f, 3f}; // Adjust as needed
        pdfTable.setWidths(columnWidths);

        // Add headers
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            PdfPCell cell = new PdfPCell(new Phrase(tableModel.getColumnName(i), headerFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            pdfTable.addCell(cell);
        }

        // Add data rows
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        double totalAmount = 0.0;

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                Object value = tableModel.getValueAt(row, col);
                String cellText = "";

                // Format the cell value
                if (value != null) {
                    if (col == 3 && value instanceof Double) { // Total Amount column
                        cellText = String.format("$%.2f", (Double) value);
                        totalAmount += (Double) value;
                    } else {
                        cellText = value.toString();
                    }
                }

                PdfPCell cell = new PdfPCell(new Phrase(cellText, dataFont));
                cell.setPadding(5);

                // Align numbers to the right
                if (col == 0 || col == 1 || col == 3) { // IDs and Amount
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                } else if (col == 2 || col == 4) { // Date/Time and Payment Method
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                } else {
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                }

                pdfTable.addCell(cell);
            }
        }

        document.add(pdfTable);

        // Add summary section
        document.add(new Paragraph("\n"));
        Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);

        Paragraph recordCount = new Paragraph("Total Records: " + tableModel.getRowCount(), summaryFont);
        recordCount.setAlignment(Element.ALIGN_LEFT);
        document.add(recordCount);

        Paragraph totalSales = new Paragraph(String.format("Total Sales Amount: $%.2f", totalAmount), summaryFont);
        totalSales.setAlignment(Element.ALIGN_LEFT);
        document.add(totalSales);

        document.close();
    }

    /**
     * Export sales data to PDF format (using DefaultTableModel - legacy method)
     */
    public static void exportSalesToPDF(javax.swing.table.DefaultTableModel tableModel, String filePath) throws Exception {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("Sales Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Add generation date
        Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        Paragraph date = new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dateFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        date.setSpacingAfter(20);
        document.add(date);

        // Create table
        PdfPTable pdfTable = new PdfPTable(tableModel.getColumnCount());
        pdfTable.setWidthPercentage(100);

        // Add headers
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            PdfPCell cell = new PdfPCell(new Phrase(tableModel.getColumnName(i), headerFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            pdfTable.addCell(cell);
        }

        // Add data rows
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                Object value = tableModel.getValueAt(row, col);
                PdfPCell cell = new PdfPCell(new Phrase(value != null ? value.toString() : "", dataFont));
                cell.setPadding(5);

                // Align numbers to the right
                if (col == 0 || col == 3) { // Sale ID and Total Amount
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                } else {
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                }

                pdfTable.addCell(cell);
            }
        }

        document.add(pdfTable);

        // Add summary
        document.add(new Paragraph("\n"));
        Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Paragraph summary = new Paragraph("Total Records: " + tableModel.getRowCount(), summaryFont);
        summary.setAlignment(Element.ALIGN_RIGHT);
        document.add(summary);

        document.close();
    }

    /**
     * Export sales data to CSV format
     */
    public static void exportSalesToCSV(javax.swing.table.DefaultTableModel tableModel, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write headers
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                writer.append(escapeCSV(tableModel.getColumnName(i)));
                if (i < tableModel.getColumnCount() - 1) {
                    writer.append(",");
                }
            }
            writer.append("\n");

            // Write data rows
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object value = tableModel.getValueAt(row, col);
                    writer.append(escapeCSV(value != null ? value.toString() : ""));
                    if (col < tableModel.getColumnCount() - 1) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }

            writer.flush();
        }
    }

    /**
     * Escape special characters in CSV format
     */
    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }
}