package com.imperialgrand.backend.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imperialgrand.backend.jwt.exception.InvalidJwtTokenException;
import com.imperialgrand.backend.jwt.JwtGeneratorService;
import com.imperialgrand.backend.user.exception.EmailNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JwtGeneratorService jwtService;
    private final Logger logger = Logger.getLogger(JWTAuthenticationFilter.class.getName());

    private static final Set<String> OPTIONAL_AUTH_ENDPOINTS = Set.of(
            "/api/v1/reservation/submit"
    );

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/verify",
            "/api/v1/auth/resend-verification",
            "/api/v1/auth/inbox-resend-verification",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/reset-password/",
            "/api/v1/auth/reset-password/validate",
            "/api/v1/contact",
            "/api/menu/categories/get_all_cats",
            "/api/menu/categories/fetch_category"
            ,"/api/menu/set-menu/fetch_set_menu",
            "/api/menu/items",
            "/api/menu/fetch-item",

            // Reservation public endpoints...
            "/api/v1/reservation/availability",

            // NEW ENDPOINTS FOR SCHOOL PROJ IMPERIAL GRAND
            // 1. Authentication public endpoints
            "/api/auth/signup",
            // 2. To verify user account by sending otp
            "/api/auth/verify",
            // 3. To resend otp
            "/api/auth/resend-otp",
            // 4. Login
            "/api/auth/login",
            // 5. Refresh token
            "/api/auth/refresh-token",
            // FOR RESERVATIONS
            "/api/reservation/availability",
            "/api/reservation/status",
            "/api/reservation/guest/submit",

            // FOR ADMIN PUBLIC
            "/api/auth/admin/login"

    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        // For checking endpoint whether to decide to skip it or not
        String path = request.getServletPath();
        String authIntent = request.getHeader("x-auth-intent");
        logger.info("\nENDPOINT: " + path);
        if(authIntent != null) {
            logger.info("AUTH INTENT: " + authIntent);
        }

        // ================== ADD THIS BLOCK (right here) ==================
        // Let SockJS/STOMP handshake URLs bypass this JWT filter
        if (path.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }
        // (Optional but useful) let CORS preflight pass
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        // ================================================================


        /**
         * Step 0: Skip public endpoints
         * */
        if(isPublicEndpoint(path)) {
            logger.info("Public endpoint hit: " + path);
            filterChain.doFilter(request, response);
            return;
        }

//        if(path.equals("/api/v1/auth/refresh-token") || path.equals("/api/v1/auth/login")
//                || path.equals("/api/v1/auth/register") || path.equals("/api/v1/auth/verify")
//                || path.equals("/api/v1/auth/resend-verification") || path.equals("/api/v1/auth/inbox-resend-verification") || path.equals("/api/v1/auth/verify-email")
//                || path.equals("/api/v1/auth/forgot-password") || path.equals("/api/v1/auth/reset-password") || path.equals("/api/v1/auth/reset-password/")
//                || path.equals("/api/v1/auth/reset-password/validate") || path.equals(("/api/v1/contact"))){
//            logger.info("Endpoints skipped");
//            filterChain.doFilter(request, response);
//            return;
//        }

        // Extract token from HttpOnly cookie
        /**
         * Step 1: Get access token from cookie
         * */
        String accessJwtToken = null;
        if(request.getCookies() != null){
            for(Cookie cookie : request.getCookies()){
                if(cookie.getName().equals("access-token")){
                    accessJwtToken = cookie.getValue();
                    logger.info("ACCESS TOKEN: " + accessJwtToken);
                    break;
                }else{
                    logger.info(cookie.getName());
                }
            }
        }

//        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//            System.out.println("Accessing public endpoints....");
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        jwtToken = authorizationHeader.substring(7);

//        if(accessJwtToken == null){
//            logger.info("No access token found");
//            exceptionSendBuilder("Access token is missing or expired.", response);
//            return;
//        }
        try{
            /**
             * Step 2: If access token exists -> validate and set context
             * */
            if(accessJwtToken != null){
                if(SecurityContextHolder.getContext().getAuthentication() == null) {
                    logger.info("Access token found...");
                    // load userEmail from db
                    UserDetails userDetails = jwtService.validateAccessToken(accessJwtToken);
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }else if(OPTIONAL_AUTH_ENDPOINTS.contains(path)){
                logger.info("Accessing optional endpoint...");
                /**
                 * Step 3: Optional id access token doesn't exist only guest are allowed to save reservation
                 * */
                if(authIntent.equals("guest")){
                    logger.info("Guest access allowed for endpoint: " + path);
                }else{
                    logger.info("User endpoint hit: " + path);
                    exceptionSendBuilder("Access token is missing or expired.", response);
                    return;
                }
            }else{
                /**
                 * Step 4: Protected private endpoints
                 * */
                logger.info("No access token found");
                exceptionSendBuilder("Access token is missing or expired.", response);
                return;
            }

            /**
             * Step 5: Continue to controller
             * */
            filterChain.doFilter(request, response);
        }catch (InvalidJwtTokenException ex){
            exceptionSendBuilder(ex.getMessage(), response);
        }catch(EmailNotFoundException ex) {
            exceptionSendBuilder(ex.getMessage(), response);
        } catch (Exception ex) {
            exceptionSendBuilder(ex.getMessage(), response);
        }
    }

    private void exceptionSendBuilder(String exceptionMessage, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        map.put("message", exceptionMessage);
        map.put("status", HttpStatus.UNAUTHORIZED.value());
        String jsonErrorResponse = objectMapper.writeValueAsString(map);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(jsonErrorResponse);
    }

    public boolean isPublicEndpoint(String endpoint) {
        return PUBLIC_ENDPOINTS.contains(endpoint);
    }

}
