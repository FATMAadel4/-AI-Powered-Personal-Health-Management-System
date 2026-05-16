-- بيتشغّل تلقائي أول ما MySQL يبدأ
-- بيعمل الـ databases المحتاجاها

CREATE DATABASE IF NOT EXISTS healthtrack_auth;
CREATE DATABASE IF NOT EXISTS healthtrack_health;

GRANT ALL PRIVILEGES ON healthtrack_auth.* TO 'healthuser'@'%';
GRANT ALL PRIVILEGES ON healthtrack_health.* TO 'healthuser'@'%';
FLUSH PRIVILEGES;
