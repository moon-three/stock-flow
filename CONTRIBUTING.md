# Git Workflow (개인 프로젝트)

## 1. 브랜치 전략
- `main` : 항상 안정된 코드
- `feature/<기능>` : 기능 개발/리팩토링용 브랜치
- 완료되면 바로 main에 PR 후 병합

## 2. 커밋 메시지 규칙
- feat: 새로운 기능 추가
- refactor: 코드 구조 개선
- chore: 빌드/설정 변경
- doc: 문서 작성/수정
- test: 테스트 작성/수정
- fix: 버그 수정

## 3. PR 작성
- [PULL_REQUEST_TEMPLATE.md](.github/PULL_REQUEST_TEMPLATE.md) 참고

## 4. 코드 스타일
**네이밍**
- 클래스: PascalCase
- 메서드/변수: camelCase
- 상수: SCREAMING_SNAKE_CASE (단어 사이 _ + 모두 대문자)

**주석**
- 클래스/메서드/중요 로직에는 주석 필수

**기타**
- 줄 길이: 80~120자
- 빈 줄: 클래스/메서드/논리 블록 사이 1~2줄
- 파일/폴더 구조: 기능별 구분