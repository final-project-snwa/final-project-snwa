-- translation_access_logs: (user_id, article_id) → (user_id, article_id, language)로 변경
-- 같은 기사의 다른 언어 번역본을 별도 구매할 수 있도록 함
-- 실행 전 기존 유니크 인덱스 이름 확인: SHOW INDEX FROM translation_access_logs WHERE Non_unique = 0;
ALTER TABLE translation_access_logs DROP INDEX UKnes5p17wfmva5kjqyh3uh4rf7;
ALTER TABLE translation_access_logs ADD UNIQUE KEY uk_user_article_language (user_id, article_id, language);
