package com.imperialgrand.backend.Menu.MenuItem;

import com.imperialgrand.backend.Menu.entities.MenuItem;
import com.imperialgrand.backend.Menu.repo.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    public Page<MenuItem> menuItemLists(Long categoryId, Long subcategoryId, int page, int size) {
        var pageable = PageRequest.of(page, size);

        if(subcategoryId != null) {
            return menuItemRepository.findByCategoryIdAndSubcategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId, subcategoryId, pageable);
        }

        return menuItemRepository.findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId, pageable);
    }
}
