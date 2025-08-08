package com.codaily.auth.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.auth.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ){
        if(principalDetails == null){
            return ResponseEntity.status(401).build();
        }

        User user = principalDetails.getUser();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("email", user.getEmail());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("socialProvider", user.getSocialProvider());
        userInfo.put("githubAccount", user.getGithubAccount());
        userInfo.put("needsGithubConnection", user.getGithubAccount() == null);

        return ResponseEntity.ok(userInfo);
    }
}
