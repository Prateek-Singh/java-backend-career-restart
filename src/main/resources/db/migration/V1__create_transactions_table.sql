CREATE TABLE transactions
(
    id                BINARY(16) NOT NULL,
    transaction_id    VARCHAR(255)   NOT NULL,
    account_id        VARCHAR(255)   NOT NULL,
    amount            DECIMAL(19, 2) NOT NULL,
    type              VARCHAR(50)    NOT NULL,
    description       VARCHAR(255),
    created_timestamp DATETIME(6) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_transactions_transaction_id UNIQUE (transaction_id)
);