# Slo-plo
서울여자대학교 바롬종합설계프로젝트 16분반 Slo-plo(슬로플로)조의 레포지토리입니다.

<br>

## Development Flow
<br>

### 1. Issue 작성
- 개발할 기능 또는 수정할 버그에 대해 이를 생성합니다.
- 이슈 템플릿에 따라 작성합니다.

### 2. develop 브랜치 최신화
- 새로운 브랜치를 만들기 전에 로컬 `develop` 브랜치를 최신 상태로 유지합니다.
- 모든 작업은 기능별 브랜치에서 진행하며, `develop` 브랜치에는 직접 `push`하지 않습니다.
```bash
git checkout develop
git fetch origin
git pull origin develop
```

### 3. Branch 생성
- 브랜치 네이밍 규칙에 따라 브랜치를 생성합니다.
- 이슈 내에서 create a branch를 통해서도 브랜치 생성이 가능합니다.
```bash
git checkout -b feature/{issue-number}-{feature-name}
```

### 4. 작업
- 기능을 개발하거나 버그를 수정합니다.
- 작업 도중의 커밋은 규칙에 따라 의미 있는 단위로 진행합니다.
```bash
git add .
git commit -m "feat: 홈 화면에 추가 버튼 구현 #1"
git push origin 브랜치명
```
### 5. PR(Pull Request) 작성
- PR 생성 후, 템플릿에 따라 PR을 작성하고 리뷰어를 지정합니다.
- PR 제목은 이슈 제목과 동일하게 작성합니다.

### 6. Merge
- 리뷰 승인 후 `develop` 브랜치에 머지합니다.
- 머지 후 브랜치도 삭제합니다.
<br>

## Convention
<br>

### Commit Convention

커밋 메시지는 `타입: 설명 #이슈 넘버`의 형식을 갖추어 **기능별로** 작성합니다.

| 타입      | 설명                           |
|-----------|--------------------------------|
| feat      | 새로운 기능 추가               |
| fix       | 버그 수정                      |
| refactor  | 코드 리팩토링                  |
| docs      | 문서 수정 (README 등)          |
| style     | 코드 스타일 변경 (세미콜론 추가 등)|
| chore     | 빌드 및 패키지 설정 변경       |
| test      | 테스트 코드 추가               |

#### Commit Example
```sh
git commit -m "feat: 로그인 기능 구현 #1"
git commit -m "fix: API 응답 오류 수정 #5"
```
<br>

### Branch Naming Convention

브랜치 이름은 `타입/이슈넘버-설명`의 형식을 갖추어 작성합니다.
- 기능 추가: `feature/{issue-number}-{feature-name}`
- 버그 수정: `bugfix/{issue-number}-{bug-description}`
- 핫픽스: `hotfix/{issue-number}-{critical-bug}`
- 릴리스: `release/{version-number}`

#### Branch Example
```sh
- feature/123-login
- bugfix/456-fix-login-api
```
<br>

### Resource Naming Convention

프로젝트에서 사용하는 리소스의 네이밍은 일관성 있게 관리되어야 합니다. 리소스 이름은 `전치사_화면_설명` 형식을 따르되, 공통 리소스는 화면을 제외하고 기능이나 용도에 맞게 작성합니다.

#### 1. Screen-Specific Resources
화면에 특화된 리소스는 전치사_화면_설명 형식을 사용하여 리소스가 사용되는 화면과 역할을 쉽게 파악할 수 있도록 합니다.

- 이미지 및 아이콘

   - `ic_login_button`: 로그인 화면에서 사용되는 로그인 버튼 아이콘
   - `bg_home_background`: 홈 화면의 배경 이미지
   - `btn_submit_login`: 로그인 화면에서 제출 버튼
 
- 문자열
  
   - `txt_login_error_message`: 로그인 화면에서 오류 메시지 텍스트
   - `txt_home_welcome_message`: 홈 화면에서 환영 메시지 텍스트
#### 2. Common Resources
여러 화면에서 공통으로 사용되는 리소스는 화면명을 제외하고 기능이나 용도에 맞게 네이밍합니다.

- 이미지 및 아이콘
  
   - `ic_close_button`: 닫기 버튼 아이콘
   - `bg_loading`: 로딩 화면 배경 이미지
     
- 문자열
  
   - `txt_error_message`: 오류 메시지 텍스트
   - `txt_loading_message`: 로딩 중 메시지 텍스트
     
- 색상
  
   - `color_primary`: 기본 색상
   - `color_secondary`: 보조 색상
   - `color_text_primary`: 주 텍스트 색상
