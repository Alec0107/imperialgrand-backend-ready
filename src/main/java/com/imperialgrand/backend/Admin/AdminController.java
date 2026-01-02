package com.imperialgrand.backend.Admin;

import com.imperialgrand.backend.Menu.MenuItem.*;
import com.imperialgrand.backend.Menu.repo.CategoryRepository;
import com.imperialgrand.backend.Menu.repo.SubcategoryRepository;
import com.imperialgrand.backend.authentication.DTO.records.CustomerDTO;
import com.imperialgrand.backend.authentication.DTO.records.UserLoginRequest;
import com.imperialgrand.backend.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
public class AdminController {

    private final Logger logger = Logger.getLogger(AdminController.class.getName());
    private final AdminService service;
    private final MenuItemService menuItemService;

    private final CategoryRepository categoryRepo;
    private final SubcategoryRepository subcategoryRepo;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody UserLoginRequest loginReq, HttpServletRequest servlet){

        logger.info("ENTERING ADMIN CONTROLLER");
        logger.info(loginReq.email() + ":" + loginReq.password());

        Map<String, Object> mapBody = service.login(loginReq.email(), loginReq.password(), servlet.getHeader("x-device-id"));
        String accessToken  = (String) mapBody.get("access_token");
        String refreshToken = (String) mapBody.get("refresh_token");
        String deviceId = (String) mapBody.get("device_id");


        ResponseCookie refresh = ResponseCookie.from("refresh-token", refreshToken)
                .httpOnly(true)
                .secure(false) // true for production, false for dev
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        ResponseCookie access = ResponseCookie.from("access-token", accessToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(10))
                .build();

        ResponseCookie device = ResponseCookie.from("device-id", deviceId)
                .httpOnly(false)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        Map<String, Object> mapBodyResponse = new HashMap<>();
        mapBodyResponse.put("username", mapBody.get("username"));
        mapBodyResponse.put("role", mapBody.get("role"));

        logger.info(String.format("\n\nLogin response sending...\n Refresh-Token:%s\nAccess-Token:%s\nDevice-Id:%s", refreshToken, accessToken, deviceId));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refresh.toString(), access.toString(), device.toString())
                .body(new ApiResponse<>(mapBodyResponse, "User logged in successfully!"));
    }

    // Only admins can access
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers")
    public List<CustomerDTO> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.fetchCustomers(page, size);
    }



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reservations")
    public Page<ReservationListItemDTO> list(
            @RequestParam(defaultValue = "CONFIRMED") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        return service.findByStatus(status, pageable);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reservations/{id}")
    public ReservationDetailsAdminDTO getReservationDetails(@PathVariable Integer id) {
        return service.getDetails(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelReservation(@PathVariable Integer id) {
        service.cancel(id);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Reservation cancelled successfully"));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/menu-items")
    public Page<MenuItemRow> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return menuItemService.findRows(PageRequest.of(page, size));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/menu-items/{id}")
    public MenuItemAdminView getOne(@PathVariable long id) {
        return menuItemService.getAdminView(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/menu-categories")
    public List<SimpleIdName> categories() {
        return categoryRepo.findAllOrderByDisplayOrder()
                .stream().map(c -> new SimpleIdName(c.getId(), c.getName())).toList();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/menu-subcategories")
    public List<SimpleIdName> subcats(@RequestParam("cat") long categoryId) {
        return subcategoryRepo.findByCategoryIdOrderByDisplayOrder(categoryId)
                .stream().map(s -> new SimpleIdName(s.getId(), s.getName())).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("menu-item-update/{id}")
    public MenuItemAdminView update(
            @PathVariable long id,
            @RequestBody UpdateMenuItemReq req
    ) {
        return menuItemService.update(id, req);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/menu-item-create")
    public MenuItemAdminView create(@RequestBody CreateMenuItemReq req) {
        return menuItemService.create(req);
    }

}



