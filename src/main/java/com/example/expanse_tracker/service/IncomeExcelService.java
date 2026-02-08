package com.example.expanse_tracker.service;

import com.example.expanse_tracker.dto.IncomeDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


@Service
public class IncomeExcelService {

    public byte[] exportIncomeToExcel(List<IncomeDTO> incomes) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Income Details");

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

            for (IncomeDTO income : incomes) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(serialNo++);
                row.createCell(1).setCellValue(income.getName());
                row.createCell(2).setCellValue(income.getCategoryName());
                row.createCell(3).setCellValue(
                        income.getAmount() != null
                                ? income.getAmount().doubleValue()
                                : 0
                );
                row.createCell(4).setCellValue(
                        income.getDate() != null
                                ? income.getDate().toString()
                                : ""
                );
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate income Excel file", e);
        }
    }
}
