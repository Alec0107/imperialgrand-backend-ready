package com.imperialgrand.backend.Menu.MenuItem;

import com.imperialgrand.backend.Menu.entities.MenuItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping("/items")
    public Page<MenuItem> fetchCatSub(
            @RequestParam Long categoryId,
            @RequestParam(required = false) Long subcategoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size

    ){
        return menuItemService.menuItemLists(categoryId, subcategoryId, page, size);
    }


}
