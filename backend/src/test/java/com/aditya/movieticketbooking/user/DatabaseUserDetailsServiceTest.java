package com.aditya.movieticketbooking.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.aditya.movieticketbooking.common.enums.Role;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void loadsDatabaseUserWithRoleAuthority() {
        User databaseUser = User.create("Admin", "admin@moviebooking.local", "encoded-password", Role.ADMIN);
        when(userRepository.findByEmail("admin@moviebooking.local")).thenReturn(Optional.of(databaseUser));

        UserDetails userDetails = new DatabaseUserDetailsService(userRepository)
                .loadUserByUsername("admin@moviebooking.local");

        assertEquals("admin@moviebooking.local", userDetails.getUsername());
        assertEquals("encoded-password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }
}
