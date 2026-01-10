#!/usr/bin/env python3
import json
import random
import sys
import time
from datetime import datetime
from urllib import request, parse

BASE_URL = "http://localhost:8080"


def log_step(msg: str) -> None:
    print(f"\n==> {msg}")


def log_req(msg: str) -> None:
    print(f"--- {msg}")


def display_width(value: str) -> int:
    import unicodedata

    width = 0
    for ch in value:
        width += 2 if unicodedata.east_asian_width(ch) in ("W", "F") else 1
    return width


def pad_cell(value: str, width: int) -> str:
    pad = width - display_width(value)
    return value + (" " * max(pad, 0))


def print_table(headers: list[str], rows: list[list[str]]) -> None:
    widths = [display_width(h) for h in headers]
    for row in rows:
        for i, cell in enumerate(row):
            widths[i] = max(widths[i], display_width(cell))
    line = " | ".join(pad_cell(h, widths[i]) for i, h in enumerate(headers))
    sep = "-+-".join("-" * widths[i] for i in range(len(headers)))
    print(line)
    print(sep)
    for row in rows:
        print(" | ".join(pad_cell(row[i], widths[i]) for i in range(len(headers))))


def log_data(resp: dict) -> None:
    data = resp.get("data")
    if isinstance(data, dict) and isinstance(data.get("items"), list):
        items = data.get("items", [])
        if items and all(key in items[0] for key in ("postId", "userId", "content", "createdAt")):
            headers = ["Post ID", "User ID", "내용 (Content)", "작성 시간 (Created At)"]
            rows = []
            for item in items:
                created_at = item["createdAt"].replace("T", " ")
                rows.append([str(item["postId"]), str(item["userId"]), item["content"], created_at])
            print_table(headers, rows)
            return
        if items and all(key in items[0] for key in ("id", "postId", "userId", "content", "createdAt")):
            headers = ["Comment ID", "Post ID", "User ID", "내용 (Content)", "작성 시간 (Created At)"]
            rows = []
            for item in items:
                created_at = item["createdAt"].replace("T", " ")
                rows.append([
                    str(item["id"]),
                    str(item["postId"]),
                    str(item["userId"]),
                    item["content"],
                    created_at,
                ])
            print_table(headers, rows)
            return
        print(json.dumps(data, ensure_ascii=False, sort_keys=True))
        return
    if isinstance(data, dict) and all(key in data for key in ("id", "userId", "content", "createdAt")):
        headers = ["Post ID", "User ID", "내용 (Content)", "작성 시간 (Created At)"]
        created_at = data["createdAt"].replace("T", " ")
        rows = [[str(data["id"]), str(data["userId"]), data["content"], created_at]]
        print_table(headers, rows)
        return
    print(json.dumps(data, ensure_ascii=False, sort_keys=True))


def http_json(method: str, path: str, payload: dict | None = None) -> tuple[int, dict]:
    url = BASE_URL + path
    data = None
    headers = {}
    if payload is not None:
        data = json.dumps(payload).encode("utf-8")
        headers["Content-Type"] = "application/json"
    req = request.Request(url, data=data, headers=headers, method=method)
    try:
        with request.urlopen(req) as resp:
            body = resp.read().decode("utf-8")
            status = resp.getcode()
    except Exception as exc:
        print(f"request failed: {method} {url}")
        raise
    if not body:
        raise RuntimeError(f"empty response body for {method} {url}")
    return status, json.loads(body)


def http_no_body(method: str, path: str) -> int:
    url = BASE_URL + path
    req = request.Request(url, method=method)
    with request.urlopen(req) as resp:
        return resp.getcode()


def assert_status(resp: dict, expected: int) -> None:
    actual = resp.get("status")
    if actual == expected:
        print(f"assert status == {expected} [OK]")
        return
    print(f"assert status == {expected} [FAIL] actual={actual}")
    raise AssertionError(f"status {actual} != {expected}")


def assert_user_name(resp: dict, expected: str) -> None:
    name = resp.get("data", {}).get("name")
    if name == expected:
        print(f"assert user.name == {expected} [OK]")
        return
    print(f"assert user.name == {expected} [FAIL] actual={name}")
    raise AssertionError(f"name {name} != {expected}")


def assert_post_content(resp: dict, expected: str) -> None:
    content = resp.get("data", {}).get("content")
    if content == expected:
        print(f"assert post.content == {expected} [OK]")
        return
    print(f"assert post.content == {expected} [FAIL] actual={content}")
    raise AssertionError(f"content {content} != {expected}")


def assert_comment_matches(resp: dict, post_id: int, user_id: int) -> None:
    data = resp.get("data", {})
    actual_post_id = data.get("postId")
    actual_user_id = data.get("userId")
    if actual_post_id == post_id:
        print(f"assert comment.postId == {post_id} [OK]")
    else:
        print(f"assert comment.postId == {post_id} [FAIL] actual={actual_post_id}")
        raise AssertionError(f"postId {actual_post_id} != {post_id}")
    if actual_user_id == user_id:
        print(f"assert comment.userId == {user_id} [OK]")
        return
    print(f"assert comment.userId == {user_id} [FAIL] actual={actual_user_id}")
    raise AssertionError(f"userId {actual_user_id} != {user_id}")


def assert_feed_items(resp: dict, allowed_user_ids: set[int]) -> None:
    items = resp.get("data", {}).get("items", [])
    if items:
        print("assert feed items not empty [OK]")
    else:
        print("assert feed items not empty [FAIL]")
        raise AssertionError("feed items empty")
    print(f"assert feed userId in {sorted(allowed_user_ids)}")
    for item in items:
        uid = item.get("userId")
        if uid not in allowed_user_ids:
            print(f"assert feed userId in {sorted(allowed_user_ids)} [FAIL] actual={uid}")
            raise AssertionError(f"unexpected userId {uid}")

    def parse_dt(value: str) -> datetime:
        if "." not in value:
            return datetime.fromisoformat(value)
        head, frac = value.split(".", 1)
        frac = frac[:6].ljust(6, "0")
        return datetime.fromisoformat(f"{head}.{frac}")

    pairs = [(parse_dt(it["createdAt"]), it["postId"]) for it in items]
    print("assert feed order (createdAt desc, id desc)")
    for i in range(len(pairs) - 1):
        if pairs[i] < pairs[i + 1]:
            print("assert feed order (createdAt desc, id desc) [FAIL]")
            raise AssertionError("feed order not descending")
    print("assert feed order (createdAt desc, id desc) [OK]")


def main() -> int:
    random.seed(42)

    log_step("1) 사용자 5명 생성")
    names = ["alice", "bob", "carol", "dave", "erin"]
    user_ids: list[int] = []
    for name in names:
        log_req(f"POST /api/users (name={name})")
        code, resp = http_json("POST", "/api/users", {"name": name})
        assert code == 201, f"http {code} != 201"
        assert_status(resp, 201)
        assert_user_name(resp, name)
        user_id = resp["data"]["id"]
        user_ids.append(user_id)
        print(f"created user {name} id={user_id}")

    alice_id, bob_id, carol_id, dave_id, erin_id = user_ids

    log_step("2) 사용자 조회")
    for uid, name in zip(user_ids, names):
        log_req(f"GET /api/users/{uid}")
        code, resp = http_json("GET", f"/api/users/{uid}")
        assert code == 200, f"http {code} != 200"
        assert_status(resp, 200)
        assert_user_name(resp, name)
        log_data(resp)

    log_step("3) 사용자 수정 (alice)")
    log_req(f"PATCH /api/users/{alice_id}")
    code, resp = http_json("PATCH", f"/api/users/{alice_id}", {"name": "alice-updated"})
    assert code == 200, f"http {code} != 200"
    assert_status(resp, 200)
    assert_user_name(resp, "alice-updated")
    log_data(resp)

    log_step("4) 팔로우 관계 설정")
    def follow(follower: int, followee: int) -> None:
        log_req(f"POST /api/follows ({follower} -> {followee})")
        code, resp = http_json("POST", "/api/follows", {"followerId": follower, "followeeId": followee})
        assert code == 201, f"http {code} != 201"
        assert_status(resp, 201)

    follow(alice_id, bob_id)
    follow(alice_id, carol_id)
    follow(alice_id, dave_id)
    follow(bob_id, alice_id)
    follow(bob_id, erin_id)
    follow(carol_id, alice_id)
    follow(carol_id, bob_id)
    follow(dave_id, carol_id)
    follow(erin_id, bob_id)
    follow(erin_id, dave_id)

    log_step("5) 포스트 생성 (유저당 10~20건)")
    first_post_id_by_user: list[int] = []
    for uid in user_ids:
        count = 10 + random.randint(0, 10)
        print(f"create {count} posts for user {uid}")
        log_req(f"POST /api/posts (userId={uid}, count={count})")
        for i in range(1, count + 1):
            code, resp = http_json("POST", "/api/posts", {"userId": uid, "content": f"post {i} by user {uid}"})
            assert code == 201, f"http {code} != 201"
            assert_status(resp, 201)
            if i == 1:
                first_post_id_by_user.append(resp["data"]["id"])
        print("done")

    alice_post_id = first_post_id_by_user[0]
    bob_post_id = first_post_id_by_user[1]

    log_step("6) 포스트 조회/수정")
    log_req(f"GET /api/posts/{alice_post_id}")
    code, resp = http_json("GET", f"/api/posts/{alice_post_id}")
    assert code == 200, f"http {code} != 200"
    assert_status(resp, 200)
    log_data(resp)

    log_req(f"PATCH /api/posts/{alice_post_id}")
    code, resp = http_json("PATCH", f"/api/posts/{alice_post_id}", {"content": "alice post updated"})
    assert code == 200, f"http {code} != 200"
    assert_status(resp, 200)
    assert_post_content(resp, "alice post updated")
    log_data(resp)

    log_step("7) 댓글 생성")
    log_req("POST /api/comments (alice -> bob post)")
    code, comment1 = http_json(
        "POST",
        "/api/comments",
        {"postId": bob_post_id, "userId": alice_id, "content": "nice post"},
    )
    assert code == 201, f"http {code} != 201"
    assert_status(comment1, 201)
    assert_comment_matches(comment1, bob_post_id, alice_id)
    comment1_id = comment1["data"]["id"]

    log_req("POST /api/comments (bob -> alice post)")
    code, comment2 = http_json(
        "POST",
        "/api/comments",
        {"postId": alice_post_id, "userId": bob_id, "content": "great post"},
    )
    assert code == 201, f"http {code} != 201"
    assert_status(comment2, 201)
    assert_comment_matches(comment2, alice_post_id, bob_id)
    comment2_id = comment2["data"]["id"]

    print(f"comment1_id={comment1_id}")
    print(f"comment2_id={comment2_id}")

    log_step("8) 댓글 조회")
    log_req(f"GET /api/comments?postId={bob_post_id}&page=0&size=20")
    code, resp = http_json("GET", f"/api/comments?postId={bob_post_id}&page=0&size=20")
    assert code == 200, f"http {code} != 200"
    assert_status(resp, 200)
    log_data(resp)

    log_step("9) 댓글 수정/삭제")
    log_req(f"PATCH /api/comments/{comment1_id}")
    code, resp = http_json(
        "PATCH",
        f"/api/comments/{comment1_id}",
        {"userId": alice_id, "content": "nice post (edited)"},
    )
    assert code == 200, f"http {code} != 200"
    assert_status(resp, 200)
    assert_comment_matches(resp, bob_post_id, alice_id)
    log_data(resp)

    log_req(f"DELETE /api/comments/{comment1_id}?userId={alice_id}")
    code = http_no_body("DELETE", f"/api/comments/{comment1_id}?userId={alice_id}")
    assert code == 204, f"http {code} != 204"

    log_step("10) 피드 조회 (alice 기준)")
    log_req(f"GET /api/feed?followerId={alice_id}&size=5")
    code, feed1 = http_json("GET", f"/api/feed?followerId={alice_id}&size=5")
    assert code == 200, f"http {code} != 200"
    assert_status(feed1, 200)
    assert_feed_items(feed1, {bob_id, carol_id, dave_id})
    log_data(feed1)

    next_cursor = feed1.get("data", {}).get("nextCursor")
    if next_cursor:
        log_req(f"GET /api/feed?followerId={alice_id}&cursorId={next_cursor}&size=5")
        code, feed2 = http_json("GET", f"/api/feed?followerId={alice_id}&cursorId={next_cursor}&size=5")
        assert code == 200, f"http {code} != 200"
        assert_status(feed2, 200)
        assert_feed_items(feed2, {bob_id, carol_id, dave_id})
        log_data(feed2)

    log_step("11) 언팔로우")
    log_req(f"DELETE /api/follows?followerId={alice_id}&followeeId={bob_id}")
    code = http_no_body("DELETE", f"/api/follows?followerId={alice_id}&followeeId={bob_id}")
    assert code == 204, f"http {code} != 204"
    log_req(f"DELETE /api/follows?followerId={bob_id}&followeeId={alice_id}")
    code = http_no_body("DELETE", f"/api/follows?followerId={bob_id}&followeeId={alice_id}")
    assert code == 204, f"http {code} != 204"

    log_step("12) 사용자 삭제")
    for uid in user_ids:
        log_req(f"DELETE /api/users/{uid}")
        code = http_no_body("DELETE", f"/api/users/{uid}")
        assert code == 204, f"http {code} != 204"
        print(f"deleted user {uid}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
