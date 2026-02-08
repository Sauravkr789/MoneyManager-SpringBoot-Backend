package com.example.expanse_tracker.controller;

import com.example.expanse_tracker.dto.IncomeDTO;
import com.example.expanse_tracker.service.IncomeExcelService;
import com.example.expanse_tracker.service.IncomeService;
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
public class IncomeExcelController {

    private final IncomeExcelService incomeExcelService;
    private final IncomeService incomeService;

    // ============================
    // 📥 DOWNLOAD EXCEL
    // ============================
    @GetMapping("/download/income")
    public ResponseEntity<Resource> downloadIncomeExcel() {

        List<IncomeDTO> incomes =
                incomeService.getCurrentMonthIncomesForCurrentUser();

        byte[] excelBytes =
                incomeExcelService.exportIncomeToExcel(incomes);

        Resource resource = new ByteArrayResource(excelBytes);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=income_details.xlsx"
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
