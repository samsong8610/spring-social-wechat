package org.springframework.social.wechat.connect;

import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UserProfileBuilder;
import org.springframework.social.wechat.api.WeChat;
import org.springframework.social.wechat.api.WeChatUserProfile;
import org.springframework.web.client.HttpClientErrorException;

public class WeChatAdapter implements ApiAdapter<WeChat> {
    @Override
    public boolean test(WeChat api) {
        try {
            api.userOperations().getUserProfile();
            return true;
        } catch (HttpClientErrorException e) {
            return false;
        }
    }

    @Override
    public void setConnectionValues(WeChat api, ConnectionValues values) {
        WeChatUserProfile profile = api.userOperations().getUserProfile();
        values.setProviderUserId(profile.getOpenId());
        values.setDisplayName(profile.getNickname());
        values.setImageUrl(profile.getHeadImgUrl());
        // TODO: use actual user profile url.
        values.setProfileUrl("");
    }

    @Override
    public UserProfile fetchUserProfile(WeChat api) {
        WeChatUserProfile profile = api.userOperations().getUserProfile();
        return new UserProfileBuilder().setName(profile.getNickname()).build();
    }

    @Override
    public void updateStatus(WeChat api, String message) {
        // TODO: update status
    }
}
