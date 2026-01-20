package com.example.expanse_tracker.controller;

import com.example.expanse_tracker.dto.ExpenseDTO;
import com.example.expanse_tracker.dto.FilterDTO;
import com.example.expanse_tracker.dto.IncomeDTO;
import com.example.expanse_tracker.service.ExpenseService;
import com.example.expanse_tracker.service.IncomeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {
    private final IncomeService incomeService;
    private final ExpenseService expenseService;


    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDTO filterDTO) {
        LocalDate startDate = filterDTO.getStartDate()!=null?filterDTO.getStartDate():LocalDate.MIN;
        LocalDate endDate = filterDTO.getEndDate()!=null?filterDTO.getEndDate():LocalDate.now();

        String keyword = filterDTO.getKeyword()!=null?filterDTO.getKeyword():"";
        String sortField = filterDTO.getSortField()!=null?filterDTO.getSortField():"date";
        Sort.Direction direction="desc".equalsIgnoreCase(filterDTO.getSortOrder())? Sort.Direction.DESC: Sort.Direction.ASC;

        Sort sort=Sort.by(direction,sortField);

        if("income".equalsIgnoreCase(filterDTO.getType())) {
            List<IncomeDTO> incomes=incomeService.filterIncomes(startDate,endDate,keyword,sort);
            return ResponseEntity.ok(incomes);
        } else if("expense".equalsIgnoreCase(filterDTO.getType())) {
            List<ExpenseDTO> expenses=expenseService.filterExpenses(startDate,endDate,keyword,sort);
            return ResponseEntity.ok(expenses);
        }
        else {
            return ResponseEntity.badRequest().body("Invalid type. Must be 'income' or 'expense'.");
        }
    }

}
