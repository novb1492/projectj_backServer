package com.kimcompay.projectjb.controllers;

import java.util.ArrayList;
import java.util.List;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.kakao.kakaoPayService;
import com.kimcompay.projectjb.apis.kakao.kakaoService;
import com.kimcompay.projectjb.apis.settle.settleService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.exceptions.paymentFailException;
import com.kimcompay.projectjb.exceptions.socialFailException;
import com.kimcompay.projectjb.payments.service.helpPaymentService;

import org.json.simple.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class restAdvice {
    private final static Logger logger=LoggerFactory.getLogger(restAdvice.class);
    @Value("${front.domain}")
    private  String frontDomain;
    @Value("${front.result.page}")
    private  String resultLink;
    @Autowired
    private kakaoService kakaoService;
    @Autowired
    private helpPaymentService helpPaymentService;
    @Autowired
    private settleService settleService;

    @ExceptionHandler(socialFailException.class)
    public void socialFailException(socialFailException exception) {
        logger.info("socialFailException");
        String message=exception.getMessage();
        String company=exception.getCompany();
        String action= exception.getAction();
        if(company.equals(senums.kakao.get())){
            //결제정보 다시 꺼내기
            JSONObject reponse=utillService.changeClass(exception.getBody(), JSONObject.class);
            //실패이유 있다면 알려주기
            message=kakaoService.failToAction(reponse,action);
            //redis비우기
            helpPaymentService.removeInRedis(reponse.get("partner_order_id").toString());
        }
        String url=frontDomain+resultLink+"?kind="+company+"&action="+action+"&result="+false+"&message="+checkMessage(message, exception);
        utillService.doRedirect(utillService.getHttpSerResponse(),url);
    }
    @ExceptionHandler(paymentFailException.class)
    public void paymentFailException(paymentFailException exception) {
        logger.info("paymentFailException");
        String message=exception.getMessage();
        message=settleService.canclePay(exception.getDto());
        String url=frontDomain+resultLink+"?kind=settle&action=payment&result="+false+"&message="+checkMessage(message, exception);
        utillService.doRedirect(utillService.getHttpSerResponse(),url);
    }

    @ExceptionHandler(RuntimeException.class)
    public JSONObject runtimeException(RuntimeException exception) {
        logger.info("runtimeException");
        return utillService.getJson(false, checkMessage(exception.getMessage(), exception));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JSONObject processValidationError(MethodArgumentNotValidException exception) {
        logger.info("processValidationError 유효성 검사 실패");
        BindingResult bindingResult = exception.getBindingResult();
        StringBuilder builder = new StringBuilder();
        List<String>list=new ArrayList<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder.append(fieldError.getDefaultMessage());
            list.add(fieldError.getField());
        }
        return utillService.getJson(false, builder.toString());
    }
    private String checkMessage(String message,Exception exception) {
        if(!message.startsWith("메")){
            message="알수 없는 오류발생";
            exception.printStackTrace();
        }
        return message;
    }
    
}
