package com.example.expanse_tracker.service;

import com.example.expanse_tracker.dto.CategoryDTO;
import com.example.expanse_tracker.entity.CategoryEntity;
import com.example.expanse_tracker.entity.ProfileEntity;
import com.example.expanse_tracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;


    public CategoryDTO saveCategory(CategoryDTO categoryDTO)
    {
        ProfileEntity profile= profileService.getCurrentProfile();
        if(categoryRepository.existsByNameAndProfileId(categoryDTO.getName(),profile.getId()))
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Category with the same name already exists.");
        }

        CategoryEntity newCategory= toEntity(categoryDTO,profile);
        newCategory= categoryRepository.save(newCategory);
        return toDTO(newCategory);
    }

    //get categories for the current User
    public List<CategoryDTO> getCategoryForCurrentUser()
    {
        ProfileEntity profile= profileService.getCurrentProfile();
        List<CategoryEntity> categories= categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::toDTO).toList();
    }

    //helper method
    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile)
    {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .type(categoryDTO.getType())
                .profile(profile)
                .build();
    }

    private CategoryDTO toDTO(CategoryEntity entity)
    {
        return CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .type(entity.getType())
                .profileId(entity.getProfile()!=null?entity.getProfile().getId():null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    //get categories by type for current user
    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type)
    {
        ProfileEntity profile= profileService.getCurrentProfile();
        List<CategoryEntity> entities= categoryRepository.findByTypeAndProfileId(type,profile.getId());
        return entities.stream().map(this::toDTO).toList();
    }

    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found."));

        if (!existingCategory.getName().equals(categoryDTO.getName()) &&
                categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), profile.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with the same name already exists.");
        }

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setIcon(categoryDTO.getIcon());
        existingCategory.setType(categoryDTO.getType());

        CategoryEntity updatedCategory = categoryRepository.save(existingCategory);
        return toDTO(updatedCategory);
    }
}
