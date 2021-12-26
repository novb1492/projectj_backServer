package com.kimcompay.projectjb.apis;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class requestTo {
    private final  Logger logger=LoggerFactory.getLogger(requestTo.class);
    
    public <T> JSONObject requestPost(T body,String url,HttpHeaders headers) {
        logger.info("requestPost");

            //body+header합치기
            HttpEntity<T>entity=new HttpEntity<>(body,headers);
            logger.info("요청 정보: "+entity);
            logger.info("요청 url: "+url);
            RestTemplate restTemplate=new RestTemplate();
            return restTemplate.postForObject(url, entity, JSONObject.class);
       
    }

    public <T> JSONObject requestGet(T body,String url,HttpHeaders headers) {
        logger.info("requestGet");
        RestTemplate restTemplate=new RestTemplate();
        HttpEntity<T>entity=new HttpEntity<>(body,headers);
        ResponseEntity<JSONObject>responseEntity=restTemplate.exchange(url,HttpMethod.GET,entity,JSONObject.class);
        return responseEntity.getBody();
    }

}
