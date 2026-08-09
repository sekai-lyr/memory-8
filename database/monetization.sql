USE sekai_friend;

CREATE TABLE IF NOT EXISTS sekai_word_user_subscriptions (
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

CREATE TABLE IF NOT EXISTS sekai_word_activation_codes (
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

SET @activation_price_column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sekai_word_activation_codes'
      AND COLUMN_NAME = 'price_yuan'
);
SET @activation_price_sql := IF(
    @activation_price_column_exists = 0,
    'ALTER TABLE sekai_word_activation_codes ADD COLUMN price_yuan DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER max_uses',
    'SELECT 1'
);
PREPARE activation_price_stmt FROM @activation_price_sql;
EXECUTE activation_price_stmt;
DEALLOCATE PREPARE activation_price_stmt;

CREATE TABLE IF NOT EXISTS sekai_word_activation_redemptions (
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
