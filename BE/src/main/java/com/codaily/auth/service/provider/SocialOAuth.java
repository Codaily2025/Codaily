package com.codaily.auth.service.provider;

public interface SocialOAuth {
    public String getProviderId();

    public String getProvider();

    public String getEmail();

    public String getName();
}
