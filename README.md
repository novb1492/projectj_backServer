# projectj_backServer

2021-12-14  
1.프로젝트 생성  

2021-12-15  
1.sns서비스구축중
-이메일/핸드폰,용도에 맞게분류
2.전송로직 구축시작  
-원래는 다른 서버를 만들려 했으나  
시간관계상 모놀리식으로 구현으로 변경  
병합시도/병합성공
3.sqs 붙히기 성공  
4.이메일/문자전송 구축  
-문자/이메일구축 성공  
병합시도/병합완료
5.문자/이메일 인증 구축  
-인증완료수 세션소멸,완료 세션생성  
6.회원가입 준비  
-사업자 등록 조회 api가 필요함  

2021-12-16  
1.사업자등록 api붙히기 
-계속 등록되지 않은 인증키 입니다 라고해서 문의 넣어논 상태
2.ec2 mysql설치후 연동 성공
-spring.datasource.url=jdbc:mysql://ip주소:3306/db이름  
모든 설정을 다해도 안되면 꼭 sudo systemctl restart mysql.service
이용해 mysql 재시작 해줘야한다  
이제 mysql 공유가 가능하다!!      
3.회원가입 유효성 검사 체크중  
-다른 프로젝트와 다르게 카카오맵 api로 주소까지 검증예정  
병합시도/병합성공

2021-12-17  
1.카카오 주소 api로 검증  
2.인증한 휴대폰/이메일인지 최종 검증  
병합시도/병합성공  
3.유효성검사 구축완료  
4.회원가입완료
-버그는 내일 테스트하면서 찾아보자  
병합시도/병합성공  

2021-12-18  
1.cors정책 변경  
-도메인 지정  
2.jwt제작시작
-로그인시 이제 db가 여러게 되었는데  
detailService를 이전과 다르게 짜야할거같다  
3.로그인 구현시작  
4.로그인 필터 구현중  
5.redis연동  

2021-12-19  
1.redis map으로 저장 성공
-HashOperations를 이용  
2.comvo/uservo를 합쳐야한다  
-map/인터페이스/어뎁터 패턴등으로 연구해보자  
어댑터 보다 map으로 하기로 결정 
details->map을 이용해 하나로 통합  
map으로 합쳤는데 일자가 timestamp숫자로 바뀌어서  
고민좀 해봐야겠다  
병합시도/병합성공

2021-12-20  
1.http->https로 변환  
-java keytools이용    
세션이슈때문에 다시  http로전환  
2.로그인 에러별 처리구현  
3.유저정보 주는 메소드구현  
-필터부터 구현했었어야하는데  
병합시도/병합성공  
인증필터 구현 branch 생성  
브랜치 생성완료  
4.redis=entries  
-맵전체  꺼내올수 있다  
5.userdetailService합치기
-redis에 들어갈때 공통값은 합쳐야한다
완벽한 어댑터 패턴은 아니지만 비스무리하게 만들어서 해결  
6.로그인 구축완료    

