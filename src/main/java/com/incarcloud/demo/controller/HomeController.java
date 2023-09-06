package com.incarcloud.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incarcloud.demo.GitVer;
import com.incarcloud.demo.entry.WechatJumpArgs;
import com.incarcloud.demo.entry.WechatToken;
import com.incarcloud.demo.entry.WechatUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

@RestController()
@RequestMapping("/api")
public class HomeController {
    private static final Logger s_logger = LoggerFactory.getLogger(HomeController.class);
    private static final ObjectMapper s_mapper = new ObjectMapper();
    @Value("${wechat_jump_login.jump_ticket}")
    private String jump_ticket;

    @Value("${wechat_jump_login.app_secret}")
    private String appSecret;

    @GetMapping("version")
    public ResponseEntity<GitVer> getGitVersion() {
        return ResponseEntity.ok(new GitVer());
    }

    @PostMapping("/wechatJumpLogin")
    public WechatJumpArgs wechatJumpLogin(@RequestBody WechatJumpArgs args) {
        // 为了支持内网地址的微信登录
        try {
            WechatJumpArgs output = new WechatJumpArgs();
            // 当前时间戳
            long tm = System.currentTimeMillis() + 62135596800000L; // 1970 => 0000
            // 拼接在一起,然后计算MD5
            String strArgs = String.format("%s;%s;%s0000;", args.redirect_uri, args.state, tm);
            String md5prefix = calcMD5(strArgs + jump_ticket).substring(0, 8); // keep the first 8 chars is enough

            output.app_id = args.app_id;
            output.redirect_uri = "https://www.incarcloud.com/oauth2/wechat";
            output.state = UriUtils.encode(strArgs + md5prefix, StandardCharsets.UTF_8);

            return output;

        } catch (Exception e) {
            s_logger.error("wechatJumpLogin failed", e);
            throw new RuntimeException("wechatJumpLogin failed", e);
        }
    }

    @GetMapping("/wechatLogin")
    public ResponseEntity<?> wechatLogin(@RequestParam("code") String code, @RequestParam("state") String state) throws Exception {
        // code 换取 access_token
        WechatToken token = fetchTokenByCode(code);
        // 获取用户信息
        WechatUserInfo userInfo = fetchUserInfoByToken(token);

        return ResponseEntity.ok(userInfo);
    }

    // 通过code获取token
    private WechatToken fetchTokenByCode(String code) throws Exception{
        String urlTemplate = "%s/sns/oauth2/access_token?"
                + "appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String url = String.format(urlTemplate, "https://api.weixin.qq.com",
                "wx60648d032a743d57", appSecret, code);

        // fetch token from wechat server
        String jsonResult = fetch(url);
        // Convert to WechatToken
        return s_mapper.readValue(jsonResult, WechatToken.class);
    }

    // 通过token获取用户信息
    private WechatUserInfo fetchUserInfoByToken(WechatToken token) throws Exception{
        String urlTemplate = "%s/sns/userinfo?access_token=%s&openid=%s";
        String url = String.format(urlTemplate, "https://api.weixin.qq.com", token.access_token, token.openid);

        // fetch user info
        String jsonResult = fetch(url);

        // Convert to WechatUserInfo
        WechatUserInfo userInfo = s_mapper.readValue(jsonResult, WechatUserInfo.class);
        return userInfo;
    }

    private String fetch(String url) throws Exception{
        int MAX_WAIT = 10; // in seconds
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(MAX_WAIT)).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // 计算MD5的HEX值
    private String calcMD5(String text) throws NoSuchAlgorithmException {
        MessageDigest md5Provider = MessageDigest.getInstance("MD5");
        byte[] md5x = md5Provider.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : md5x) {
            // in capital letters
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}

