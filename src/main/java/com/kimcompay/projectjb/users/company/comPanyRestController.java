package com.kimcompay.projectjb.users.company;

import com.kimcompay.projectjb.users.company.service.storeService;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/store")
public class comPanyRestController {
    
    @Autowired
    private storeService storeService;

    //스토어 정보 불러오기 원래는 매장 고유값으로 해야하나 
    //데이터가 없는 관계로 주소 매장이름으로 조회
    @RequestMapping(value ="/get/{storeAddress}/{storeName}/{page}",method=RequestMethod.GET)
    public JSONObject getStore(@PathVariable String storeAddress,@PathVariable String storeName,@PathVariable int page) {
        return storeService.getStore(storeAddress, storeName,page);
    }
    //매장 리뷰 페이징
    @RequestMapping(value ="/get/reviews/{storeId}/{page}",method=RequestMethod.GET)
    public JSONObject getStoreReviews(@PathVariable int storeId,@PathVariable int page) {
        System.out.println(storeId);
        System.out.println(page);
        return storeService.getReviews(storeId, page);
    }
}
