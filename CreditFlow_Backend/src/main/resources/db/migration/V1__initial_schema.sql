-- CreditFlow Initial Schema
-- V1__initial_schema.sql

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
    national_id VARCHAR(50) UNIQUE,
    role VARCHAR(20) NOT NULL,
    kyc_status VARCHAR(30) NOT NULL DEFAULT 'NOT_SUBMITTED',
    wallet_balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE kyc_documents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    national_id_path VARCHAR(500),
    police_clearance_path VARCHAR(500),
    selfie_path VARCHAR(500),
    proof_of_residence_path VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'NOT_SUBMITTED',
    rejection_reason VARCHAR(500),
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    submitted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE credit_scores (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    score INT NOT NULL DEFAULT 300,
    risk_tier CHAR(1),
    mobile_money_score NUMERIC(5, 2) DEFAULT 0.00,
    repayment_history_score NUMERIC(5, 2) DEFAULT 0.00,
    bank_behaviour_score NUMERIC(5, 2) DEFAULT 0.00,
    kyc_score NUMERIC(5, 2) DEFAULT 0.00,
    total_loans_taken INT DEFAULT 0,
    loans_repaid_on_time INT DEFAULT 0,
    loans_defaulted INT DEFAULT 0,
    suggested_interest_rate NUMERIC(5, 2),
    last_calculated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    borrower_id BIGINT NOT NULL REFERENCES users(id),
    principal_amount NUMERIC(15, 2) NOT NULL,
    interest_rate NUMERIC(5, 2) NOT NULL,
    term_months INT NOT NULL,
    monthly_repayment NUMERIC(15, 2),
    total_repayment NUMERIC(15, 2),
    amount_repaid NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    funded_amount NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    purpose VARCHAR(500),
    start_date DATE,
    due_date DATE,
    admin_notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE loan_investments (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loans(id),
    lender_id BIGINT NOT NULL REFERENCES users(id),
    invested_amount NUMERIC(15, 2) NOT NULL,
    expected_return NUMERIC(15, 2),
    actual_return NUMERIC(15, 2) DEFAULT 0.00,
    invested_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE repayment_schedules (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loans(id) ON DELETE CASCADE,
    installment_number INT NOT NULL,
    due_date DATE NOT NULL,
    principal_component NUMERIC(15, 2) NOT NULL,
    interest_component NUMERIC(15, 2) NOT NULL,
    total_amount NUMERIC(15, 2) NOT NULL,
    paid_amount NUMERIC(15, 2) DEFAULT 0.00,
    paid_date DATE,
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,
    is_overdue BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    reference VARCHAR(100) NOT NULL UNIQUE,
    sender_id BIGINT REFERENCES users(id),
    recipient_id BIGINT REFERENCES users(id),
    loan_id BIGINT REFERENCES loans(id),
    amount NUMERIC(15, 2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(30),
    external_reference VARCHAR(100),
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    notification_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_loans_borrower ON loans(borrower_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_transactions_sender ON transactions(sender_id);
CREATE INDEX idx_transactions_recipient ON transactions(recipient_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_repayment_loan ON repayment_schedules(loan_id);

-- Default admin user (password: Admin@1234 - change in production)
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, is_email_verified)
VALUES (
    'admin@creditflow.co.zw',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8RIZe5T5u0/C4BYUAi',
    'System',
    'Admin',
    '+263771000001',
    'ADMIN',
    'APPROVED',
    TRUE
);

INSERT INTO credit_scores (user_id, score, risk_tier, suggested_interest_rate)
VALUES (1, 850, 'A', 8.00);
