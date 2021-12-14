package com.kimcompay.projectjb.apis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class utillService {
    private final static Logger logger=LoggerFactory.getLogger(requestTo.class);

    public static RuntimeException makeRuntimeEX(String message,String methodName) {
        logger.info("getRuntimeEX");
        logger.info(methodName);
        return new RuntimeException("메세지: "+message);
    }
    public static RuntimeException throwRuntimeEX(String message) {
        logger.info("throwRuntimeEX");
        throw new RuntimeException("메세지: "+message);
    }
    public static boolean checkBlank(String ob) {
        logger.info("checkBlank");
        if(ob.isBlank()){
            return true;
        }
        return false;
    }

}
