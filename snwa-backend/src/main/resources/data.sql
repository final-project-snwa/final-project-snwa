
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

-- 테스트용 기사 5개
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
),
(
    3,
    1,
    'Ohtani Hits 50th Home Run of the Season',
    'Shohei Ohtani of the LA Dodgers reached 50 home runs this season with a two-run shot in the fifth inning against the San Francisco Giants.',
    '오타니, 시즌 50호 홈런 달성',
    'LA 다저스 오타니 쇼헤이가 샌프란시스코 자이언츠전 5회말 2점 홈런으로 시즌 50호를 기록했다.',
    '오타니 시즌 50호 홈런',
    'https://example.com/article-3',
    'MLB.com',
    'MLB.com',
    'https://images.unsplash.com/photo-1566577739112-5180d4bf9390?w=800&h=450&fit=crop',
    0,
    NOW(),
    NOW()
),
(
    4,
    1,
    'Cristiano Ronaldo Reaches 500 Club Goals',
    'Cristiano Ronaldo scored his 500th goal in club competitions during the match, becoming the first player to reach the milestone in the history of professional football.',
    '호날두, 클럽 통산 500호골 달성',
    '크리스티아누 호날두가 경기에서 클럽 대회 통산 500호골을 기록하며 프로 축구 사상 최초로 해당 마일스톤에 도달한 선수가 됐다.',
    '호날두 클럽 통산 500호골',
    'https://example.com/article-4',
    'ESPN',
    'ESPN',
    'https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=800&h=450&fit=crop',
    0,
    NOW(),
    NOW()
),
(
    4,
    1,
    'Patrick Mahomes Leads Chiefs to Back-to-Back Super Bowl',
    'Patrick Mahomes threw three touchdown passes as the Kansas City Chiefs defeated the San Francisco 49ers to win their second consecutive Super Bowl title.',
    '패트릭 마홈스, 쿼터백으로 연속 슈퍼볼 우승 이끌다',
    '패트릭 마홈스가 3개의 터치다운 패스를 성공시키며 캔자스시티 치프스가 샌프란시스코 49ers를 꺾고 2년 연속 슈퍼볼 우승을 차지했다.',
    '마홈스 연속 슈퍼볼 우승',
    'https://example.com/article-5',
    'NFL.com',
    'NFL.com',
    'https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=800&h=450&fit=crop',
    0,
    NOW(),
    NOW()
);

DELETE FROM coin_charge_policy;
INSERT INTO coin_charge_policy (id, name, coin_amount, price, active) VALUES
                                                                          (1,'10코인',10,1100,true),
                                                                          (2,'30코인',30,3100,true),
                                                                          (3,'50코인',50,5000,true),
                                                                          (4,'100코인',100,9900,true);