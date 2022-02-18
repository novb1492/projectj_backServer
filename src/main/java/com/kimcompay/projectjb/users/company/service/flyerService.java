package com.kimcompay.projectjb.users.company.service;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.apis.google.ocrService;
import com.kimcompay.projectjb.users.company.model.flyerDao;
import com.kimcompay.projectjb.users.company.model.flyerVo;

import org.json.simple.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Service
public class flyerService {
    private final int pageSize=2;
    @Autowired
    private flyerDao flyerDao;
    @Autowired
    private fileService fileService;

    
    public JSONObject getFlyerAndProducts(int flyerId) {
        JSONObject response=new JSONObject();
        //조회전단 검색
        List<Map<String,Object>>flyerAndProducts=flyerDao.findFlyerJoinProducts(flyerId);
        if(utillService.checkEmthy(flyerAndProducts)){
            throw utillService.makeRuntimeEX("존재하지 않는 전단입니다", "getFlyerAndProducts");
        }
        //소유자 검사
        utillService.checkOwner(Integer.parseInt(flyerAndProducts.get(0).get("company_id").toString()), "회사소유의 전단이 아닙니다");
        //flyer정보 꺼내기
        Map<String,Object>flyer=new HashMap<>();
        flyer.put("flyer_id", flyerAndProducts.get(0).get("flyer_id"));
        flyer.put("default", flyerAndProducts.get(0).get("default_select"));
        flyer.put("flyer_img_path", flyerAndProducts.get(0).get("flyer_img_path"));
        response.put("flyerFlag", true);
        response.put("flyer", flyer);
        //전단에 있는 상품들 추출
        boolean productFlag=false;
        List<Map<String,Object>>products=new ArrayList<>();
        for(Map<String,Object>product:flyerAndProducts){
            if(Optional.ofNullable(product.get("product_id")).orElseGet(()->null)!=null){
                productFlag=true;
                Map<String,Object>onlyProduct=new HashMap<>();
                onlyProduct.put("product_id", product.get("product_id"));
                onlyProduct.put("origin", product.get("origin"));
                onlyProduct.put("price", product.get("price"));
                onlyProduct.put("product_img_path", product.get("product_img_path"));
                onlyProduct.put("event_state", product.get("event_state"));
                products.add(onlyProduct);                
            }
        }
        if(productFlag){
            response.put("products", products);
        }
        response.put("productFlag", productFlag);

        return response;
    }

    /*public JSONObject getProductAndEvent(int productId) {
        logger.info("getFlyerAndProducts");
        JSONObject response=new JSONObject();
        //조회전단 검색
        //소유자 검사
        utillService.checkOwner(flyerVo.getCompanyId(), "회사소유의 전단이 아닙니다");
        response.put("flyerFlag", true);
        response.put("flyer", flyerVo);
        //전단에 있는 상품들 검색
        List<productVo>products=productService.getByFlyerId(flyerId);
        List<Map<String,Object>>productAndEventArr=new ArrayList<>();
        if(products.isEmpty()){
            response.put("productFlag", false);
        }else{
            response.put("productFlag", true);
            //상품별 이벤트 조회
            for(productVo vo:products){
                Map<String,Object>productAndEvent=new HashMap<>();
                productAndEvent.put("product", vo);
                if(vo.getEventFlag()==1){
                    productAndEvent.put("event", productService.getProductEvent(vo.getId()));
                }
                productAndEventArr.add(productAndEvent);
            }
            response.put("productAndEvents", productAndEventArr);
        }
        return response;
    }*/

    public List<Map<String,Object>> getFlyerArr(int storeId,String startDate,String endDate,int page) {
        Map<String,Object>result=utillService.checkRequestDate(startDate, endDate);
        if((boolean)result.get("flag")){
            Timestamp start=Timestamp.valueOf(result.get("start").toString());
            Timestamp end=Timestamp.valueOf(result.get("end").toString());
            return flyerDao.findByDay(start, end, storeId,start, end, storeId,utillService.getStart(page, pageSize)-1,pageSize);

        }
        return flyerDao.findByStoreId(storeId,storeId,utillService.getStart(page, pageSize)-1,pageSize);
    }
    public flyerVo getFlyer(int flyerId) {
        return flyerDao.findById(flyerId).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 전단입니다", "getFlyer"));
    }
    private int insert(String imgPath,int storeId) {
        flyerVo vo=flyerVo.builder().img_path(imgPath).defaultSelect(0).storeId(storeId).companyId(utillService.getLoginId()).build();
        flyerDao.save(vo);
        return vo.getId();
    }
    public JSONObject ocrAndUpload(MultipartHttpServletRequest request,int storeId) {
        JSONObject response=new JSONObject();
        File file=fileService.convert(request.getFile("upload"));
        //aws upload
        response=fileService.upload(file);
        //글자추출
        try {
            response.put("ocr",ocrService.detectText(file.toPath().toString()));
        } catch (Exception e) {
            utillService.writeLog("ocr 글자 추출 실패",flyerService.class);
            e.printStackTrace();
        }
        //로컬 파일 삭제
        file.delete();
        //전단지 insert 
        response.put("id",insert(response.get("message").toString(),storeId));
        return response;
    }
}
