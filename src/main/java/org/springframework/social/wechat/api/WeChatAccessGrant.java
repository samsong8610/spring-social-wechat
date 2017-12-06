package org.springframework.social.wechat.api;

import org.springframework.social.oauth2.AccessGrant;

public class WeChatAccessGrant extends AccessGrant {
    private final String openId;
    private final String unionId;

    public WeChatAccessGrant(String accessToken) {
        this(accessToken, null, null, null, null, null);
    }

    public WeChatAccessGrant(String accessToken, String scope, String refreshToken, Long expiresIn, String openId, String unionId) {
        super(accessToken, scope, refreshToken, expiresIn);
        this.openId = openId;
        this.unionId = unionId;
    }

    public String getOpenId() {
        return openId;
    }

    public String getUnionId() {
        return unionId;
    }
}
