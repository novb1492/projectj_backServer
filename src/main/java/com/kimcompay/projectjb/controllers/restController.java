package com.kimcompay.projectjb.controllers;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.kimcompay.projectjb.apis.sns.snsService;
import com.kimcompay.projectjb.users.user.tryInsertDto;
import com.kimcompay.projectjb.users.user.userService;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class restController {
    private Logger logger=LoggerFactory.getLogger(restController.class);
    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    @Autowired
    private snsService snsService;
    @Autowired
    private userService userService;

    @RequestMapping(value = "/test/**",method = RequestMethod.GET)
    public void name(HttpSession session) {
        logger.info("test");
        logger.info(session.getAttribute("auth").toString());
    }
    @RequestMapping(value = "/message",method =RequestMethod.POST )
    public void sendSqs(HttpServletRequest request,HttpServletResponse response) {
        logger.info("sendSqs");
        String url="https://sqs.ap-northeast-2.amazonaws.com/527222691614/testsqs";
        String message=request.getParameter("message");
        queueMessagingTemplate.send(url,MessageBuilder.withPayload(message).build());
        
    }
    @RequestMapping(value = "/sns/**",method = RequestMethod.POST)
    public JSONObject sendSns(@RequestBody JSONObject jsonObject,HttpSession httpSession,HttpServletResponse response) {
        logger.info("sendSns Controller");
        return snsService.send(jsonObject, httpSession);
    }
    @RequestMapping(value = "/confrim/**",method = RequestMethod.POST)
    public JSONObject checkConfrim(@RequestBody JSONObject jsonObject,HttpSession httpSession) {
        logger.info("checkConfrim");
        return snsService.confrim(jsonObject,httpSession);
    }
    @RequestMapping(value = "/user/**",method = RequestMethod.POST)
    public void tryJoin(@Valid @RequestBody tryInsertDto tryInsertDto ,HttpSession session) {
        logger.info("tryJoin");
        logger.info(tryInsertDto.toString());
        userService.insert(tryInsertDto, session);
    }
}
