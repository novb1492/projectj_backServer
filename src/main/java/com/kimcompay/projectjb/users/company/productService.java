package com.kimcompay.projectjb.users.company;

import java.util.List;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.users.company.model.flyerDao;
import com.kimcompay.projectjb.users.company.model.flyerVo;
import com.kimcompay.projectjb.users.company.model.productDao;
import com.kimcompay.projectjb.users.company.model.productEventDao;
import com.kimcompay.projectjb.users.company.model.productEventVo;
import com.kimcompay.projectjb.users.company.model.productVo;
import com.kimcompay.projectjb.users.company.model.tryProductInsertDto;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class productService {
    private Logger logger=LoggerFactory.getLogger(productService.class);
    @Autowired
    private productDao productDao;
    @Autowired
    private productEventDao productEventDao;

    @Transactional(rollbackFor = Exception.class)
    public JSONObject insert(tryProductInsertDto tryProductInsertDto) {
        logger.info("insert");
        //옳바른 전단이지 검
        productVo vo=productVo.builder().category(tryProductInsertDto.getCategory()).text(tryProductInsertDto.getText()).flyerId(tryProductInsertDto.getFlyerId()).origin(tryProductInsertDto.getOrigin()).price(tryProductInsertDto.getPrice()).productImgPath(tryProductInsertDto.getProductImgPath()).productName(tryProductInsertDto.getProductName()).eventFlag(tryProductInsertDto.getEventFlag()).build();
        if(tryProductInsertDto.getEventFlag()==1){
            logger.info("이벤트 테이블 insert시도");
            checkEvent(tryProductInsertDto.getEventInfors());

        }
        productDao.save(vo);
        List<Map<String,Object>>eventInfors=(List<Map<String,Object>>)tryProductInsertDto.getEventInfors();
        for(Map<String,Object>eventInfor:eventInfors){
            productEventVo vo2=productEventVo.builder().date(eventInfor.get("date").toString()).productId(vo.getPid()).eventPrice(Integer.parseInt(eventInfor.get("price").toString())).build();
            productEventDao.save(vo2);
        }
        return utillService.getJson(true, "상품등록에 성공 하였습니다");
    }
   
    private boolean checkPrice(int price) {
        logger.info("checkPrice");
        if(price<=0){
            return true;
        }
        return false;
    }
    private void checkEvent(List<Map<String,Object>>eventInfors) {
        logger.info("checkEvent");

            for(Map<String,Object>eventInfor:eventInfors){
                try {
                    if(checkPrice(Integer.parseInt(eventInfor.get("price").toString()))){
                        throw utillService.makeRuntimeEX("이벤트가격이 0원 보다 작거나 같습니다 \n 날짜: "+eventInfor.get("date"), "checkEvent");
    
                    }
                } catch (NumberFormatException e) {
                    throw utillService.makeRuntimeEX("가격은 숫자만입렵해주세요 \n 날짜: "+eventInfor.get("date"), "checkEvent");
                }
                
            }
        
        logger.info("이벤트 유효성 검사 통과");
    }
}