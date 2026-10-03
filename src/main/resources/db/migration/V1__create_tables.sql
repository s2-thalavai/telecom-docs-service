-- MySQL 8.x schema. Keep in sync with com.telecom.entity.*
CREATE TABLE plans (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    code               VARCHAR(20)   NOT NULL,
    name               VARCHAR(100)  NOT NULL,
    monthly_price      DECIMAL(10,2) NOT NULL,
    data_allowance_gb  INT           NOT NULL,
    voice_minutes      INT           NULL,
    active             BOOLEAN       NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_plans_code UNIQUE (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE customers (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    msisdn      VARCHAR(15)  NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    plan_id     BIGINT       NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NULL,
    version     BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_customers_email  UNIQUE (email),
    CONSTRAINT uk_customers_msisdn UNIQUE (msisdn),
    CONSTRAINT fk_customers_plan   FOREIGN KEY (plan_id) REFERENCES plans (id),
    INDEX idx_customers_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
