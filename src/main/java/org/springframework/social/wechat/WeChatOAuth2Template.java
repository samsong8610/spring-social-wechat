package org.springframework.social.wechat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.social.oauth2.*;
import org.springframework.social.support.ClientHttpRequestFactorySelector;
import org.springframework.social.support.FormMapHttpMessageConverter;
import org.springframework.social.support.LoggingErrorHandler;
import org.springframework.social.wechat.api.WeChatAccessGrant;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * WeChatOAuth2Operations implementation that uses REST-template to make the WeChat OAuth calls.
 * @author Sam Song
 */
public class WeChatOAuth2Template implements OAuth2Operations {
    private final String appId;
    private final String appSecret;
    private final String authorizeUrl;
    private final String accessTokenUrl;
    private final String refreshTokenUrl;
    private String authenticateUrl;
    private RestTemplate restTemplate;
    private boolean useParametersForClientAuthentication;

    /**
     * Constructs an WeChatOAuth2Template for a given set of client credentials.
     * Assumes that the authorization URL is the same as the authentication URL.
     * @param appId the client ID
     * @param appSecret the client secret
     * @param authorizeUrl the base URL to redirect to when doing authorization code or implicit grant authorization
     * @param accessTokenUrl the URL at which an authorization code or user credentials may be exchanged for an access token
     * @param refreshTokenUrl the URL at which a refresh token can be exchanged for a new access token.
     */
    public WeChatOAuth2Template(String appId, String appSecret, String authorizeUrl, String accessTokenUrl, String refreshTokenUrl) {
        this(appId, appSecret, authorizeUrl, null, accessTokenUrl, refreshTokenUrl);
    }

    /**
     * Constructs an WeChatOAuth2Template for a given set of client credentials.
     * @param appId the client ID
     * @param appSecret the client secret
     * @param authorizeUrl the base URL to redirect to when doing authorization code or implicit grant authorization
     * @param authenticateUrl the URL to redirect to when doing authentication via authorization code grant
     * @param accessTokenUrl the URL at which an authorization code or user credentials may be exchanged for an access token
     * @param refreshTokenUrl the URL at which a refresh token can be exchanged for a new access token.
     */
    public WeChatOAuth2Template(String appId, String appSecret, String authorizeUrl, String authenticateUrl, String accessTokenUrl, String refreshTokenUrl) {
        this.appId = appId;
        this.appSecret = appSecret;
        String clientInfo = "?appid=" + formEncode(appId);
        this.authorizeUrl = authorizeUrl + clientInfo;
        if (authenticateUrl != null) {
            this.authenticateUrl = authenticateUrl + clientInfo;
        } else {
            this.authenticateUrl = null;
        }
        this.accessTokenUrl = accessTokenUrl;
        this.refreshTokenUrl = refreshTokenUrl;
    }

    /**
     * Set to true to pass client credentials to the provider as parameters instead of using HTTP Basic authentication.
     * @param useParametersForClientAuthentication true if the client credentials should be passed as parameters; false if passed via HTTP Basic
     */
    public void setUseParametersForClientAuthentication(boolean useParametersForClientAuthentication) {
        this.useParametersForClientAuthentication = useParametersForClientAuthentication;
    }

    @Override
    public String buildAuthorizeUrl(OAuth2Parameters parameters) {
        return buildAuthUrl(authorizeUrl, GrantType.AUTHORIZATION_CODE, parameters);
    }

    @Override
    public String buildAuthorizeUrl(GrantType grantType, OAuth2Parameters parameters) {
        return buildAuthUrl(authorizeUrl, grantType, parameters);
    }

    @Override
    public String buildAuthenticateUrl(OAuth2Parameters parameters) {
        return authenticateUrl != null ? buildAuthUrl(authenticateUrl, GrantType.AUTHORIZATION_CODE, parameters) : buildAuthorizeUrl(GrantType.AUTHORIZATION_CODE, parameters);
    }

    @Override
    public String buildAuthenticateUrl(GrantType grantType, OAuth2Parameters parameters) {
        return authenticateUrl != null ? buildAuthUrl(authenticateUrl, grantType, parameters) : buildAuthorizeUrl(grantType, parameters);
    }

    @Override
    public AccessGrant exchangeForAccess(String authorizationCode, String redirectUri, MultiValueMap<String, String> additionalParameters) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        if (useParametersForClientAuthentication) {
            params.set("appid", appId);
            params.set("secret", appSecret);
        }
        params.set("code", authorizationCode);
        params.set("redirect_uri", redirectUri);
        params.set("grant_type", "authorization_code");
        if (additionalParameters != null) {
            params.putAll(additionalParameters);
        }
        return postForAccessGrant(accessTokenUrl, params);
    }

    @Override
    public AccessGrant exchangeCredentialsForAccess(String username, String password, MultiValueMap<String, String> additionalParameters) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        if (useParametersForClientAuthentication) {
            params.set("appid", appId);
            params.set("secret", appSecret);
        }
        params.set("username", username);
        params.set("password", password);
        params.set("grant_type", "password");
        if (additionalParameters != null) {
            params.putAll(additionalParameters);
        }
        return postForAccessGrant(accessTokenUrl, params);
    }

    @Override
    public AccessGrant refreshAccess(String refreshToken, String scope, MultiValueMap<String, String> additionalParameters) {
        additionalParameters.set("scope", scope);
        return refreshAccess(refreshToken, additionalParameters);
    }

    @Override
    public AccessGrant refreshAccess(String refreshToken, MultiValueMap<String, String> additionalParameters) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        if (useParametersForClientAuthentication) {
            params.set("appid", appId);
            params.set("secret", appSecret);
        }
        params.set("refresh_token", refreshToken);
        params.set("grant_type", "refresh_token");
        if (additionalParameters != null) {
            params.putAll(additionalParameters);
        }
        return postForAccessGrant(refreshTokenUrl, params);
    }

    @Override
    public AccessGrant authenticateClient() {
        return authenticateClient(null);
    }

    @Override
    public AccessGrant authenticateClient(String scope) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        if (useParametersForClientAuthentication) {
            params.set("appid", appId);
            params.set("secret", appSecret);
        }
        params.set("grant_type", "client_credentials");
        if (scope != null) {
            params.set("scope", scope);
        }
        return postForAccessGrant(accessTokenUrl, params);
    }

    // subclassing hooks

    /**
     * Creates the {@link RestTemplate} used to communicate with the provider's OAuth 2 API.
     * This implementation creates a RestTemplate with a minimal set of HTTP message converters ({@link FormHttpMessageConverter} and {@link MappingJackson2HttpMessageConverter}).
     * May be overridden to customize how the RestTemplate is created.
     * For example, if the provider returns data in some format other than JSON for form-encoded, you might override to register an appropriate message converter.
     * @return a {@link RestTemplate} used to communicate with the provider's OAuth 2 API
     */
    protected RestTemplate createRestTemplate() {
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactorySelector.getRequestFactory();
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>(3);
        converters.add(new FormHttpMessageConverter());
        converters.add(new FormMapHttpMessageConverter());
        converters.add(createJsonHttpMessageConverter());
        restTemplate.setMessageConverters(converters);
        restTemplate.setErrorHandler(new LoggingErrorHandler());
        if (!useParametersForClientAuthentication) {
            List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
            if (interceptors == null) {   // defensively initialize list if it is null. (See SOCIAL-430)
                interceptors = new ArrayList<ClientHttpRequestInterceptor>();
                restTemplate.setInterceptors(interceptors);
            }
            interceptors.add(new PreemptiveBasicAuthClientHttpRequestInterceptor(appId, appSecret));
        }
        return restTemplate;
    }

    /**
     * Posts the request for an access grant to the provider.
     * The default implementation uses RestTemplate to request the access token and expects a JSON response to be bound to a Map. The information in the Map will be used to create an {@link AccessGrant}.
     * Since the OAuth 2 specification indicates that an access token response should be in JSON format, there's often no need to override this method.
     * If all you need to do is capture provider-specific data in the response, you should override createAccessGrant() instead.
     * However, in the event of a provider whose access token response is non-JSON, you may need to override this method to request that the response be bound to something other than a Map.
     * For example, if the access token response is given as form-encoded, this method should be overridden to call RestTemplate.postForObject() asking for the response to be bound to a MultiValueMap (whose contents can then be used to create an AccessGrant).
     * @param accessTokenUrl the URL of the provider's access token endpoint.
     * @param parameters the parameters to post to the access token endpoint.
     * @return the access grant.
     */
    @SuppressWarnings("unchecked")
    protected AccessGrant postForAccessGrant(String accessTokenUrl, MultiValueMap<String, String> parameters) {
        return extractAccessGrant(getRestTemplate().postForObject(accessTokenUrl, parameters, Map.class));
    }

    /**
     * Creates an {@link AccessGrant} given the response from the access token exchange with the provider.
     * May be overridden to create a custom AccessGrant that captures provider-specific information from the access token response.
     * @param accessToken the access token value received from the provider
     * @param scope the scope of the access token
     * @param refreshToken a refresh token value received from the provider
     * @param expiresIn the time (in seconds) remaining before the access token expires.
     * @param response all parameters from the response received in the access token exchange.
     * @return an {@link AccessGrant}
     */
    protected AccessGrant createAccessGrant(String accessToken, String scope, String refreshToken, Long expiresIn, Map<String, Object> response) {

        return new WeChatAccessGrant(accessToken, scope, refreshToken, expiresIn,
                (String) response.getOrDefault("openid", null),
                (String) response.getOrDefault("unionid", null));
    }

    // testing hooks

    protected RestTemplate getRestTemplate() {
        // Lazily create RestTemplate to make sure all parameters have had a chance to be set.
        // Can't do this InitializingBean.afterPropertiesSet() because it will often be created directly and not as a bean.
        if (restTemplate == null) {
            restTemplate = createRestTemplate();
        }
        return restTemplate;
    }

    // internal helpers

    private String buildAuthUrl(String baseAuthUrl, GrantType grantType, OAuth2Parameters parameters) {
        StringBuilder authUrl = new StringBuilder(baseAuthUrl);
        if (grantType == GrantType.AUTHORIZATION_CODE) {
            authUrl.append('&').append("response_type").append('=').append("code");
        } else if (grantType == GrantType.IMPLICIT_GRANT) {
            authUrl.append('&').append("response_type").append('=').append("token");
        }
        for (Iterator<Map.Entry<String, List<String>>> additionalParams = parameters.entrySet().iterator(); additionalParams.hasNext();) {
            Map.Entry<String, List<String>> param = additionalParams.next();
            String name = formEncode(param.getKey());
            for (Iterator<String> values = param.getValue().iterator(); values.hasNext();) {
                authUrl.append('&').append(name);
                String value = values.next();
                if (StringUtils.hasLength(value)) {
                    authUrl.append('=').append(formEncode(value));
                }
            }
        }
        return authUrl.toString();
    }

    private String formEncode(String data) {
        try {
            return URLEncoder.encode(data, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            // should not happen, UTF-8 is always supported
            throw new IllegalStateException(ex);
        }
    }

    private AccessGrant extractAccessGrant(Map<String, Object> result) {
        return createAccessGrant((String) result.get("access_token"), (String) result.get("scope"),
                (String) result.get("refresh_token"), getIntegerValue(result, "expires_in"), result);
    }

    // Retrieves object from map into an Integer, regardless of the object's actual type. Allows for flexibility in object type (eg, "3600" vs 3600).
    private Long getIntegerValue(Map<String, Object> map, String key) {
        try {
            return Long.valueOf(String.valueOf(map.get(key))); // normalize to String before creating integer value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private AbstractJackson2HttpMessageConverter createJsonHttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        List<MediaType> supportedMediaTypes = converter.getSupportedMediaTypes();
        List<MediaType> newMediaTypes = new ArrayList<>(supportedMediaTypes.size() + 1);
        newMediaTypes.add(MediaType.TEXT_PLAIN);
        converter.setSupportedMediaTypes(newMediaTypes);
        return converter;
    }

    class ContentTypeTolerateJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
        public ContentTypeTolerateJackson2HttpMessageConverter() {
            this(Jackson2ObjectMapperBuilder.json().build());
        }

        protected ContentTypeTolerateJackson2HttpMessageConverter(ObjectMapper objectMapper) {
            this(objectMapper, MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
        }

        protected ContentTypeTolerateJackson2HttpMessageConverter(ObjectMapper objectMapper, MediaType... supportedMediaTypes) {
            super(objectMapper, supportedMediaTypes);
        }
    }
}
