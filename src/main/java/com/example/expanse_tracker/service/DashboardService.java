package com.example.expanse_tracker.service;

import com.example.expanse_tracker.dto.ExpenseDTO;
import com.example.expanse_tracker.dto.IncomeDTO;
import com.example.expanse_tracker.dto.RecentTransactionDTO;
import com.example.expanse_tracker.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final ProfileService profileService;

    public Map<String,Object> getDashboardData()
    {
        Map<String,Object> returnValue = new LinkedHashMap<>();
        ProfileEntity profile = profileService.getCurrentProfile();

        List<IncomeDTO> lastestIncomes = incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDTO> lastestExpenses = expenseService.getLatest5ExpensesForCurrentUser();

        List<RecentTransactionDTO> recentTransactions= Stream.concat(lastestIncomes.stream().map(income->
                RecentTransactionDTO.builder()
                        .id(income.getId())
                        .profileId(profile.getId())
                        .icon(income.getIcon())
                        .name(income.getName())
                        .date(income.getDate())
                        .amount(income.getAmount())
                        .type("income")
                        .createdAt(income.getCreatedAt())
                        .updatedAt(income.getUpdatedAt())
                        .build()
        ),lastestExpenses.stream().map(expense->
                RecentTransactionDTO.builder()
                        .id(expense.getId())
                        .profileId(profile.getId())
                        .icon(expense.getIcon())
                        .name(expense.getName())
                        .date(expense.getDate())
                        .amount(expense.getAmount())
                        .type("expense")
                        .createdAt(expense.getCreatedAt())
                        .updatedAt(expense.getUpdatedAt())
                        .build()
        ))
                .sorted((a,b)-> {
                    int cmp=b.getDate().compareTo(a.getDate());
                    if(cmp==0 && a.getCreatedAt()!=null && b.getCreatedAt()!=null){
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return cmp;
                }).collect(Collectors.toList());

        returnValue.put("totalBalance", incomeService.getTotalIncomesForCurrentUser().subtract(expenseService.getTotalExpensesForCurrentUser()));
        returnValue.put("totalIncome", incomeService.getTotalIncomesForCurrentUser());
        returnValue.put("totalExpense", expenseService.getTotalExpensesForCurrentUser());
        returnValue.put("recent5Incomes", lastestIncomes);
        returnValue.put("recent5Expenses", lastestExpenses);
        returnValue.put("recentTransactions", recentTransactions);
        return returnValue;


    }
}
