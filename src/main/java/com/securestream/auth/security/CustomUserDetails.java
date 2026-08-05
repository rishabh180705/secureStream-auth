package com.securestream.auth.security;


import java.util.Collection;
import java.util.List;

import com.securestream.auth.entity.Subscription;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.securestream.auth.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;
    @Override

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public @NullMarked String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }

//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }

    public String getSubscription() {
        return user.getSubscription().name();
    }
    public String getUserId() {
        return user.getId();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }




}