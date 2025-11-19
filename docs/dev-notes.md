# 🗒️ Dev Notes

## 🐞 Issue 1: JpaConfig 추가 시 빌드/테스트 오류 발생 문제 (contextLoads 실패)
- **문제:** stock-online 모듈에 JpaConfig (@EntityScan) 추가 -> 빌드/테스트 실패
- **원인:** @EntityScan 적용 시 EntityManagerFactory 생성 -> 그 과정에서 DB 연결 필요 -> DB 없으면 contextLoads() 실패
- **해결:** 테스트 시 H2 인메모리 DB 사용, 실제 실행은 MySQL
- **👍🏻장점:** 테스트와 실제 DB 분리, 테스트 빠르고 외부 DB 의존성 없음

## 🐞 Issue 2: 환경변수(.env) 설정 후 DB 테이블 생성 실패 문제
- **문제:** @Entity 작성 후 컨테이너를 실행했지만 DB에 테이블 생성 실패
- **원인:** 애플리케이션 실행 시 mysql_password 환경변수가 전달되지 않음 -> DB 접속 시 비밀번호를 몰라 DB 연결 실패
- **해결:** docker-compose.yml에서 애플리케이션(stock-online-app)에 환경변수 추가
- **💡깨달음/Tip:** 애플리케이션 실행 시 환경변수를 반드시 넘겨야 properties를 읽고 DB 연결이 가능
- **👍🏻장점:** 환경변수 사용으로 민감 정보 보호

## 🐞 Issue 3: Docker 컨테이너 실행 시 애플리케이션(stock-online-app) 연결 실패 문제
- **문제:** DB 컨테이너가 준비된 후에도 애플리케이션(stock-online-app) 컨테이너 실행 실패 
- **원인:** depends_on 설정만으로는 DB가 완전히 준비되었는지 보장되지 않아 앱이 먼저 시작될 수 있음
- **해결:** DB 컨테이너의 healthy 상태 확인 후 애플리케이션 컨테이너 실행
- **💡깨달음/Tip:** depends on 설정만으로는 충분하지 않고 healthy check 필요
- **👍🏻장점:** 안정적인 컨테이너 연결 가능
