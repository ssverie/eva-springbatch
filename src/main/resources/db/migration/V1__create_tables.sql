CREATE TABLE source_deals (
    id              BIGSERIAL PRIMARY KEY,
    deal_number     VARCHAR(50),
    customer_name   VARCHAR(100),
    amount          NUMERIC(15,2),
    currency        VARCHAR(3),
    deal_date       DATE,
    category        VARCHAR(50),
    status          VARCHAR(20),
    risk_level      VARCHAR(20),
    error_message   VARCHAR(500),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE staging_deals AS SELECT * FROM source_deals WHERE 1=0;
CREATE TABLE processing_deals AS SELECT * FROM source_deals WHERE 1=0;
CREATE TABLE output_deals AS SELECT * FROM source_deals WHERE 1=0;

CREATE TABLE summary_deals (
    id              BIGSERIAL PRIMARY KEY,
    category        VARCHAR(50),
    risk_level      VARCHAR(20),
    deal_count      INTEGER,
    total_amount    NUMERIC(15,2),
    avg_amount      NUMERIC(15,2),
    calculated_at   TIMESTAMP DEFAULT NOW()
);