package com.dawillygene.Smart.Inventory.Management.System.service;

import com.dawillygene.Smart.Inventory.Management.System.entity.User;
import com.dawillygene.Smart.Inventory.Management.System.entity.Role;
import com.dawillygene.Smart.Inventory.Management.System.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Custom UserDetailsService implementation for Spring Security
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    
    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username/email: {}", usernameOrEmail);
        
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
            .orElseThrow(() -> {
                log.warn("Authentication failed: User not found with username/email: {}", usernameOrEmail);
                return new UsernameNotFoundException("User not found: " + usernameOrEmail);
            });
        
        log.debug("User found: id={}, username={}, active={}", user.getId(), user.getUsername(), user.getIsActive());
        
        if (!user.getIsActive()) {
            log.warn("Authentication failed: User account is disabled: {}", usernameOrEmail);
            throw new UsernameNotFoundException("User account is disabled: " + usernameOrEmail);
        }
        
        // Log roles assigned to user
        if (user.getRoles() != null) {
            log.debug("User roles: {}", 
                user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.joining(", ")));
        } else {
            log.warn("User has no roles assigned!");
        }
        
        UserDetails userDetails = UserPrincipal.create(user);
        log.debug("Created UserDetails with authorities: {}", 
            userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", ")));
                
        return userDetails;
    }
    
    /**
     * Load user by ID for JWT authentication
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("User account is disabled with id: " + id);
        }
        
        return UserPrincipal.create(user);
    }
    
    /**
     * Custom UserPrincipal class
     */
    public static class UserPrincipal implements UserDetails {
        private Long id;
        private String username;
        private String email;
        private String password;
        private Collection<? extends GrantedAuthority> authorities;
        private boolean enabled;
        
        public UserPrincipal(Long id, String username, String email, String password, 
                            Collection<? extends GrantedAuthority> authorities, boolean enabled) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.password = password;
            this.authorities = authorities;
            this.enabled = enabled;
        }
        
        public static UserPrincipal create(User user) {
            Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
            
            return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getIsActive()
            );
        }
        
        public Long getId() {
            return id;
        }
        
        public String getEmail() {
            return email;
        }
        
        @Override
        public String getUsername() {
            return username;
        }
        
        @Override
        public String getPassword() {
            return password;
        }
        
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}
