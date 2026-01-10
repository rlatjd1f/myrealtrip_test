# myrealtrip_test

소셜 플랫폼의 핵심 기능(포스트 작성/조회, 팔로우 관계, 홈 피드)을 구현한 Spring Boot 기반 API 서버입니다.
팔로우 관계를 기준으로 최신 포스트를 커서 기반으로 조회할 수 있도록 설계했습니다.

## 기술 스택
- Java 21
- Spring Boot 3.2.x
- Spring Data JPA
- Flyway
- H2
- Gradle

## 실행 방법
- 실행: `./gradlew bootRun`
- 테스트: `./gradlew test`

## 주요 API

### 사용자(User)
| HTTP Method | URL | 기능 |
| --- | --- | --- |
| POST | `/api/users` | 사용자 생성 |
| GET | `/api/users/{userId}` | 사용자 조회 |
| PUT | `/api/users/{userId}` | 사용자 수정 |
| DELETE | `/api/users/{userId}` | 사용자 삭제 |

### 포스트(Post)
| HTTP Method | URL | 기능 |
| --- | --- | --- |
| POST | `/api/posts` | 포스트 작성 |
| PUT | `/api/posts/{postId}` | 포스트 수정 |
| GET | `/api/posts/{postId}` | 포스트 조회 |

### 팔로우(Follow)
| HTTP Method | URL | 기능 |
| --- | --- | --- |
| POST | `/api/follows` | 팔로우 |
| DELETE | `/api/follows?followerId=&followeeId=` | 언팔로우 |

### 피드(Feed)
| HTTP Method | URL | 기능 |
| --- | --- | --- |
| GET | `/api/feed?followerId=&cursorId=&size=` | 홈 피드 조회 |

### 댓글(Comment)
| HTTP Method | URL | 기능 |
| --- | --- | --- |
| POST | `/api/comments` | 댓글 작성 |
| GET | `/api/comments?postId=&page=&size=` | 댓글 조회 |
| PUT | `/api/comments/{commentId}` | 댓글 수정 |
| DELETE | `/api/comments/{commentId}?userId=` | 댓글 삭제 |

## API 요청/응답 예시

### 사용자 생성
- 요청
```json
POST /api/users
{
  "name": "Alice"
}
```
- 응답
```json
{
  "status": 201,
  "code": "OK",
  "message": "성공",
  "data": {
    "id": 1,
    "name": "Alice",
    "createdAt": "2025-01-01T10:00:00"
  }
}
```

### 포스트 작성
- 요청
```json
POST /api/posts
{
  "userId": 1,
  "content": "첫 번째 포스트"
}
```
- 응답
```json
{
  "status": 201,
  "code": "OK",
  "message": "성공",
  "data": {
    "id": 10,
    "userId": 1,
    "content": "첫 번째 포스트",
    "createdAt": "2025-01-01T10:10:00",
    "updatedAt": "2025-01-01T10:10:00"
  }
}
```

### 팔로우
- 요청
```json
POST /api/follows
{
  "followerId": 1,
  "followeeId": 2
}
```
- 응답
```json
{
  "status": 201,
  "code": "OK",
  "message": "성공",
  "data": null
}
```

### 홈 피드 조회
- 요청
```json
GET /api/feed?followerId=1&cursorId=10&size=20
```
- 응답
```json
{
  "status": 200,
  "code": "OK",
  "message": "성공",
  "data": {
    "items": [
      {
        "postId": 9,
        "userId": 2,
        "content": "오늘의 글",
        "createdAt": "2025-01-01T09:50:00"
      }
    ],
    "nextCursor": 9,
    "size": 1
  }
}
```

### 댓글 수정
- 요청
```json
PUT /api/comments/5
{
  "userId": 1,
  "content": "댓글 수정"
}
```
- 응답
```json
{
  "status": 200,
  "code": "OK",
  "message": "성공",
  "data": {
    "id": 5,
    "postId": 10,
    "userId": 1,
    "content": "댓글 수정",
    "createdAt": "2025-01-01T10:12:00"
  }
}
```

### 에러 응답 예시
- 요청
```json
PUT /api/comments/5
{
  "userId": 2,
  "content": "댓글 수정"
}
```
- 응답
```json
{
  "status": 400,
  "code": "INVALID_REQUEST",
  "message": "요청이 올바르지 않습니다.",
  "data": {
    "fieldErrors": []
  }
}
```

## 도메인 모델 설계
| 도메인 | 설계 내용 | 관계 |
| --- | --- | --- |
| User | 사용자 식별은 `users.id`(PK)로, 표시용 이름은 `users.name`에 저장합니다. | User 1:N Post<br>User 1:N Comment |
| Post | `posts.user_id`(FK)로 작성자(User)와 연결되는 1:N 구조입니다.<br>수정 시 `posts.content` 컬럼만 갱신합니다. | Post N:1 User<br>Post 1:N Comment |
| Follow | 사용자 간 N:M 관계를 `follows(follower_id, followee_id)`로 분리합니다.<br>`(follower_id, followee_id)` 유니크로 중복 팔로우를 방지합니다. | User N:M User(Follow) |
| Comment | `comments.post_id`(FK), `comments.user_id`(FK)로 포스트/작성자를 연결합니다.<br>수정 시 `comments.content` 컬럼만 갱신합니다. | Comment N:1 Post<br>Comment N:1 User |

## User–Follow 관계 모델링
- Follow 엔티티(`follows`)로 사용자 간 N:M 관계를 분리합니다.
- 컬럼은 `follower_id`(팔로우하는 사용자)와 `followee_id`(팔로우 대상 사용자)로 구성됩니다.
- `(follower_id, followee_id)` 유니크 제약으로 중복 팔로우를 방지합니다.
- 자기 자신 팔로우는 서비스 로직과 DB 체크 제약(`follower_id <> followee_id`)으로 차단합니다.

## Feed 생성 전략
- fan-out on read 방식으로 요청 시점에 피드를 구성합니다.
- `follows`와 `posts`를 조인하여 팔로우한 사용자의 포스트만 조회합니다.
- 읽기 비용을 감수하는 대신 쓰기(포스트 생성)는 가볍게 유지합니다.

## 팔로우/언팔로우 처리 로직
- 팔로우 요청에서 자기 자신을 팔로우하는 경우 `400 INVALID_REQUEST`로 처리합니다.
- 이미 존재하는 관계는 `409 CONFLICT`로 처리합니다.
- 언팔로우는 관계가 없을 경우 `404 NOT_FOUND`, 존재하면 삭제합니다.

## Feed 로딩 시 정렬/페이징 전략
- 정렬은 `posts.id desc`로 최신 포스트가 먼저 오도록 구성합니다.
- 페이징은 커서 기반(`cursorId`)으로 `p.id < cursorId` 조건을 사용합니다.
- 정렬 기준과 커서 기준을 동일하게 유지해 중복/누락을 방지합니다.
- 기본 페이지 크기는 `size` 파라미터로 제어합니다.

## 삭제 처리
- 유저 삭제는 하드 삭제 방식입니다.
- 삭제 순서: 댓글(본인 작성/본인 포스트), 팔로우 관계, 포스트, 사용자 순으로 정리합니다.
- FK 제약을 고려해 연관 데이터를 먼저 삭제합니다.

## 성능 및 확장성 고려
- 본 프로젝트는 초기 단계 소셜 서비스를 가정하며 별도의 캐시 저장소 없이 RDB만 사용하는 환경을 전제로 합니다.
- 데이터 복잡도를 낮추고 개발 효율성을 높이기 위해 Pull Model(Fan-out on Read)을 채택했습니다.
- 향후 조회 성능이 저하될 경우 다음을 단계적으로 적용할 수 있습니다.
  - 인덱스 보강: `follows(follower_id, followee_id)` 및 `posts(user_id, created_at, id)` 복합 인덱스 최적화와 실행 계획 점검.
  - 캐싱: 사용자별 피드 결과를 짧은 TTL로 Redis에 저장하고, 새 포스트 작성 시 캐시 무효화 또는 부분 갱신.
  - Push Model 혼용: 포스트 생성 시 팔로워별 피드 테이블(예: `feed_items`)에 사전 적재하여 읽기 경로를 단순화.
