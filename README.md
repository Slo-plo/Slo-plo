# Slo-plo
서울여자대학교 바롬종합설계프로젝트 16분반 Slo-plo(슬로플로)조의 레포지토리입니다.

<br>

## Development Flow
<br>

1. **Issue 작성**
2. **브랜치 생성**
3. **작업**
4. **PR 작성**
5. **Merge**

<br>

## Convention
<br>

### Commit Convention

커밋 메시지는 `타입: 설명 #이슈 넘버`의 형식을 갖추어 작성합니다.

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

```Examples
1. 이미지 및 아이콘:
   - `ic_login_button`: 로그인 화면에서 사용되는 로그인 버튼 아이콘
   - `bg_home_background`: 홈 화면의 배경 이미지
   - `btn_submit_login`: 로그인 화면에서 제출 버튼

2. 문자열:
   - `txt_login_error_message`: 로그인 화면에서 오류 메시지 텍스트
   - `txt_home_welcome_message`: 홈 화면에서 환영 메시지 텍스트
   - `btn_submit_text`: 버튼에 표시될 텍스트

3. 레이아웃:
   - `layout_activity_login`: 로그인 화면에 해당하는 레이아웃 파일
   - `layout_fragment_home`: 홈 화면에 해당하는 프래그먼트 레이아웃 파일
```

#### 2. Common Resources
여러 화면에서 공통으로 사용되는 리소스는 화면명을 제외하고 기능이나 용도에 맞게 네이밍합니다.

```Examples
1. 이미지 및 아이콘:
   - `ic_close_button`: 닫기 버튼 아이콘
   - `ic_search_icon`: 검색 아이콘
   - `bg_loading`: 로딩 화면 배경 이미지

2. 문자열:
   - `txt_error_message`: 오류 메시지 텍스트
   - `txt_empty_list`: 비어 있는 리스트를 나타내는 텍스트
   - `txt_loading_message`: 로딩 중 메시지 텍스트

3. 색상:
   - `color_primary`: 기본 색상
   - `color_secondary`: 보조 색상
   - `color_text_primary`: 주 텍스트 색상
```
