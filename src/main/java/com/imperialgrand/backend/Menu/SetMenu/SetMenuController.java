package com.imperialgrand.backend.Menu.SetMenu;

import com.imperialgrand.backend.Menu.entities.SetMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu/set-menu")
@RequiredArgsConstructor
public class SetMenuController {
    private final SetMenuService setMenuService;

    @GetMapping("/fetch_set_menu")
    public Page<SetMenu> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ){
        return setMenuService.list(page, size);
    }
}

