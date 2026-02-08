package com.example.expanse_tracker.service;

import com.example.expanse_tracker.dto.IncomeDTO;
import com.example.expanse_tracker.entity.CategoryEntity;
import com.example.expanse_tracker.entity.IncomeEntity;
import com.example.expanse_tracker.entity.ProfileEntity;

import com.example.expanse_tracker.repository.CategoryRepository;
import com.example.expanse_tracker.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;

    public IncomeDTO addIncome(IncomeDTO dto)
    {
        ProfileEntity profile= profileService.getCurrentProfile();

        CategoryEntity category= categoryRepository.findById(Long.parseLong(dto.getCategoryId()))
                .orElseThrow(()-> new RuntimeException("Category not found"));
        IncomeEntity expenseEntity= toEntity(dto,profile,category);
        IncomeEntity savedEntity= incomeRepository.save(expenseEntity);
        return toDTO(savedEntity);
    }

    //Retrieve all Incomes for current month/based on startDate and endDate
    public List<IncomeDTO> getCurrentMonthIncomesForCurrentUser()
    {
        ProfileEntity profile= profileService.getCurrentProfile();
        LocalDate now= LocalDate.now();
        LocalDate startDate= now.withDayOfMonth(1);
        LocalDate endDate= now.withDayOfMonth(now.lengthOfMonth());

        List<IncomeEntity> expenses= incomeRepository
                .findByProfileIdAndDateBetween(profile.getId(),startDate,endDate);

        return expenses.stream().map(this::toDTO).toList();
    }

    //Delete Expense
    public void deleteIncome(Long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new RuntimeException("Income not found"));

        if (!income.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized to delete this income");
        }
        incomeRepository.delete(income);
    }

    //Get latest 5 expenses
    public List<IncomeDTO> getLatest5IncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> incomes = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return incomes.stream().map(this::toDTO).toList();
    }

    //Get total expenses for current user
    public BigDecimal getTotalIncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal totalIncomes = incomeRepository.findTotalIncomesByProfileId(profile.getId());
        return totalIncomes != null ? totalIncomes : BigDecimal.ZERO;
    }

    //filter incomes
    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort)
    {
        ProfileEntity profile=profileService.getCurrentProfile();
        List<IncomeEntity> incomes= incomeRepository
                .findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                        profile.getId(),
                        startDate,
                        endDate,
                        keyword,
                        sort
                );
        return incomes.stream().map(this::toDTO).toList();
    }

    private IncomeEntity toEntity(IncomeDTO dto, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private IncomeDTO toDTO(IncomeEntity entity) {
        return IncomeDTO.builder()
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
