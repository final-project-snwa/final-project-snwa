
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