package hr.algebra.ledvision.service;

import hr.algebra.ledvision.model.LoginHistory;
import hr.algebra.ledvision.repository.LoginHistoryRepository;
import hr.algebra.ledvision.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncService {

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    @Async
    public void saveLoginHistory(String username, String ipAddress, String userAgent) {
        userRepository.findByUsername(username).ifPresent(user -> {
            LoginHistory history = new LoginHistory();
            history.setUser(user);
            history.setIpAddress(ipAddress);
            history.setUserAgent(userAgent);
            loginHistoryRepository.save(history);
        });
    }
}