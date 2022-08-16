package com.imemalta.api.gourmetSnApp.services;

import com.imemalta.api.gourmetSnApp.entities.authentication.Role;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.services.Principal.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{


    private final UserRepository userRepository;
    private final TimeService timeService;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository, TimeService timeService) {
        this.userRepository = userRepository;
        this.timeService = timeService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);


        if (user.isPresent()) {

            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
            for (Role role : user.get().getRoles()){
                grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
            }

            return new UserDetailsImpl(user.get(), grantedAuthorities, timeService);
        } else {
            throw new UsernameNotFoundException(username);
        }
    }
}
