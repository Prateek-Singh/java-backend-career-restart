CREATE TABLE PROCESSED_EVENTS (
                                  EVENT_ID BINARY(16) NOT NULL,
                                  PROCESSED_AT DATETIME(6) NOT NULL,
                                  CONSUMER_NAME VARCHAR(55) NOT NULL,

                                  PRIMARY KEY (EVENT_ID)
);

CREATE TABLE TRANSACTION_EVENT_AUDIT (
                                         ID BINARY(16) NOT NULL,
                                         EVENT_ID BINARY(16) NOT NULL,
                                         TRANSACTION_ID VARCHAR(255) NOT NULL,
                                         ACCOUNT_ID VARCHAR(255) NOT NULL,
                                         AMOUNT DECIMAL(19, 2) NOT NULL,
                                         TRANSACTION_TYPE VARCHAR(50) NOT NULL,
                                         EVENT_TIMESTAMP DATETIME(6) NOT NULL,
                                         CREATED_AT DATETIME(6) NOT NULL,

                                         PRIMARY KEY (ID),

                                         CONSTRAINT UK_TRANSACTION_EVENT_AUDIT_EVENT_ID
                                             UNIQUE (EVENT_ID),

                                         CONSTRAINT FK_TRANSACTION_EVENT_AUDIT_EVENT_ID
                                             FOREIGN KEY (EVENT_ID)
                                                 REFERENCES PROCESSED_EVENTS(EVENT_ID)
);

CREATE INDEX IDX_TRANSACTION_EVENT_AUDIT_TRANSACTION_ID
    ON TRANSACTION_EVENT_AUDIT (TRANSACTION_ID);