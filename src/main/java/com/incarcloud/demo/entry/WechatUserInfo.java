package com.incarcloud.demo.entry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WechatUserInfo {
    public String openid;
    public String nickname;
    public int sex;
    public String province;
    public String city;
    public String country;
    public String headimgurl;
    public String unionid;
}
