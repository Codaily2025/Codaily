-- ✅ DROP 순서 (foreign key 역순)
DROP TABLE IF EXISTS code_review_items;
DROP TABLE IF EXISTS code_reviews;
DROP TABLE IF EXISTS feature_item_checklist;
DROP TABLE IF EXISTS feature_items;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS users;

-- ✅ users
INSERT INTO users (
    user_id,
    social_provider,
    email,
    nickname,
    password,
    role,
    created_at
) VALUES (
    1,  -- 명시적으로 ID 지정
    'local',
    'dummy@codaily.com',
    '테스트유저',
    'password123',
    'USER',
    now()
);

-- ✅ projects
INSERT INTO projects (
    project_id,
    user_id,
    title,
    created_at,
    updated_at
) VALUES (
    1,
    1,
    '코데일리 테스트 프로젝트',
    now(),
    now()
);

-- ✅ feature_items
INSERT INTO feature_items (
    feature_id,
    project_id,
    title,
    is_reduced,
    is_selected
) VALUES (
    101,
    1,
    '소셜 로그인',
    false,
    true
);

-- ✅ code_reviews
INSERT INTO code_reviews (
    code_review_id,
    project_id,
    feature_id,
    quality_score,
    summary,
    convention,
    refactor_suggestion,
    complexity,
    bug_risk,
    security_risk,
    created_at
) VALUES (
    201,
    1,
    101,
    86.0,
    '보안 항목 개선 필요. 중복 로직 존재.',
    '보통',
    '중복 메서드 분리 필요',
    '중간',
    '에러 누락 가능성 있음',
    'JWT 키 하드코딩',
    now()
);

-- ✅ code_review_items
INSERT INTO code_review_items (
    code_review_id, file_path, line_range,
    severity, message, category
) VALUES
(201, 'auth/jwt/JwtService.java', '30-45', '중간', 'JWT 키 하드코딩되어 있음', '보안'),
(201, 'controller/LoginController.java', '75-78', '높음', '에러 메시지 누락', '버그'),
(201, 'service/UserService.java', '120-130', '낮음', '중복된 로직 존재', '리팩토링');

-- ✅ feature_item_checklist
INSERT INTO feature_item_checklist (
    feature_item_id, item, done
) VALUES
(101, 'JWT 발급', true),
(101, '예외 처리', true),
(101, '로그인 실패 메시지 반환', false);
