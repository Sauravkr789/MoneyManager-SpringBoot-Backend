package com.example.expanse_tracker.controller;

import com.example.expanse_tracker.dto.ExpenseDTO;
import com.example.expanse_tracker.service.ExpenseExcelService;
import com.example.expanse_tracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/excel")
public class ExpenseExcelController {

    private final ExpenseExcelService expenseExcelService;
    private final ExpenseService expenseService;

    // ============================
    // 📥 DOWNLOAD EXCEL
    // ============================
    @GetMapping("/download/expense")
    public ResponseEntity<Resource> downloadIncomeExcel() {

        List<ExpenseDTO> expenses =
                expenseService.getCurrentMonthExpensesForCurrentUser();

        byte[] excelBytes =
                expenseExcelService.exportExpenseToExcel(expenses);

        Resource resource = new ByteArrayResource(excelBytes);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=expense_details.xlsx"
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .contentLength(excelBytes.length)
                .body(resource);
    }
}
