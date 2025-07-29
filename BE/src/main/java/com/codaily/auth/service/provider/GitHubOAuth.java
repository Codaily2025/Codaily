//package com.codaily.auth.service.provider;
//
//import lombok.RequiredArgsConstructor;
//
//import java.util.Map;
//
//@RequiredArgsConstructor
//public class GitHubOAuth implements SocialOAuth {
//    private final Map<String, Object> attributes;
//
//    @Override
//    public String getProviderId() {
//        // GitHub의 고유 사용자 ID는 숫자이므로 문자열로 변환
//        return String.valueOf(attributes.get("id"));
//    }
//
//    @Override
//    public String getProvider() {
//        return "github";
//    }
//
//    @Override
//    public String getEmail() {
//        return (String) attributes.get("email");
//    }
//
//    @Override
//    public String getName() {
//        return (String) attributes.get("name");
//    }
//}
