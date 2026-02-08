package com.example.expanse_tracker.service;

import com.example.expanse_tracker.dto.ExpenseDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


@Service
public class ExpenseExcelService {

    public byte[] exportExpenseToExcel(List<ExpenseDTO> expenses) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Expense Details");

            // ===== Header Style =====
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // ===== Header Row =====
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "S.No",
                    "Name",
                    "Category",
                    "Amount",
                    "Date"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ===== Data Rows =====
            int rowIndex = 1;
            int serialNo = 1;

            for (ExpenseDTO expense : expenses) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(serialNo++);
                row.createCell(1).setCellValue(expense.getName());
                row.createCell(2).setCellValue(expense.getCategoryName());
                row.createCell(3).setCellValue(
                        expense.getAmount() != null
                                ? expense.getAmount().doubleValue()
                                : 0
                );
                row.createCell(4).setCellValue(
                        expense.getDate() != null
                                ? expense.getDate().toString()
                                : ""
                );
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate expense Excel file", e);
        }
    }
}
