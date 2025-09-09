package com.imperialgrand.backend.Menu;

import com.imperialgrand.backend.Menu.entities.CategoryDTO;
import com.imperialgrand.backend.Menu.entities.CategoryTreeDTO;
import com.imperialgrand.backend.Menu.entities.SubcategoryDTO;
import com.imperialgrand.backend.Menu.repo.CategoryRepository;
import com.imperialgrand.backend.Menu.repo.SubcategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;


    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    public List<CategoryTreeDTO> retrieveCategoriesAndSubCategories() {
        // 1. Fetch all categories
        List<CategoryDTO> categories = categoryRepository.findAllByOrderByDisplayOrderAsc();

        // 2. Take all category ids and put in a list
        List <Long> catIds = new ArrayList<>();
        for (CategoryDTO category : categories) {
            catIds.add(category.getId());
        }

        // 3. Fetch all Sub-Category using Category IDs
        List<SubcategoryDTO> subs = subcategoryRepository.findByCategoryIdInOrderByDisplayOrderAsc(catIds);

        // 4. Index subs by categoryId
        Map<Long, List<SubcategoryDTO>> subByCat = new HashMap<>();
        for(SubcategoryDTO sub : subs){
            // Get category id
            Long catId = sub.getCategoryId();

            // add the category id in the map as key if not yet added and make a ney list
            subByCat.putIfAbsent(catId, new ArrayList<>());

            // add the subs based on its category id
            subByCat.get(catId).add(sub);
        }

        // 5. Build Category Tree
        List<CategoryTreeDTO> result = new ArrayList<>();

        for (CategoryDTO c: categories) {
            // Create category DTO
            CategoryTreeDTO catNode = new CategoryTreeDTO(c.getId(), c.getName(), c.getSlug());

            // get subcategories for the current category /otherwise use an empty list
            List<SubcategoryDTO> subForCat = subByCat.getOrDefault(c.getId(), new ArrayList<>());

            // convert each sub into SubcategoryDTO and add
            for (SubcategoryDTO sub : subForCat) {
                SubcategoryDTO subNode = new SubcategoryDTO(
                        sub.getId(),
                        sub.getName(),
                        sub.getSlug(),
                        sub.getDisplayOrder()
                );

                catNode.getSubcategories().add(subNode);
            }

            result.add(catNode);
        }
        return result;
    }

}
