package com.imperialgrand.backend.Menu.MenuItem;

import com.imperialgrand.backend.Menu.entities.MenuItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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


//    @GetMapping("/fetch/signature")


    @GetMapping("/fetch-item")
    public ResponseEntity<MenuItem> fetchProductById(@RequestParam Long id){
        MenuItem item = menuItemService.getMenuItemById(id);
        System.out.println("Product Id: " + id);

        return ResponseEntity.ok(item);
    }






}
