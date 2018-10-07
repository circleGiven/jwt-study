package com.example.demo.security;

import com.example.demo.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

public final class JwtUserFactory {
    private JwtUserFactory() {
    }

    public static UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getName(),
                user.getEmail(),
                mapToGrantedAuthorities(user.getAdminFlag())
        );
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(Boolean adminFlag) {
        // 권한 목록
        List<GrantedAuthority> authorities = new ArrayList();
        // 유저 권한 추가
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        // 해당 사용자가 어드민이 있다면 어드민 권한 추가
        if (adminFlag == true) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return authorities;
    }
}
