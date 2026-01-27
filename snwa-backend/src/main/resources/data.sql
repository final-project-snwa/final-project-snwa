
INSERT IGNORE INTO category (id, category_name) VALUES (1, 'BASKETBALL');
INSERT IGNORE INTO category (id, category_name) VALUES (2, 'SOCCER');
INSERT IGNORE INTO category (id, category_name) VALUES (3, 'BASEBALL');
INSERT IGNORE INTO category (id, category_name) VALUES (4, 'FOOTBALL');

-- INSERT IGNORE INTO crawling_job (id, category_id, source_name, job_name, target_url, cron_expression, is_active, created_at, updated_at)
-- VALUES (1, 1, 'ESPN', 'NBA News Auto', 'http://site.api.espn.com/apis/site/v2/sports/basketball/nba/news', '0 0 * * * *', 1, NOW(), NOW());
--
-- INSERT IGNORE INTO crawling_job (id, category_id, source_name, job_name, target_url, cron_expression, is_active, created_at, updated_at)
-- VALUES (2, 2, 'ESPN', 'EPL News Auto', 'http://site.api.espn.com/apis/site/v2/sports/soccer/eng.1/news', '0 0 * * * *', 1, NOW(), NOW());