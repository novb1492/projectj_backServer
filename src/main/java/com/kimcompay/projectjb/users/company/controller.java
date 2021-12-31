package com.kimcompay.projectjb.users.company;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class controller {
    private Logger logger=LoggerFactory.getLogger(controller.class);

    @Autowired
    private storeService storeService;

    @RequestMapping(value = "/auth/store/{action}",method = RequestMethod.POST)
    public void storeAction(@PathVariable String action,@Valid @RequestBody tryInsertStoreDto tryInsertStoreDto) {
        logger.info("storeAction");
        storeService.insertOrUpdate(tryInsertStoreDto,action);
    }
}
