package com.babalegends.service;

import com.babalegends.exception.NotFoundException;
import com.babalegends.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return new UserPrincipal(userService.getUserByEmail(username));
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException("User not found");
        }
    }
}
