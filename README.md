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
병합시도/병합완료  

2021-12-21  
1.정부 api연동성공  
-okhttp3이용 
2.json패키지 교체  
-import org.json.simple.JSONObject  
이게 짱이다  
3.회원가입 유효성검사로직 확인  
-다시 유효성 검사로직을 확인 및 추가  
병합시도/병합성공
4.표기법 변경  
-언더바에서 카멜표기법으로 변경  
지금 써온건 어쩔 수없고 변경예정  
로그인/회원가입은 여기까지 구현  
추가로 나중에 테스트해보고 더 보충하자  
병합시도/병합성공  
5.로그인시 리프레시토큰 redis저장  
-토큰재발급을 위해  
6.토큰 만료시 처리 구현  
-리프레시찾아 확인 후 재발급  
병합시도/병합성공  

store브랜치 생성  

2021-12-22  
1.회원가입시 이메일 전송  
-비동기로 구현  

store전에  아이디찾기/비밀번호찾기/로그아웃구현하고 가자  
user other branch 생성  

2.이메일/비밀번호찾기 구축  
-비밀번호 변경요청시 알림 구축  
병합시도/병합성공  
병합시도/병합성공  
실수로 main에서 작업했네...

2021-12-24  
1.페이지 토큰검증 구축  
main에서 브랜치 땡겨오기 성공  
2.로그아웃 구현  
-쿠키/redis삭제  
3.로그인 도메인분리/인증도메인 문자 auth로수정  
-진작 이렇게 분리할껄 api->auth로 수정   
앞으로도 도메인 분리/수정이 일어날예정  
병합시도/병합성공  
4.아이디중복검사 구현  

이제 프론트 디자인 다시 한번 보고
store로 가자 
병합시도/병합성공  

5.소셜로그인 구현깜빡했네..

2021-12-25  
1.소셜로그인 구현중   
-에러메세지 재가공/response직접 호출하기등  
깜빡한게 있다 나이검사를 해야한다  

2021-12-26  
1.로그인시 로그인날짜 갱신  
-비동기처리  
2.로그인시 로그인 기간체크 (1년)
-추후에 잠금해제 페이지를 만들예정  
3.회원가입시 나이 계산
-추후에 18세 미만일시 부모님 동의 얻기 만들어야 한다  
사실상 현재 시스템은 의미가 없음 
나중에 나이스 api를 활용해 재구축 해야함  
4.카카오/네이버 로그인 로직 구축완료 
-사업자등록없는 테스트모드여서 못받는 정보들이 좀있다  
소셜로그인시에도 이메일로 알림  
병합시도/병합성공  
5.소스정리
-공통 로그인 소스들 utll로 묶음  
병합시도/병합성공  

2021-12-27  
1.기업회원 회원가입방식변경  
-기존 사업자+상호 같이회원가입  
변경 사업자로 회원가입 후 가맹점 따로 가입 
2.버그 fix
-도대체 다음 단계를 나갈수가 없네 ㅋㅋ  
3.기업 회원가입 형식 변경  
병합시도/병합성공  

가맹점 등록 구축위해 company 브랜치 생성  

2021-12-28
1.가맹정 등록 구축시작  
2.로그인 체크시 이메일+권한까지 던져주기  
  
2021-12-29  
1.이미지 업로드구축 시작
-s3연동시작 
2.업로드 형식이 썸네일이라면 이전 사진 교체  
-일찍 구축한 감이 있다 가게등록이 끝나면 한꺼번에 비동기 요청으로 처리하는게 더 좋을듯하다   

2021-12-31  
1.사업자 번호와 분점은 휴대폰 번호가 달라도된다  
-휴대폰 인증/확인 시스템 재구축 해야함  
이메일/휴대폰 분리해야할듯  
그냥 파라미터값만 바꿔주면 해결되는 문제였다  
2.많은 도메인이 생길거 같아서.. 컨트롤러 분리해야겠다  
3.일단 멈추고 소스정리  
병합시도/
