package org.springframework.social.wechat.security;

import org.springframework.social.security.provider.OAuth2AuthenticationService;
import org.springframework.social.wechat.api.WeChat;
import org.springframework.social.wechat.connect.WeChatConnectionFactory;

public class WeChatAuthenticationService extends OAuth2AuthenticationService<WeChat> {
    public WeChatAuthenticationService(String appId, String appSecret) {
        super(new WeChatConnectionFactory(appId, appSecret));
    }
}
