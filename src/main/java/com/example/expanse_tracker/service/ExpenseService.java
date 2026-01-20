package com.example.expanse_tracker.service;

import com.example.expanse_tracker.dto.ExpenseDTO;
import com.example.expanse_tracker.entity.CategoryEntity;
import com.example.expanse_tracker.entity.ExpenseEntity;
import com.example.expanse_tracker.entity.ProfileEntity;
import com.example.expanse_tracker.repository.CategoryRepository;
import com.example.expanse_tracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public  class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    public ExpenseDTO addExpense(ExpenseDTO dto)
    {
        ProfileEntity profile= profileService.getCurrentProfile();

        CategoryEntity category= categoryRepository.findById(Long.parseLong(dto.getCategoryId()))
                .orElseThrow(()-> new RuntimeException("Category not found"));
        ExpenseEntity expenseEntity= toEntity(dto,profile,category);
        ExpenseEntity savedEntity= expenseRepository.save(expenseEntity);
        return toDTO(savedEntity);
    }

    //Retrieve all expenses for current month/based on startDate and endDate
    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser()
    {
        ProfileEntity profile= profileService.getCurrentProfile();
        LocalDate now= LocalDate.now();
        LocalDate startDate= now.withDayOfMonth(1);
        LocalDate endDate= now.withDayOfMonth(now.lengthOfMonth());

        List<ExpenseEntity> expenses= expenseRepository
                .findByProfileIdAndDateBetween(profile.getId(),startDate,endDate);

        return expenses.stream().map(this::toDTO).toList();
    }

    //Delete Expense
    public void deleteExpense(Long expenseId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized to delete this expense");
        }
        expenseRepository.delete(expense);
    }

    //Get latest 5 expenses
    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> expenses = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return expenses.stream().map(this::toDTO).toList();
    }

    //Get total expenses for current user
    public BigDecimal getTotalExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal totalExpenses = expenseRepository.findTotalExpensesByProfileId(profile.getId());
        return totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
    }

    //filter expenses
    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort)
    {
        ProfileEntity profile=profileService.getCurrentProfile();
        List<ExpenseEntity> expenses= expenseRepository
                .findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                        profile.getId(),
                        startDate,
                        endDate,
                        keyword,
                        sort
                );
        return expenses.stream().map(this::toDTO).toList();
    }

    //Notification
    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId, LocalDate date) {
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDate(profileId, date);
        return expenses.stream().map(this::toDTO).toList();
    }

    //helper methods
    private ExpenseEntity toEntity(ExpenseDTO dto, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private ExpenseDTO toDTO(ExpenseEntity entity) {
        return ExpenseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .amount(entity.getAmount())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .categoryId(entity.getCategory().getId().toString())
                .categoryName(entity.getCategory().getName())
                .build();
    }
}
