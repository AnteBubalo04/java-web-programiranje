package hr.algebra.talaria.listener;

import hr.algebra.talaria.service.AsyncService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener
        implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

    private final AsyncService asyncService;

    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) return;

        HttpServletRequest request = attributes.getRequest();

        asyncService.saveLoginHistory(
                username,
                getClientIpAddress(request),
                request.getHeader("User-Agent")
        );
    }




    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }
}