# 📝 Board 프로젝트 (Spring Boot + MyBatis + MariaDB)

## ✅ 기술 스택
- **Backend**: Spring Boot, MyBatis
- **Frontend**: HTML, CSS, JavaScript
- **Database**: MariaDB
- **빌드 도구**: Maven

## 🔧 주요 기능
- 게시글 목록/등록/수정/삭제
- 게시글 상세 조회
- 페이지네이션
- 댓글/대댓글
- (옵션) 검색 기능

## 🏗️ 시스템 구조
src/
├─ main/
│ ├─ java/com/hgc/package/
│ │ ├─ controller/
│ │ ├─ service/
│ │ ├─ mapper/
│ │ └─ domain/
│ └─ resources/
│ ├─ templates/ # HTML
│ ├─ static/ # CSS, JS
│ └─ application.properties
└─ test/
