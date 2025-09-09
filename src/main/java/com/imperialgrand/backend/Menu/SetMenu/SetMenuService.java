package com.imperialgrand.backend.Menu.SetMenu;

import com.imperialgrand.backend.Menu.entities.SetMenu;
import com.imperialgrand.backend.Menu.repo.SetMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SetMenuService {
    private final SetMenuRepository setMenuRepo;

    public Page<SetMenu> list(int page, int size) {
        return setMenuRepo.findAllByIsActiveTrueOrderByDisplayOrderAsc(PageRequest.of(page, size));
    }
}
