package hr.algebra.talaria.controller.api;

import hr.algebra.talaria.dto.AuthResponse;
import hr.algebra.talaria.service.AuthService;
import hr.algebra.talaria.utils.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieHelper cookieHelper;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response) {
        try {
            AuthResponse authResponse = authService.login(username, password);
            cookieHelper.setRefreshTokenCookie(response, authResponse.getRefreshToken());
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request,
                                                HttpServletResponse response) {
        String refreshToken = cookieHelper.getRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            AuthResponse authResponse = authService.refresh(refreshToken);
            cookieHelper.setRefreshTokenCookie(response, authResponse.getRefreshToken());
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request,
                                         HttpServletResponse response) {
        String refreshToken = cookieHelper.getRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        cookieHelper.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok("Logout successful!");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password) {
        try {
            authService.register(username, email, password);
            return ResponseEntity.ok("Registration Successful!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}