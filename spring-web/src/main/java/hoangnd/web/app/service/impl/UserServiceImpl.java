package hoangnd.web.app.service.impl;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import hoangnd.web.app.repository.UserRepository;
import hoangnd.web.app.service.UserService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername (final String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .map(u -> new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPassword(),
                Stream.ofNullable(u.getRoles())
                    .flatMap(Collection::stream)
                    .map(r -> new SimpleGrantedAuthority(r.getRoleName()))
                    .collect(Collectors.toList())
            ))
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

    }

}
