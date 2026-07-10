package hr.algebra.talaria.service;

import hr.algebra.talaria.dto.AuthResponse;
import hr.algebra.talaria.model.RefreshToken;
import hr.algebra.talaria.model.User;
import hr.algebra.talaria.repository.RefreshTokenRepository;
import hr.algebra.talaria.repository.UserRepository;
import hr.algebra.talaria.security.CustomUserDetails;
import hr.algebra.talaria.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration.refresh}")
    private Long refreshExpiration;

    public void register(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists!");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));

        userRepository.save(user);
    }

    @Transactional
    public AuthResponse login(String username, String password) {
       Authentication authentication =  authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();


        refreshTokenRepository.revokeAllUserTokens(user);

        String accessToken = jwtService.generateAccessToken(
                user.getUsername(),
                user.getRole().name(),
                user.getId()
        );
        String refreshToken = generateAndSaveRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(refreshTokenString)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found!"));

        if (!refreshToken.isValid()) {
            if (refreshToken.isUsed()) {
                refreshTokenRepository.revokeAllUserTokens(refreshToken.getUser());
            }
            throw new IllegalArgumentException("Refresh token not valid!");
        }

        refreshToken.setUsed(true);
        refreshTokenRepository.save(refreshToken);

        User user = refreshToken.getUser();

        String newAccessToken = jwtService.generateAccessToken(
                user.getUsername(),
                user.getRole().name(),
                user.getId()
        );
        String newRefreshToken = generateAndSaveRefreshToken(user);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshTokenString) {
        refreshTokenRepository.findByToken(refreshTokenString)
                .ifPresent(token ->
                        refreshTokenRepository.revokeAllUserTokens(token.getUser())
                );
    }

    private String generateAndSaveRefreshToken(User user) {
        String tokenString = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(tokenString);
        refreshToken.setExpiresAt(
                LocalDateTime.now().plusSeconds(refreshExpiration / 1000)
        );

        refreshTokenRepository.save(refreshToken);
        return tokenString;
    }
}