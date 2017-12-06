package org.springframework.social.wechat.api.impl;

import org.springframework.social.wechat.api.UserOperations;
import org.springframework.social.wechat.api.WeChatUserProfile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

public class UserTemplate implements UserOperations {
    private RestTemplate restTemplate;
    private final boolean isAuthorized;
    private String openId;

    public UserTemplate(RestTemplate restTemplate, boolean authorized) {
        this.restTemplate = restTemplate;
        this.isAuthorized = authorized;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    @Override
    public String getProfileId() {
        return getUserProfile().getOpenId();
    }

    @Override
    public WeChatUserProfile getUserProfile() {
        // TODO: error response parsing
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(snsUserInfoUrl());
        if (openId != null) {
            builder.queryParam("openid", this.openId);
        }
        return restTemplate.getForObject(builder.build().toUri(), WeChatUserProfile.class);
    }

    private String snsUserInfoUrl() {
        return "https://api.weixin.qq.com/sns/userinfo";
    }
}
