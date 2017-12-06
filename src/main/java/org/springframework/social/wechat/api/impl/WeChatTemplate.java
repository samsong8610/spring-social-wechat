package org.springframework.social.wechat.api.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.social.oauth2.AbstractOAuth2ApiBinding;
import org.springframework.social.oauth2.TokenStrategy;
import org.springframework.social.wechat.api.Gender;
import org.springframework.social.wechat.api.UserOperations;
import org.springframework.social.wechat.api.WeChat;
import org.springframework.social.wechat.api.WeChatUserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WeChatTemplate extends AbstractOAuth2ApiBinding implements WeChat {
    private String openId;
    private UserTemplate userTemplate;

    public WeChatTemplate() {
        super();
        userTemplate = new UserTemplate(getRestTemplate(), isAuthorized());
    }

    public WeChatTemplate(String accessToken) {
        super(accessToken, TokenStrategy.ACCESS_TOKEN_PARAMETER);
        userTemplate = new UserTemplate(getRestTemplate(), isAuthorized());
    }

    @Override
    public UserOperations userOperations() {
        return userTemplate;
    }

    @Override
    public void setOpenId(String openId) {
        this.openId = openId;
        userTemplate.setOpenId(openId);
    }

    @Override
    protected MappingJackson2HttpMessageConverter getJsonMessageConverter() {
        MappingJackson2HttpMessageConverter converter = super.getJsonMessageConverter();
        List<MediaType> supportedMediaTypes = converter.getSupportedMediaTypes();
        List<MediaType> newMediaTypes = new ArrayList<>(supportedMediaTypes.size() + 1);
        newMediaTypes.add(MediaType.TEXT_PLAIN);
        converter.setSupportedMediaTypes(newMediaTypes);
        // TODO: how to support error json parsing with 200 status code?
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(createWeChatModule());
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    private class WeChatModule extends SimpleModule {
        public WeChatModule() {
            super("WeChatModule");
        }

        @Override
        public void setupModule(SetupContext context) {
            context.setMixInAnnotations(WeChatUserProfile.class, WeChatUserProfileMixin.class);
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        abstract class WeChatUserProfileMixin {
            @JsonProperty("sex")
            Gender sex;
            @JsonProperty("country")
            String country;
            @JsonProperty("province")
            String province;
            @JsonProperty("city")
            String city;
            @JsonProperty("privilege")
            Set<String> privileges;

            public WeChatUserProfileMixin(
                    @JsonProperty("unionid") String unionId,
                    @JsonProperty("openid") String openId,
                    @JsonProperty("nickname") String nickname,
                    @JsonProperty("headimgurl") String headImgUrl) {}
        }
    }

    private Module createWeChatModule() {
        return new WeChatModule();
    }
}
