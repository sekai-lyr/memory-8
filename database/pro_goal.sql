USE sekai_friend;

CREATE TABLE IF NOT EXISTS sekai_word_pro_study_goals (
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
