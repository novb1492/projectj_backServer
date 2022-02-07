package com.kimcompay.projectjb.delivery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.delivery.model.deliverRoomDetailVo;
import com.kimcompay.projectjb.enums.intenums;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.users.principalDetails;
import com.kimcompay.projectjb.users.company.storeService;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


@Service
public class deliverPostionHandler extends TextWebSocketHandler {
    private Logger logger=LoggerFactory.getLogger(deliverPostionHandler.class);
    Map<Integer, List<Map<String,Object>>> roomList =new HashMap<>(); //웹소켓 세션을 담아둘 리스트 ---roomListSessions
   @Autowired
   private storeService storeService;
   @Autowired
   private deliveryService deliveryService;


    @Override//메세지가오는함수
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
      logger.info("handleTextMessage");
      logger.info(message.toString());
      logger.info(session.toString());
      JSONObject xAndYAndRoom=utillService.stringToJson(message.getPayload());
      int roomId=Integer.parseInt(xAndYAndRoom.get("roomId").toString());
      System.out.println(xAndYAndRoom);
      //배달방번호 조회
      try {
            for(Map<String,Object>room:roomList.get(roomId)){
               try {
                  //보내기만 하면됨 n번방 세션 들 다꺼내기
                  WebSocketSession wss = (WebSocketSession) room.get("session");           
                  wss.sendMessage(new TextMessage(xAndYAndRoom.toJSONString()));
               } catch (Exception e) {
                                 
               }
            }
         
      } catch (NullPointerException e) {
         logger.info("만들어진 방이 없습니다");
      }
      
   }
   @Override//연결이되면 자동으로 작동하는함수
   public void afterConnectionEstablished(WebSocketSession session) throws Exception {
      logger.info("afterConnectionEstablished");
      //로그인 상세정보 꺼내기
      Map<Object,Object>infor=getLoginInfor(session);
      String role=infor.get("role").toString();
      int id=Integer.parseInt(infor.get("id").toString());
      logger.info("deliverPostionHandler 웹소켓요청 사용자아이디: "+id);
      //권한에 따라 
      if(role.equals(senums.company_role.get())){
         logger.info("회사이용자");
         try {
            //배달 요청이 있는지 검사 로직 추가해야함
         } catch (NullPointerException e) {
            //주문 요청이 한건도 없다면 예외발생
            throw utillService.makeRuntimeEX("상점: "+id+" 배달목록 존재하지 않음", "afterConnectionEstablished");
         }
      }else if(role.equals(senums.user_role.get())){
         logger.info("일반이용자");
         actionAtUser(session, id);
      }   
   }
   private Map<Object,Object> getLoginInfor(WebSocketSession session) {
      logger.info("getLoginInfor");
      AbstractAuthenticationToken principal=(AbstractAuthenticationToken) session.getPrincipal();
      principalDetails  principalDetails=(com.kimcompay.projectjb.users.principalDetails) principal.getPrincipal();
      return principalDetails.getPrinci();
   }
   @Override //연결이끊기면 자동으로 작동하는함수
   public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
      logger.info("afterConnectionClosed");
      //회사가 배달완료를 누르면 =배열전체삭제
      //roomList.get(1).clear();//예제코드
      //유저가 퇴장하면 현재 가지고있던 배열에서 자기 제거 내일 구현해보자
   }
   public void actionAtUser(WebSocketSession session,int id) {
      logger.info("actionAtUser");
      //int roomId=0;//상점에서준 방번호 꺼내기 로직 추가해야함
         List<Integer>roomIds=deliveryService.selectRoomIdByUserIdAndFlag(id,intenums.NOT_FLAG.get());
         if(utillService.checkEmthy(roomIds)){
            throw utillService.makeRuntimeEX("배달이 존재하지 않습니다", "checkUser");
         }
         for(int roomId:roomIds){
            logger.info("조회배달방번호: "+roomId);
            List<Map<String,Object>>room=new ArrayList<>();
            boolean findFlag=false;
            //배달번호로 된 방이 있나검사
            try {
               room=roomList.get(roomId);
               for(Map<String,Object>rd:room){
                  if(rd.get("userId").equals(id)){
                     //방이 있고 기존유저 소켓정보 수정
                     logger.info("소켓 세션 변경");
                     rd.put("sessionId", session.getId());
                     rd.put("session", session);
                     findFlag=true;
                     break;
                  }
               }
               //방이 이미 있고 새유저가 왔을때
               if(!findFlag){
                  logger.info("roomId번방에 새유저 추가");
                  room.add(makeRoomDetail(session, roomId, id));
               }
            } catch (NullPointerException e) {
               //배달번호 관계없이 방이 하나도 없을경우만듬
               logger.info("첫룸생성");
               room=new ArrayList<>();
               room.add(makeRoomDetail(session, roomId, id));
               roomList.put(roomId, room);
            }
         } 
   }
   public Map<String,Object> makeRoomDetail(WebSocketSession session,int roomId,int userId) {
      logger.info("makeRoomDetail");
      Map<String,Object>roomDetail=new HashMap<>();
      roomDetail.put("roomNumber", roomId);
      roomDetail.put("userId",userId);
      roomDetail.put("sessionId", session.getId());
      roomDetail.put("session", session);
      return roomDetail;
   }
}