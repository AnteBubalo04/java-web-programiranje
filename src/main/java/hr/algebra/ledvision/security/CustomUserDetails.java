package hr.algebra.ledvision.security;

import hr.algebra.ledvision.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    // NOTE: must NOT be transient. UserDetails extends Serializable, and Spring
    // Boot DevTools restarts trigger Tomcat to persist/restore active sessions
    // (including the SecurityContext's principal) across the restart - a
    // transient field gets silently dropped in that round-trip, leaving this
    // object with a null user and crashing every request that reuses the
    // session (see the NullPointerException in getUsername() below).
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(
                "ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
}