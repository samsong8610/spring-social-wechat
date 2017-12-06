package org.springframework.social.wechat.connect;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2ServiceProvider;
import org.springframework.social.wechat.api.WeChat;
import org.springframework.social.wechat.api.WeChatAccessGrant;

public class WeChatConnectionFactory extends OAuth2ConnectionFactory<WeChat> {
    public WeChatConnectionFactory(String appId, String appSecret) {
        super("wechat", new WeChatServiceProvider(appId, appSecret), new WeChatAdapter());
    }

    @Override
    public Connection<WeChat> createConnection(AccessGrant accessGrant) {
        return new WeChatOAuth2Connection(getProviderId(), extractProviderUserId(accessGrant), accessGrant.getAccessToken(),
                accessGrant.getRefreshToken(), accessGrant.getExpireTime(), (OAuth2ServiceProvider<WeChat>)getServiceProvider(), getApiAdapter());
    }

    @Override
    protected String extractProviderUserId(AccessGrant accessGrant) {
        if (accessGrant instanceof WeChatAccessGrant) {
            return ((WeChatAccessGrant) accessGrant).getOpenId();
        }
        return super.extractProviderUserId(accessGrant);
    }
}
