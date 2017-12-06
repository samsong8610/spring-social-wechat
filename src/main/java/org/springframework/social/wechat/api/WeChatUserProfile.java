package org.springframework.social.wechat.api;

import java.io.Serializable;
import java.util.Set;

public class WeChatUserProfile implements Serializable {
    public static final long serialVersionUID = 1L;

//    private final String unionId;
//    private final String openId;
    private String unionId;
    private String openId;
    private String nickname;
    private String headImgUrl;
    private Gender sex;
    private String country;
    private String province;
    private String city;
    private Set<String> privileges;

    public WeChatUserProfile() {}

    public WeChatUserProfile(String unionId, String openId, String nickname, String headImgUrl) {
        this.unionId = unionId;
        this.openId = openId;
        this.nickname = nickname;
        this.headImgUrl = headImgUrl;
    }

    public String getUnionId() {
        return unionId;
    }

    public String getOpenId() {
        return openId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    public Gender getSex() {
        return sex;
    }

    public void setSex(Gender sex) {
        this.sex = sex;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Set<String> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Set<String> privileges) {
        this.privileges = privileges;
    }
}
