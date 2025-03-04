DROP TABLE IF EXISTS game_sales;
DROP TABLE IF EXISTS daily_sales_aggregate;
DROP TABLE IF EXISTS import_job;

CREATE TABLE IF NOT EXISTS game_sales (
    id BIGINT PRIMARY KEY,
    game_no INT NOT NULL,
    game_name VARCHAR(20) NOT NULL,
    game_code VARCHAR(5) NOT NULL,
    type INT NOT NULL,
    cost_price DECIMAL(10, 2) NOT NULL,
    tax DECIMAL(10, 2) NOT NULL,
    sale_price DECIMAL(10, 2) NOT NULL,
    date_of_sale DATETIME NOT NULL,

    INDEX idx_game_sales_date (date_of_sale),
    INDEX idx_game_sales_sale_price (sale_price),
    INDEX idx_game_sales_date_sales_price (date_of_sale, sale_price)
) ;


CREATE TABLE IF NOT EXISTS daily_sales_aggregate (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_date DATE NOT NULL,
    game_no INT,
    total_count BIGINT NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,

    INDEX idx_daily_game_sales_date (sale_date),
    INDEX idx_daily_sales_unique (sale_date, game_no)
) ;

CREATE TABLE IF NOT EXISTS import_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    total_records BIGINT,
    processed_records BIGINT,
    failed_records BIGINT,
    error_message TEXT
) ;
