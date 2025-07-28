package com.codaily.auth.service;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.auth.entity.User;
import com.codaily.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security에서 username으로 사용자 정보를 조회할 때 사용하는 메서드
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username은 DB에서의 식별자 (예: 이메일, 닉네임 등)
        log.info("username: " + username);
        User user = userRepository.findByNickname(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        log.info("user: "+user.getNickname());
        return new PrincipalDetails(user);
    }
}
