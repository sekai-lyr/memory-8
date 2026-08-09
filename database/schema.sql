CREATE DATABASE IF NOT EXISTS sekai_friend DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sekai_friend;

DROP TABLE IF EXISTS sekai_word_study_records;
DROP TABLE IF EXISTS sekai_word_favorite_words;
DROP TABLE IF EXISTS sekai_word_wrong_words;
DROP TABLE IF EXISTS sekai_word_mastered_words;
DROP TABLE IF EXISTS sekai_word_learning_status;
DROP TABLE IF EXISTS sekai_word_user_words;
DROP TABLE IF EXISTS sekai_word_activation_redemptions;
DROP TABLE IF EXISTS sekai_word_activation_codes;
DROP TABLE IF EXISTS sekai_word_pro_daily_tasks;
DROP TABLE IF EXISTS sekai_word_pro_study_goals;
DROP TABLE IF EXISTS sekai_word_user_subscriptions;
DROP TABLE IF EXISTS sekai_word_words;
DROP TABLE IF EXISTS sekai_word_users;

CREATE TABLE sekai_word_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sekai_word_user_subscriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    plan_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    start_time DATETIME,
    expire_time DATETIME,
    source VARCHAR(100),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_subscription_user_id (user_id),
    INDEX idx_subscription_expire_time (expire_time),
    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES sekai_word_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sekai_word_activation_codes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    plan_type VARCHAR(30) NOT NULL,
    duration_days INT NOT NULL,
    max_uses INT NOT NULL,
    price_yuan DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    used_count INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    note VARCHAR(255),
    expires_at DATETIME,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activation_code_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sekai_word_activation_redemptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    plan_type VARCHAR(30) NOT NULL,
    duration_days INT NOT NULL,
    redeemed_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code_user (code_id, user_id),
    INDEX idx_redemptions_user_id (user_id),
    CONSTRAINT fk_redemptions_code FOREIGN KEY (code_id) REFERENCES sekai_word_activation_codes(id) ON DELETE CASCADE,
    CONSTRAINT fk_redemptions_user FOREIGN KEY (user_id) REFERENCES sekai_word_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sekai_word_pro_study_goals (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    target_name VARCHAR(100) NOT NULL,
    target_date DATE NOT NULL,
    daily_minutes INT NOT NULL,
    target_word_count INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_pro_goal_user_id (user_id),
    INDEX idx_pro_goal_target_date (target_date),
    CONSTRAINT fk_pro_goals_user FOREIGN KEY (user_id) REFERENCES sekai_word_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sekai_word_pro_daily_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    task_date DATE NOT NULL,
    task_key VARCHAR(60) NOT NULL,
    title VARCHAR(100) NOT NULL,
    detail VARCHAR(500) NOT NULL,
    route VARCHAR(100) NOT NULL,
    target_count INT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_time DATETIME,
    sort_order INT NOT NULL,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_daily_task (user_id, task_date, task_key),
    INDEX idx_daily_task_user_date (user_id, task_date),
    CONSTRAINT fk_daily_tasks_user FOREIGN KEY (user_id) REFERENCES sekai_word_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sekai_word_words (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    english VARCHAR(100) NOT NULL,
    chinese VARCHAR(500) NOT NULL,
    phonetic VARCHAR(100),
    example_sentence VARCHAR(1000),
    category VARCHAR(100),
    difficulty VARCHAR(50),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_words_english (english),
    INDEX idx_words_english (english),
    INDEX idx_words_chinese (chinese),
    INDEX idx_words_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sekai_word_user_words (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    english VARCHAR(100) NOT NULL,
    chinese VARCHAR(500) NOT NULL,
    phonetic VARCHAR(100),
    example_sentence VARCHAR(1000),
    category VARCHAR(100),
    difficulty VARCHAR(50),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_word (user_id, english),
    INDEX idx_user_words_user_id (user_id),
    CONSTRAINT fk_user_words_user FOREIGN KEY (user_id) REFERENCES sekai_word_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sekai_word_wrong_words (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    word_id BIGINT NOT NULL,
    word_type VARCHAR(20) NOT NULL,
    english VARCHAR(100) NOT NULL,
    chinese VARCHAR(500) NOT NULL,
    wrong_count INT DEFAULT 1,
    unknown_count INT DEFAULT 0,
    last_wrong_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wrong_word (user_id, word_id, word_type),
    INDEX idx_wrong_words_user_id (user_id),
    CONSTRAINT fk_wrong_words_user FOREIGN KEY (user_id) REFERENCES sekai_word_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sekai_word_mastered_words (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    word_id BIGINT NOT NULL,
    word_type VARCHAR(20) NOT NULL,
    english VARCHAR(100) NOT NULL,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_mastered_word (user_id, word_id, word_type),
    INDEX idx_mastered_words_user_id (user_id),
    CONSTRAINT fk_mastered_words_user FOREIGN KEY (user_id) REFERENCES sekai_word_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sekai_word_learning_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    word_id BIGINT NOT NULL,
    word_type VARCHAR(20) NOT NULL,
    english VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    correct_count INT DEFAULT 0,
    wrong_count INT DEFAULT 0,
    unknown_count INT DEFAULT 0,
    review_interval_days INT DEFAULT 0,
    next_review_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_practiced_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_learning_status_word (user_id, word_id, word_type),
    INDEX idx_learning_status_user_id (user_id),
    INDEX idx_learning_status_status (status),
    INDEX idx_learning_status_next_review (next_review_time),
    CONSTRAINT fk_learning_status_user FOREIGN KEY (user_id) REFERENCES sekai_word_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sekai_word_favorite_words (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    word_id BIGINT NOT NULL,
    word_type VARCHAR(20) NOT NULL,
    english VARCHAR(100) NOT NULL,
    chinese VARCHAR(500) NOT NULL,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_favorite_word (user_id, word_id, word_type),
    INDEX idx_favorite_words_user_id (user_id),
    CONSTRAINT fk_favorite_words_user FOREIGN KEY (user_id) REFERENCES sekai_word_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sekai_word_study_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    total_count INT NOT NULL,
    correct_count INT NOT NULL,
    wrong_count INT NOT NULL,
    accuracy DECIMAL(5,2) NOT NULL,
    mode VARCHAR(50),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_study_records_user_id (user_id),
    CONSTRAINT fk_study_records_user FOREIGN KEY (user_id) REFERENCES sekai_word_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sekai_word_words (english, chinese, phonetic, example_sentence, category, difficulty) VALUES
('elaborate', '详细的；详尽的；精心制作的', '[i-LAB-er-uht]', 'She gave an elaborate explanation.', 'CET-6', 'medium'),
('accurate', '准确的；精确的', '[AK-yuh-ruht]', 'The report is accurate and clear.', 'CET-4', 'easy'),
('benefit', '好处；利益；有益于', '[BEN-uh-fit]', 'Reading brings great benefit.', 'CET-4', 'easy'),
('challenge', '挑战；质疑', '[CHAL-inj]', 'The task is a real challenge.', 'CET-4', 'easy'),
('determine', '决定；确定；查明', '[di-TUR-min]', 'We need to determine the cause.', 'CET-4', 'medium'),
('essential', '必要的；本质的；基本的', '[i-SEN-shuhl]', 'Water is essential for life.', 'CET-4', 'medium'),
('frequent', '频繁的；经常发生的', '[FREE-kwuhnt]', 'He makes frequent visits.', 'CET-4', 'medium'),
('generate', '产生；生成', '[JEN-uh-rayt]', 'The system can generate reports.', 'CET-6', 'medium'),
('improve', '改善；提高', '[im-PROOV]', 'Practice can improve your English.', 'CET-4', 'easy'),
('knowledge', '知识；学问', '[NOL-ij]', 'Knowledge changes life.', 'CET-4', 'easy'),
('maintain', '保持；维护；坚持认为', '[mayn-TAYN]', 'We should maintain good habits.', 'CET-4', 'medium'),
('opportunity', '机会；时机', '[op-er-TOO-nuh-tee]', 'This is a good opportunity.', 'CET-4', 'easy'),
('particular', '特定的；特别的；挑剔的', '[per-TIK-yuh-ler]', 'I have no particular plan.', 'CET-4', 'medium'),
('reliable', '可靠的；可信赖的', '[ri-LY-uh-buhl]', 'This source is reliable.', 'CET-6', 'medium'),
('significant', '重要的；显著的；有意义的', '[sig-NIF-i-kuhnt]', 'The change is significant.', 'CET-6', 'hard'),
('temporary', '暂时的；临时的', '[TEM-puh-rer-ee]', 'This is a temporary solution.', 'CET-6', 'medium'),
('various', '各种各样的；多方面的；多样的', '[VAIR-ee-uhs]', 'People have various opinions.', 'CET-4', 'easy'),
('wisdom', '智慧；明智', '[WIZ-duhm]', 'Experience brings wisdom.', 'CET-6', 'medium'),
('analysis', '分析；解析', '[uh-NAL-uh-sis]', 'Data analysis is important.', 'academic', 'medium'),
('strategy', '策略；战略', '[STRAT-uh-jee]', 'They created a learning strategy.', 'academic', 'medium');
