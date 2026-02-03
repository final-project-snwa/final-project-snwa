
INSERT IGNORE INTO category (id, category_name) VALUES (1, 'BASKETBALL');
INSERT IGNORE INTO category (id, category_name) VALUES (2, 'SOCCER');
INSERT IGNORE INTO category (id, category_name) VALUES (3, 'BASEBALL');
INSERT IGNORE INTO category (id, category_name) VALUES (4, 'FOOTBALL');

-- 관리자 계정 (처음 한 번만 생성)
INSERT IGNORE INTO users (
    email, 
    password, 
    nickname, 
    status, 
    role, 
    email_verified,
    created_date,
    updated_date
) VALUES (
    'admin@snwa.com',
    '$2a$12$5Bx9Lfq.UPvJ5KjZ56lwmO/.ENw7dMifaMM2gV30iDJYJS4f5s.r6',  -- 비밀번호: admin123!
    '관리자',
    'ACTIVE',
    'ADMIN',
    true,
    NOW(),
    NOW()
);
--관리자 아이디: admin@snwa.com 비밀번호: admin123!

/*
-- 테스트용 기사 2개
INSERT IGNORE INTO articles (
    category_id,
    user_id,
    title,
    content,
    translated_title,
    translated_content,
    summary,
    original_url,
    author_name,
    publisher_name,
    image_url,
    click_count,
    created_date,
    updated_date
) VALUES
(
    1,
    1,
    'NBA All-Star Game Sets New Scoring Record',
    'The 2026 NBA All-Star Game set a new scoring record. The Eastern Conference defeated the Western Conference 211-208.',
    'NBA 올스타전, 사상 최고 득점 기록 경신',
    '2026 NBA 올스타전이 사상 최고 득점 기록을 세우며 팬들에게 화려한 볼거리를 선사했다. 동부팀이 서부팀을 211-208로 꺾으며 승리를 거뒀다.',
    'NBA 올스타전 사상 최고 득점 기록 경신',
    'https://example.com/article-1',
    'The Athletic',
    'The Athletic',
    'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=800&h=450&fit=crop',
    0,
    NOW(),
    NOW()
),
(
    2,
    1,
    'Manchester City Secures Champions League Final Spot',
    'Manchester City mounted a dramatic comeback against Real Madrid to secure their place in the Champions League final.',
    '맨체스터 시티, 챔피언스리그 결승 진출 확정',
    '맨체스터 시티가 레알 마드리드를 상대로 극적인 역전승을 거두며 챔피언스리그 결승 진출을 확정했다.',
    '맨체스터 시티 챔피언스리그 결승 진출',
    'https://example.com/article-2',
    'ESPN',
    'ESPN',
    'https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800&h=450&fit=crop',
    0,
    NOW(),
    NOW()
);
*/