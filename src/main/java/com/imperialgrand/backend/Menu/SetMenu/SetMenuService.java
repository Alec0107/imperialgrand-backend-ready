package com.imperialgrand.backend.Menu.SetMenu;

import com.imperialgrand.backend.Menu.dto.CNYMenuResponseDTO;
import com.imperialgrand.backend.Menu.dto.MenuOptionDTO;
import com.imperialgrand.backend.Menu.entities.CNYMenu;
import com.imperialgrand.backend.Menu.entities.MenuOption;
import com.imperialgrand.backend.Menu.entities.SetMenu;
import com.imperialgrand.backend.Menu.repo.CNYMenuRepository;
import com.imperialgrand.backend.Menu.repo.SetMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SetMenuService {
    private final SetMenuRepository setMenuRepo;
    private final CNYMenuRepository cnyMenuRepository;

    public Page<SetMenu> list(int page, int size) {
        return setMenuRepo.findAllByIsActiveTrueOrderByDisplayOrderAsc(PageRequest.of(page, size));
    }


    // Chinese set menu
    public Page<CNYMenuResponseDTO> fetchCnyMenus(int page, int size){

        Pageable pageable = PageRequest.of(page, size);

        Page<CNYMenu> menus =
                cnyMenuRepository.findByCategoryAndIsActiveTrue("CNY", pageable);


        return menus.map(menu -> {
            menu.getOptions().size();
            return toDTO(menu);
        });
    }

    private CNYMenuResponseDTO toDTO(CNYMenu menu) {

        List<MenuOptionDTO> options = menu.getOptions()
                .stream()
                .filter(MenuOption::isActive)
                .map(this::toOptionDTO)
                .toList();

        return new CNYMenuResponseDTO(
                menu.getId(),
                menu.getCategory(),
                menu.getNameEn(),
                menu.getNameCn(),
                menu.getImageUrl(),
                options,
                menu.getCoursesJson()
        );
    }

    private MenuOptionDTO toOptionDTO(MenuOption option) {
        return new MenuOptionDTO(
                option.getId(),
                option.getPricingModel(),
                option.getPax(),
                option.getMinPax(),
                option.getPriceCents(),
                option.getPriceSuffix()
        );
    }
}
