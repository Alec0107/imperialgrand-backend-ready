package com.imperialgrand.backend.Menu;

import com.imperialgrand.backend.Menu.entities.CategoryDTO;
import com.imperialgrand.backend.Menu.entities.CategoryTreeDTO;
import com.imperialgrand.backend.Menu.entities.SubcategoryDTO;
import com.imperialgrand.backend.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;


    @GetMapping("/get_all_cats")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {

        List<CategoryDTO> cats = categoryService.getAllCategories();
        System.out.println("Categories:\n");
        for(CategoryDTO cat : cats) {
            System.out.println(cat.toString());
        }

        return ResponseEntity.ok(new ApiResponse<>(cats, "All categories are fetched successfully."));

    }

    @GetMapping("/fetch_category")
    public ResponseEntity<List<CategoryTreeDTO>> fetchCategoryTree(){
        List<CategoryTreeDTO> cat = categoryService.retrieveCategoriesAndSubCategories();
        System.out.println("Returning Category Tree:\n");

        for(CategoryTreeDTO catTree : cat) {
            System.out.println(catTree.getCatId());
            System.out.println(catTree.getCatName());
            System.out.println(catTree.getCatSlug());

            for(SubcategoryDTO s: catTree.getSubcategories()) {
                System.out.println(s.toString());
            }


        }

        System.out.println("FINISH\n");
        return ResponseEntity.ok(cat);
    }


}
