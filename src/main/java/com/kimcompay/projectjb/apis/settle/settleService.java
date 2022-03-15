package com.kimcompay.projectjb.apis.settle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.payments.model.order.orderVo;
import com.kimcompay.projectjb.payments.model.pay.paymentVo;
import com.kimcompay.projectjb.payments.model.pay.settleDto;
import com.kimcompay.projectjb.payments.service.aes256;
import com.kimcompay.projectjb.payments.service.sha256;
import com.kimcompay.projectjb.users.company.model.products.productVo;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class settleService {
    
    public JSONObject makeRequestPayInfor(String productNames,paymentVo paymentVo,List<orderVo>orders,String mchtId,String expireIfVank) { 
        //응답 생성
        JSONObject respons=new JSONObject();
        String mchtTrdNo=paymentVo.getMchtTrdNo();
        String method=paymentVo.getMethod();
        Map<String,String>dateAndTime=utillService.getSettleTimeAndDate(LocalDateTime.now());
        String date=dateAndTime.get("date");
        String time=dateAndTime.get("time");
        if(expireIfVank!=null){
            respons.put("expireDt",expireIfVank);
        }
        respons.put("method", method);
        respons.put("mchtId", mchtId);
        respons.put("mchtCustId", aes256.encrypt(Integer.toString(utillService.getLoginId())));
        respons.put("date", date);
        respons.put("time", time);
        respons.put("mchtTrdNo", mchtTrdNo);
        respons.put("productNames",new StringBuffer(productNames).deleteCharAt(productNames.length()-1));
        respons.put("price", aes256.encrypt( Integer.toString(paymentVo.getTotalPrice())));
        respons.put("pktHash", sha256.encrypt(utillService.getSettleText(mchtId,method,mchtTrdNo, date, time, Integer.toString(paymentVo.getTotalPrice()))));
        respons.put("flag", true);
        return respons;
    }
}
