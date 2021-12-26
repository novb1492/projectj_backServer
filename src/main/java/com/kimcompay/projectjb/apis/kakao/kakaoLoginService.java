package com.kimcompay.projectjb.apis.kakao;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.requestTo;
import com.kimcompay.projectjb.configs.securityConfig;
import com.kimcompay.projectjb.enums.kenum;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.jwt.jwtService;
import com.kimcompay.projectjb.users.user.userService;
import com.kimcompay.projectjb.users.user.userVo;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class kakaoLoginService {
    private Logger logger=LoggerFactory.getLogger(kakaoLoginService.class);

    @Value("${oauth.pwd}")
    private String oauthPwd;
    @Value("${access_token_cookie}")
    private String accessTokenCookieName;
    @Value("${refresh_token_cookie}")
    private String refreshTokenCookieName;
    @Autowired
    private requestTo requestTo;
    @Autowired
    private userService userService;
    @Autowired
    private securityConfig securityConfig;
    @Autowired
    private jwtService jwtService;

    public JSONObject doLogin(String code,String restKey,String redirectUrl) {
        logger.info("doLogin");
        //토큰얻어오기
        JSONObject response=new JSONObject();
        try {
            response=getToken(code,restKey,redirectUrl);
            logger.info("카카오응답: "+response);
        } catch (HttpClientErrorException e) {
            logger.info("카카오 로그인 통신에러 발생");
            logger.info("카카오 에러 메세지: "+e.getMessage());
            //json으로 변환위해 문자열 재수정
            String message=e.getMessage().split(": ")[1];
            message=message.substring(1,message.length()-1);
            JSONObject error=utillService.stringToJson(message);
            logger.info("에러내용: "+error);
            try {
                //if대신 enum사용
                return  utillService.getJson(false,kenum.valueOf(error.get("error_code").toString()).get());
            } catch (IllegalArgumentException e2) {
                return  utillService.getJson(false,senums.defaultFailMessage.get());
            }
        }
        //사용자정보 얻어오기
        response=getKuserInfor(response);
        logger.info("카카오응답: "+response);
        LinkedHashMap<String,Object>linkedHashMap=(LinkedHashMap<String,Object>)response.get("kakao_account");
        logger.info("유저정보: "+linkedHashMap);
        //정보분리
        LinkedHashMap<String,Object>profile=(LinkedHashMap<String,Object>)linkedHashMap.get("profile");
        //나이검사는 테스트어플로 불가
        //map->vo
        String email=linkedHashMap.get("email").toString();
        userVo userVo=mapToVo(profile,email);
        //로그인처리
        String accessToken=jwtService.get_access_token(email);
        String refreshToken=jwtService.get_refresh_token();
        try {
            userService.oauthLogin(userVo,refreshToken);
        } catch (RuntimeException e) {
            logger.info("소셜로그인 에러 발생: "+e.getMessage());
            String message=e.getMessage();
            if(!message.startsWith("메")){
                message=senums.defaultFailMessage.get();
            }
            return utillService.getJson(false, message);
        }
        //쿠키발급
        Map<String,Object>cookies=new HashMap<>();
        cookies.put(accessTokenCookieName, accessToken);
        cookies.put(refreshTokenCookieName, refreshToken);
        utillService.makeCookie(cookies, utillService.getHttpSerResponse());
        return utillService.getJson(true, "카카오 로그인 완료");
    }
    private userVo mapToVo(LinkedHashMap<String,Object>profile,String email) {
        logger.info("mapToVo");
        userVo vo=userVo.builder().email(email).uaddress("테스트주소x").ubirth("테스트x").udetail_address("테스트x").ulogin_date(Timestamp.valueOf(LocalDateTime.now())).uphone("01011113333")
                                    .upostcode("111111").upwd(securityConfig.pwdEncoder().encode(oauthPwd)).urole(senums.user_role.get()).usleep(0).build();
                                    return vo;
    }
    private JSONObject getKuserInfor(JSONObject response) {
        logger.info("getKuserInfor");
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add("Authorization", "Bearer "+response.get("access_token"));
        response.clear();
        return requestTo.requestPost(null, "https://kapi.kakao.com/v2/user/me", httpHeaders);

    }
    private JSONObject getToken(String code,String restKey,String redirectUrl) {
        logger.info("getToken");
        logger.info("code: "+code);
        //body만들기
        MultiValueMap<String,Object> multiValueBody=new LinkedMultiValueMap<>();
        multiValueBody.add("grant_type", "authorization_code");//카카오에서  요청하는 고정값
        multiValueBody.add("client_id", restKey);
        multiValueBody.add("redirect_uri", redirectUrl);
        multiValueBody.add("code", code);
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return requestTo.requestPost(multiValueBody, "https://kauth.kakao.com/oauth/token", headers);
    }
    
}
