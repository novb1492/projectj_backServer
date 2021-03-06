package com.kimcompay.projectjb.jwt;

import java.util.Date;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class jwtService {
    
    @Value("${access_name}")
    private String access_name;
    @Value("${refresh_name}")
    private String refresh_name;
    @Value("${jwt_sing}")
    private String jwt_sing;
    @Value("${access_expire_min}")
    private int access_expire_min;
    @Value("${refresh_expire_day}")
    private int refresh_expire_day;


    public String get_access_token(String id) {
        return JWT.create().withSubject(access_name).withClaim("id",id).withExpiresAt(new Date(System.currentTimeMillis()+1000*10000)).sign(Algorithm.HMAC512(jwt_sing));
    }
    public String get_refresh_token() {
        return JWT.create().withSubject(refresh_name).withExpiresAt(new Date(System.currentTimeMillis()+1000*60*24*refresh_expire_day)).sign(Algorithm.HMAC512(jwt_sing));
    }
    public String openJwt(String accessToken) {
        return JWT.require(Algorithm.HMAC512(jwt_sing)).build().verify(accessToken).getClaim("id").asString();
    }
}
