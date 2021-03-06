package com.kimcompay.projectjb.payments.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import javax.print.DocFlavor.STRING;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.kakao.kakaoMapService;
import com.kimcompay.projectjb.apis.kakao.kakaoPayService;
import com.kimcompay.projectjb.apis.settle.settleService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.payments.model.basket.basketDao;
import com.kimcompay.projectjb.payments.model.coupon.couponVo;
import com.kimcompay.projectjb.payments.model.order.orderDao;
import com.kimcompay.projectjb.payments.model.order.orderVo;
import com.kimcompay.projectjb.payments.model.pay.paymentDao;
import com.kimcompay.projectjb.payments.model.pay.paymentVo;
import com.kimcompay.projectjb.payments.model.pay.tryOrderDto;
import com.kimcompay.projectjb.users.company.model.products.productVo;
import com.kimcompay.projectjb.users.company.model.stores.storeVo;
import com.kimcompay.projectjb.users.company.service.productService;
import com.kimcompay.projectjb.users.company.service.storeService;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class paymentService {
    private final int pageSize=2;
    @Autowired
    private basketService basketService;
    @Autowired
    private kakaoMapService kakaoMapService;
    @Autowired
    private storeService storeService;
    @Autowired
    private couponService couponService;
    @Autowired
    private productService productService;
    @Autowired
    private paymentDao paymentDao;
    @Autowired
    private settleService settleService;
    @Autowired
    private kakaoPayService kakaoPayService;
    @Autowired
    private RedisTemplate<String,Object>redisTemplate;
    @Autowired
    private orderService orderService;

    public List<Map<String,Object>> getPaymentAndOrdersUseDeliver(String mchtTrdNo,int storeId) {
        return paymentDao.findByMchtTrdNoAndStoreIdJoinOrders(storeId, mchtTrdNo);
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject cancleByStore(int orderId,int storeId) {
        Map<String,Object>orderAndPayments=new HashMap<>();
        orderAndPayments=paymentDao.findByJoinCardVbankKpayAndPayment(orderId, storeId);
        int cancleAllFlag=Integer.parseInt(orderAndPayments.get("cancle_all_flag").toString());
        int totalPrice=Integer.parseInt(orderAndPayments.get("payment_total_price").toString());
        int minusPrice=Integer.parseInt(orderAndPayments.get("order_price").toString());
        int orderCancleFlag=Integer.parseInt(orderAndPayments.get("oder_cancle_flag").toString());
        totalPrice=totalPrice-minusPrice;
        if(orderAndPayments.isEmpty()){
            throw utillService.makeRuntimeEX("????????? ???????????? ????????????", "cancleByStore");
        }else if(cancleAllFlag==1||totalPrice<=0||orderCancleFlag==1){
            throw utillService.makeRuntimeEX("?????? ?????? ????????? ???????????????", "cancleByStore");
        }else if(totalPrice<0){
            throw utillService.makeRuntimeEX("?????? ???????????? ??????????????? ????????? \n????????????: "+(totalPrice+minusPrice)+"\n????????????: "+minusPrice, "cancleByStore");
        }
        //db??????
        int cancleTime=Integer.parseInt(orderAndPayments.get("cncl_ord").toString())+1;
        String mchtTrdNo=orderAndPayments.get("order_mcht_trd_no").toString();
        updatePirceAndCancleTime(totalPrice, cancleTime, mchtTrdNo);
        orderService.changeStateById(orderId, 1);
        //pg?????????
        String method=orderAndPayments.get("method").toString();
        String  message="";
        if(method.equals(senums.cardText.get())||method.equals(senums.vbankText.get())){
            message=settleService.cancleByStore(orderAndPayments, method);
        }else if(method.equals(senums.kpayText.get())){
            kakaoPayService.cancleKpay(orderAndPayments.get("tid").toString(), minusPrice, 0);
            message="???????????????????????????";
        }else{
            throw utillService.makeRuntimeEX("???????????? ?????? ?????? ???????????????", "cancleByStore");
        }
        return utillService.getJson(true, message);
    }
    public void updatePirceAndCancleTime(int price,int cancleTime,String mchtTrdNo) {
        if(price<=0){
            paymentDao.updatePriceAndCancleTimeZero(cancleTime, price,1,mchtTrdNo);
        }else{
            paymentDao.updatePriceAndCancleTime(cancleTime, price, mchtTrdNo);
        }
    }
    public JSONObject getPaymentsByStoreId(int page,String start,String end,int storeId) {
        List<Map<String,Object>>paymentVos=getVos(page, start, end, storeId);
        utillService.checkDaoResult(paymentVos, "????????? ???????????? ????????????", "getPaymentsByStoreId");
        System.out.println(paymentVos.toString());
        int totalPage=utillService.getTotalPage(Integer.parseInt(paymentVos.get(0).get("totalCount").toString()), pageSize); 
        JSONObject response=new JSONObject();
        response.put("flag", true);
        response.put("message", paymentVos);
        response.put("totalPage", totalPage);
        return response;
    }
    private List<Map<String,Object>> getVos(int page,String start,String end,int storeId) {
        return paymentDao.findJoinByStoreId(storeId,storeId,utillService.getStart(page, pageSize)-1, pageSize);
    }
    public JSONObject getPayments(int page,String start,String end) {
        return utillService.getJson(true, getDtosByUserId(page, start, end));
    }
    private List<Map<String,Object>> getDtosByUserId(int page,String start,String end) {
        return paymentDao.findJoinCardVbankKpayOrder(utillService.getLoginId());
    }
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public JSONObject tryOrder(tryOrderDto tryOrderDto,String action) {
        int userId=utillService.getLoginId();
        //???????????? ????????????
        List<Map<String,Object>>basketAndProducts=new ArrayList<>();
        if(action.equals("all")){
            basketAndProducts=basketService.getBasketsAndProduct(userId);
        }else if(action.equals("choice")){
            List<Map<String,Object>>baskets=tryOrderDto.getCoupons();
            for(Map<String,Object>basket:baskets){
                int basketId=Integer.parseInt(basket.get("id").toString());
                Map<String,Object>basketVo=basketService.getBasketAndProductByBasketId(basketId);
                if(basketVo.isEmpty()){
                    throw utillService.makeRuntimeEX("???????????? ?????? ????????????????????? \n??????: "+basketId, "tryOrder");
                }else if(Integer.parseInt(basketVo.get("user_id").toString())!=userId){
                    throw utillService.makeRuntimeEX("????????? ??????????????? ???????????? \n??????: "+basketId, "tryOrder");
                }
                basketAndProducts.add(basketVo);
            }
        }else{
            throw utillService.makeRuntimeEX("????????? ???????????? ????????????????????? \n??????????????? ?????? ????????????", "tryOrder");
        }
        //???????????? ?????????
        Map<Integer,List<Map<String,Object>>>divisionByStoreIds=divisionByStoreId(basketAndProducts);
        //?????? ????????????
        Map<String,Object>ordersAndPayment=confrimByStore(divisionByStoreIds,tryOrderDto);
        //insert??????
        paymentVo paymentVo=(paymentVo)ordersAndPayment.get("payment");
        List<orderVo>orders=(List<orderVo>)ordersAndPayment.get("orders");
        redisTemplate.opsForHash().put(paymentVo.getMchtTrdNo(),paymentVo.getMchtTrdNo(), paymentVo);
        redisTemplate.opsForHash().put(paymentVo.getMchtTrdNo()+senums.basketsTextReids.get(),paymentVo.getMchtTrdNo()+senums.basketsTextReids.get(), orders);
       /* LinkedHashMap<String,Object> paymentVo2=(LinkedHashMap)redisTemplate.opsForHash().entries(paymentVo.getMchtTrdNo());
        LinkedHashMap<String,Object> orders2=(LinkedHashMap)redisTemplate.opsForHash().entries(paymentVo.getMchtTrdNo()+senums.basketsTextReids.get());
        System.out.println(paymentVo2.toString());
        System.out.println(orders2.toString());
        /*for(orderVo order:orders){
            String basketId=Integer.toString(order.getBasketId());
            redisTemplate.opsForHash().put(basketId,basketId, order);
        }*/
        //????????? ???????????? ??? ??? ???????????? ??????
        if(tryOrderDto.getPayKind().equals("kpay")){
            int totalCount=Integer.parseInt(ordersAndPayment.get("totalCount").toString());
            return kakaoPayService.requestPay(ordersAndPayment.get("productNames").toString(), paymentVo, orders, totalCount);
        }
        String mchtId="nxca_jt_il";
        //vbank??? ??????
        String expire=null;
        if(paymentVo.getMethod().equals("vbank")){
            mchtId="nx_mid_il";
            expire=LocalDateTime.now().plusMinutes(15).toString().replaceAll("[:,T,-]", "").substring(0, 14);
        }
        return settleService.makeRequestPayInfor(ordersAndPayment.get("productNames").toString(),paymentVo,orders,mchtId,expire);
    }
    private Map<String,Object> confrimByStore(Map<Integer,List<Map<String,Object>>>divisionByStoreIds,tryOrderDto tryOrderDto) {
        int userId=utillService.getLoginId();
        String productNames="";
        List<orderVo>orderVos=new ArrayList<>();
        Map<String,Object>orderAndPayment=new HashMap<>();
        //?????? ????????????
        Map<Integer,List<couponVo>>coupons=confrimCoupone(tryOrderDto.getCoupons());
        //???????????? ????????? ????????????
        int realTotalPrice=0;
        int totalCount=0;
        //?????????????????? ??????
        String mchtTrdNo=getMchtTrdNo();
        //???????????? ?????????
        for(Entry<Integer, List<Map<String, Object>>> divisionByStoreId:divisionByStoreIds.entrySet()){
            int storeId=divisionByStoreId.getKey();
            storeVo storeVo=storeService.getVo(storeId);
            //?????? ????????? ?????? 0????????? ?????????
            int totalPrice=0;
            LocalDateTime now=LocalDateTime.now();
            String nows=now.toString().split("T")[0];
            Timestamp openTime=Timestamp.valueOf(nows+" "+storeVo.getOpenTime()+":00");
            Timestamp closeTime=Timestamp.valueOf(nows+" "+storeVo.getCloseTime()+":00");
            System.out.println("????????????: "+openTime);
            //???????????? ?????? ??????,???????????? ??????   
            if(checkDeliverRadius(storeVo.getSaddress(),storeVo.getDeliverRadius(),tryOrderDto.getAddress())){
                throw utillService.makeRuntimeEX("?????? ??????????????? ???????????????\n????????????:"+storeVo.getSname()+"\n????????????:"+divisionByStoreId.getValue().get(0).get("product_name")+"\n????????????:"+storeVo.getDeliverRadius()+"km", "checkDeliverRadius");
            }else if(LocalDateTime.now().isAfter(closeTime.toLocalDateTime())||LocalDateTime.now().isBefore(openTime.toLocalDateTime())){
                throw utillService.makeRuntimeEX("???????????? ????????? ????????????\n????????????:"+storeVo.getSname()+"\n????????????:"+divisionByStoreId.getValue().get(0).get("product_name")+"\n????????????:"+storeVo.getDeliverRadius()+"km", "checkDeliverRadius");
            }
            //System.out.println("storeId"+storeId);
            //?????? ?????? ???????????? ???????????? ??? ????????????
            List<Map<String, Object>>basketAndProducts=divisionByStoreId.getValue(); 
            for(Map<String,Object>basketAndProduct:basketAndProducts){ 
                int basketId=Integer.parseInt(basketAndProduct.get("basket_id").toString());
                int productId=Integer.parseInt(basketAndProduct.get("product_id").toString());
                int count=Integer.parseInt(basketAndProduct.get("basket_count").toString());
                int price=0;
                //????????? ????????? ??????????????????
                productVo productVo=(productVo)productService.getProduct(productId).get("message");
                price=productVo.getPrice();
                //?????? ?????? ?????? ???????????? ????????? ?????? ???????????? ?????? 
                totalPrice+=price*count;
                totalCount+=count;
                //???????????? ????????? ??????
                String couponName=null;
                if(coupons.get(basketId)!=null){
                    List<couponVo>counponInfors=coupons.get(basketId);
                    //?????? ?????? ??? ?????? ?????? ??????
                    if(counponInfors.size()>count){
                        throw utillService.makeRuntimeEX("??????????????? ????????? ??????????????? \n????????????: "+productVo.getProductName(), "confrimByStore");
                    }
                    List<Integer>discountPrices=new ArrayList<>();
                    couponName="";
                    for(couponVo couponVo:counponInfors){
                        price=productVo.getPrice();
                        //?????? ?????? ?????? ??????
                        if(couponVo.getStoreId()!=storeId){
                            throw utillService.makeRuntimeEX("?????? ?????? ?????? ????????? ???????????? \n????????????:"+couponVo.getName(), "confrimByStore");
                        }
                        int action=couponVo.getKind();
                        int discountNum=couponVo.getNum();
                        //????????? enum?????? ??????
                        if(action==0){
                            price-=discountNum;
                        }else if(action==1){
                            System.out.println("discountNum:"+discountNum);
                            int totalDiscount=(int)((int)productVo.getPrice()*(0.01*discountNum));
                            System.out.println("discountNum:"+discountNum);
                            price-=totalDiscount;
                        }
                        //0?????? ????????? ?????? ???????????? 
                        if(price<=0){
                            price=100;
                        }
                        couponName=couponName+couponVo.getName()+",";
                        //System.out.println("discountprice: "+price);
                        discountPrices.add(price);
                    }
                    //?????? ??????
                    price=productVo.getPrice();
                    //System.out.println("productCount: "+count);
                    //System.out.println("discountPrices: "+discountPrices);
                    price=price*(count-discountPrices.size());//?????? ?????? ???????????? ????????????
                    //System.out.println("originprice: "+price);
                    for(int discountPrice:discountPrices){//?????? ?????? ????????? ????????????
                        System.out.println("discountPrice"+discountPrice);
                        price+=discountPrice;
                    }
                }else{
                    price*=count;
                }
                //System.out.println("basketId:"+basketId);
                //System.out.println(price);
                orderVo vo=orderVo.builder().cancleFlag(0).mchtTrdNo(mchtTrdNo).coupon(couponName).price(price).productId(productVo.getId())
                            .basketId(basketId).count(count).storeId(storeId).userId(userId).build();
                //System.out.println(vo.toString());
                realTotalPrice+=price;
                orderVos.add(vo);
                productNames=productNames+productVo.getProductName()+",";
            }
            //?????? ??????
            if(storeVo.getMinPrice()>totalPrice){
                throw utillService.makeRuntimeEX("?????? ?????? ???????????? ??????????????? \n ????????????: "+storeVo.getSname(),"confrimByStore");
            }
        }
        //????????????,????????? ?????? ?????? ??????
        String method=tryOrderDto.getPayKind();
        checkPayKind(method);
        String soldOutAction=tryOrderDto.getSoldOut();
        checkSoldOutAction(soldOutAction);
        paymentVo vo=paymentVo.builder().cancleAllFlag(0).cnclOrd(0).mchtTrdNo(mchtTrdNo).method(method).soldOurAction(soldOutAction)
                    .totalPrice(realTotalPrice).userId(userId).address(tryOrderDto.getAddress()).postcode(tryOrderDto.getPostcode()).detailAddress(tryOrderDto.getDetailAddress()).build();
        //System.out.println(vo.toString());
        orderAndPayment.put("totalCount", totalCount);
        orderAndPayment.put("orders", orderVos);
        orderAndPayment.put("payment", vo);
        orderAndPayment.put("productNames", productNames);
        //System.out.println(orderAndPayment.toString());
        return orderAndPayment;
    }
    private void checkSoldOutAction(String soldOutAction) {
        if(!soldOutAction.equals("replace")&&!soldOutAction.equals("cancle")&&!soldOutAction.equals("contact")){
            throw utillService.makeRuntimeEX("???????????? ?????? ????????? ???????????????", "checkSoldOutAction");
        }
    }
    private void checkPayKind(String payKind) {
        if(!payKind.equals("card")&&!payKind.equals("vbank")&&!payKind.equals("kpay")){
            throw utillService.makeRuntimeEX("???????????? ?????? ?????? ???????????????", "checkPayKind");
        }
    }
    private String getMchtTrdNo() {
        //?????? ???????????? ???????????????
        while(true){
            String mchtTrdNo=utillService.getRandomNum(10);
            if(!paymentDao.existsByMchtTrdNo(mchtTrdNo)){
                return  mchtTrdNo;
            }
        }
    }
    private Map<Integer,List<couponVo>> confrimCoupone(List<Map<String,Object>>coupons) {
        Map<Integer,List<couponVo>>couponInfors=new HashMap<>();
        Map<String,String>conponNames=new HashMap<>();
        for(Map<String,Object>coupon:coupons){
            //null????????? ??????
            if(Optional.ofNullable(coupon.get("coupon")).orElseGet(()->null)==null){
                continue;
            }
            // , ??? ?????? ?????? ???????????? ??????
            String[] couponSplit=coupon.get("coupon").toString().split(",");
            List<couponVo>couponAll=new ArrayList<>();
            for(String couponName:couponSplit){
                if(conponNames.containsValue(couponName)){
                    throw utillService.makeRuntimeEX("?????? ?????? ??????:"+couponName, "confrimByStore");
                }
                couponVo couponVo=couponService.CheckAndGet(couponName);
                couponAll.add(couponVo);
                conponNames.put(couponName, couponName);
            }
            couponInfors.put(Integer.parseInt(coupon.get("id").toString()),couponAll);
        }
        //System.out.println("--------------------");
        //System.out.println(couponInfors.toString());
        return couponInfors;
    }
    private Map<Integer,List<Map<String,Object>>> divisionByStoreId(List<Map<String,Object>>basketAndProducts) {
        Map<Integer,List<Map<String,Object>>>divisionByStoreId=new HashMap<>();
        for(Map<String,Object>basket:basketAndProducts){
            //System.out.println(basket.get("product_id"));
            int storeId=Integer.parseInt(basket.get("store_id").toString());
            //System.out.println("s:"+storeId);
            if(storeId==0){
                throw utillService.makeRuntimeEX("????????? ????????????????????? \n"+basket.get("product_name"), "divisionByStoreId");
            }else if(divisionByStoreId.containsKey(storeId)){
                divisionByStoreId.get(storeId).add(basket);
            }else{
                List<Map<String,Object>>basketAndProduct=new ArrayList<>();
                basketAndProduct.add(basket);
                divisionByStoreId.put(storeId, basketAndProduct);
            }
        }
        return divisionByStoreId;
    }
    public boolean checkDeliverRadius(String storeAddress,double storeRadius,String userAddress) {
        double result=getWay(storeAddress,userAddress);
        System.out.println(result);
        if(storeRadius<result){
            return true;
        }
        return false;
    }
    private double getWay(String storeAddress,String userAddress) {
        List<LinkedHashMap<String,Object>> userAddressInfor=kakaoMapService.checkAddress(userAddress);
        List<LinkedHashMap<String,Object>> storeAddressInfor=kakaoMapService.checkAddress(storeAddress);
        double userLat=Double.parseDouble(userAddressInfor.get(0).get("y").toString());
        double userLon=Double.parseDouble(userAddressInfor.get(0).get("x").toString());
        double storeLat=Double.parseDouble(storeAddressInfor.get(0).get("y").toString());
        double storeLon=Double.parseDouble(storeAddressInfor.get(0).get("x").toString());
        return utillService.distance(userLat, userLon, storeLat, storeLon); 
    }
    
}
