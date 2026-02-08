package com.example.expanse_tracker.controller;

import com.example.expanse_tracker.dto.ExpenseDTO;
import com.example.expanse_tracker.dto.IncomeDTO;
import com.example.expanse_tracker.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final IncomeExcelService incomeExcelService;
    private final ExpenseExcelService expenseExcelService;
    private final EmailService emailService;
    private final ProfileService profileService;

    @GetMapping("/income-excel")
    public ResponseEntity<String> emailIncomeExcel() {

        var profile = profileService.getCurrentProfile();

        List<IncomeDTO> incomes =
                incomeService.getCurrentMonthIncomesForCurrentUser();

        byte[] excelBytes =
                incomeExcelService.exportIncomeToExcel(incomes);

        emailService.sendEmailWithAttachment(
                profile.getEmail(),
                "Your Income Excel Report",
                "<p>Please find attached your income report.</p>",
                excelBytes,
                "income_details.xlsx"
        );

        return ResponseEntity.ok("Income report emailed successfully");
    }

    @GetMapping("/expense-excel")
    public ResponseEntity<String> emailExpenseExcel() {

        var profile = profileService.getCurrentProfile();

        List<ExpenseDTO> expenses =
                expenseService.getCurrentMonthExpensesForCurrentUser();

        byte[] excelBytes =
                expenseExcelService.exportExpenseToExcel(expenses);

        emailService.sendEmailWithAttachment(
                profile.getEmail(),
                "Your Expense Excel Report",
                "<p>Please find attached your expense report.</p>",
                excelBytes,
                "expense_details.xlsx"
        );

        return ResponseEntity.ok("Expense report emailed successfully");
    }
}
