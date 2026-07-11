package hr.algebra.ledvision.security;

import hr.algebra.ledvision.model.User;
import hr.algebra.ledvision.repository.RefreshTokenRepository;
import hr.algebra.ledvision.repository.UserRepository;
import hr.algebra.ledvision.model.RefreshToken;
import hr.algebra.ledvision.utils.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieHelper cookieHelper;

    @Value("${jwt.expiration.refresh}")
    private Long refreshExpiration;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        String username = authentication.getName();



        User user = userRepository.findByUsername(username)
                .orElseThrow();

        refreshTokenRepository.revokeAllUserTokens(user);

        String tokenString = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(tokenString);
        refreshToken.setExpiresAt(
                LocalDateTime.now().plusSeconds(refreshExpiration / 1000)
        );
        refreshTokenRepository.save(refreshToken);

        cookieHelper.setRefreshTokenCookie(response, tokenString);

        response.sendRedirect("/");
    }
}