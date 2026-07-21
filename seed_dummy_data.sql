-- ============================================================
--  CreditFlow  |  Dummy Data Seed Script
--  Run once in DBeaver against your AWS PostgreSQL database.
--
--  All accounts share the same password: Admin@1234
--  (same BCrypt hash as the seeded admin account)
-- ============================================================

DO $$
DECLARE
  -- Password hash for "Admin@1234"
  pwd CONSTANT TEXT := '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8RIZe5T5u0/C4BYUAi';

  -- Borrower IDs
  uid_tendai   BIGINT;
  uid_rudo     BIGINT;
  uid_farai    BIGINT;
  uid_tatenda  BIGINT;
  uid_simba    BIGINT;
  uid_chipo    BIGINT;
  uid_ngoni    BIGINT;
  uid_rutendo  BIGINT;

  -- Lender IDs
  uid_munashe  BIGINT;
  uid_vimbai   BIGINT;
  uid_blessing BIGINT;
  uid_tapiwa   BIGINT;
  uid_kudzai   BIGINT;

BEGIN

-- ════════════════════════════════════════════════════════════
--  BORROWERS
-- ════════════════════════════════════════════════════════════

-- 1. Tendai Chirwa — $500 PENDING  |  Good credit
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, wallet_balance, is_email_verified)
VALUES ('tendai.chirwa@mail.com', pwd, 'Tendai', 'Chirwa', '+263771100001', 'BORROWER', 'APPROVED', 0.00, TRUE)
RETURNING id INTO uid_tendai;

INSERT INTO credit_scores (user_id, score, risk_tier, total_loans_taken, loans_repaid_on_time, loans_defaulted, suggested_interest_rate)
VALUES (uid_tendai, 620, 'B', 1, 1, 0, 14.50);

INSERT INTO loans (borrower_id, principal_amount, interest_rate, term_months, monthly_repayment, total_repayment, status, purpose)
VALUES (uid_tendai, 500.00, 14.50, 6, 87.00, 522.00, 'PENDING', 'School fees for semester 2');


-- 2. Rudo Mukona — $2 000 APPROVED  |  Strong credit
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, wallet_balance, is_email_verified)
VALUES ('rudo.mukona@mail.com', pwd, 'Rudo', 'Mukona', '+263771100002', 'BORROWER', 'APPROVED', 0.00, TRUE)
RETURNING id INTO uid_rudo;

INSERT INTO credit_scores (user_id, score, risk_tier, total_loans_taken, loans_repaid_on_time, loans_defaulted, suggested_interest_rate)
VALUES (uid_rudo, 725, 'A', 2, 2, 0, 11.00);

INSERT INTO loans (borrower_id, principal_amount, interest_rate, term_months, monthly_repayment, total_repayment, status, purpose)
VALUES (uid_rudo, 2000.00, 11.00, 12, 177.00, 2124.00, 'APPROVED', 'Small business inventory restocking');


-- 3. Farai Banda — $800 ACTIVE (30 % repaid)
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, wallet_balance, is_email_verified)
VALUES ('farai.banda@mail.com', pwd, 'Farai', 'Banda', '+263771100003', 'BORROWER', 'APPROVED', 50.00, TRUE)
RETURNING id INTO uid_farai;

INSERT INTO credit_scores (user_id, score, risk_tier, total_loans_taken, loans_repaid_on_time, loans_defaulted, suggested_interest_rate)
VALUES (uid_farai, 680, 'B', 1, 0, 0, 13.00);

INSERT INTO loans (borrower_id, principal_amount, interest_rate, term_months, monthly_repayment, total_repayment,
                   amount_repaid, funded_amount, status, purpose, start_date, due_date)
VALUES (uid_farai, 800.00, 13.00, 9, 94.00, 846.00,
        254.00, 800.00, 'ACTIVE', 'Agricultural equipment repair',
        CURRENT_DATE - INTERVAL '3 months', CURRENT_DATE + INTERVAL '6 months');


-- 4. Tatenda Moyo — $1 500 ACTIVE (50 % repaid)
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, wallet_balance, is_email_verified)
VALUES ('tatenda.moyo@mail.com', pwd, 'Tatenda', 'Moyo', '+263771100004', 'BORROWER', 'APPROVED', 120.00, TRUE)
RETURNING id INTO uid_tatenda;

INSERT INTO credit_scores (user_id, score, risk_tier, total_loans_taken, loans_repaid_on_time, loans_defaulted, suggested_interest_rate)
VALUES (uid_tatenda, 700, 'A', 2, 1, 0, 12.00);

INSERT INTO loans (borrower_id, principal_amount, interest_rate, term_months, monthly_repayment, total_repayment,
                   amount_repaid, funded_amount, status, purpose, start_date, due_date)
VALUES (uid_tatenda, 1500.00, 12.00, 12, 133.00, 1596.00,
        798.00, 1500.00, 'ACTIVE', 'Home renovation materials',
        CURRENT_DATE - INTERVAL '6 months', CURRENT_DATE + INTERVAL '6 months');


-- 5. Simba Ncube — $3 000 PENDING  |  KYC under review (admin KYC tab will show this)
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, wallet_balance, is_email_verified)
VALUES ('simba.ncube@mail.com', pwd, 'Simba', 'Ncube', '+263771100005', 'BORROWER', 'PENDING_REVIEW', 0.00, TRUE)
RETURNING id INTO uid_simba;

INSERT INTO credit_scores (user_id, score, risk_tier, total_loans_taken, loans_repaid_on_time, loans_defaulted, suggested_interest_rate)
VALUES (uid_simba, 450, 'C', 0, 0, 0, 18.00);

INSERT INTO loans (borrower_id, principal_amount, interest_rate, term_months, monthly_repayment, total_repayment, status, purpose)
VALUES (uid_simba, 3000.00, 18.00, 18, 197.00, 3546.00, 'PENDING', 'Vehicle for business transport');

INSERT INTO kyc_documents (user_id, national_id_path, selfie_path, status)
VALUES (uid_simba, '/uploads/kyc/simba_national_id.jpg', '/uploads/kyc/simba_selfie.jpg', 'PENDING_REVIEW');


-- 6. Chipo Dube — $4 500 APPROVED  |  Excellent credit
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, wallet_balance, is_email_verified)
VALUES ('chipo.dube@mail.com', pwd, 'Chipo', 'Dube', '+263771100006', 'BORROWER', 'APPROVED', 0.00, TRUE)
RETURNING id INTO uid_chipo;

INSERT INTO credit_scores (user_id, score, risk_tier, total_loans_taken, loans_repaid_on_time, loans_defaulted, suggested_interest_rate)
VALUES (uid_chipo, 760, 'A', 3, 3, 0, 10.00);

INSERT INTO loans (borrower_id, principal_amount, interest_rate, term_months, monthly_repayment, total_repayment, status, purpose)
VALUES (uid_chipo, 4500.00, 10.00, 24, 208.00, 4992.00, 'APPROVED', 'Expand retail shop stock');


-- 7. Ngoni Mhuru — $1 200 ACTIVE (60 % repaid)
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, wallet_balance, is_email_verified)
VALUES ('ngoni.mhuru@mail.com', pwd, 'Ngoni', 'Mhuru', '+263771100007', 'BORROWER', 'APPROVED', 200.00, TRUE)
RETURNING id INTO uid_ngoni;

INSERT INTO credit_scores (user_id, score, risk_tier, total_loans_taken, loans_repaid_on_time, loans_defaulted, suggested_interest_rate)
VALUES (uid_ngoni, 730, 'A', 2, 2, 0, 11.50);

INSERT INTO loans (borrower_id, principal_amount, interest_rate, term_months, monthly_repayment, total_repayment,
                   amount_repaid, funded_amount, status, purpose, start_date, due_date)
VALUES (uid_ngoni, 1200.00, 11.50, 10, 127.00, 1270.00,
        762.00, 1200.00, 'ACTIVE', 'Medical expenses',
        CURRENT_DATE - INTERVAL '6 months', CURRENT_DATE + INTERVAL '4 months');


-- 8. Rutendo Zulu — $600 PENDING  |  No KYC yet, low score
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, wallet_balance, is_email_verified)
VALUES ('rutendo.zulu@mail.com', pwd, 'Rutendo', 'Zulu', '+263771100008', 'BORROWER', 'NOT_SUBMITTED', 0.00, FALSE)
RETURNING id INTO uid_rutendo;

INSERT INTO credit_scores (user_id, score, risk_tier, total_loans_taken, loans_repaid_on_time, loans_defaulted, suggested_interest_rate)
VALUES (uid_rutendo, 300, 'D', 0, 0, 0, 22.00);

INSERT INTO loans (borrower_id, principal_amount, interest_rate, term_months, monthly_repayment, total_repayment, status, purpose)
VALUES (uid_rutendo, 600.00, 22.00, 6, 114.00, 684.00, 'PENDING', 'Emergency household repairs');


-- ════════════════════════════════════════════════════════════
--  LENDERS
-- ════════════════════════════════════════════════════════════

-- 1. Munashe Tande — $3 500 balance
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, wallet_balance, is_email_verified)
VALUES ('munashe.tande@mail.com', pwd, 'Munashe', 'Tande', '+263771200001', 'LENDER', 'APPROVED', 3500.00, TRUE)
RETURNING id INTO uid_munashe;

INSERT INTO credit_scores (user_id, score, risk_tier, suggested_interest_rate)
VALUES (uid_munashe, 800, 'A', 9.00);


-- 2. Vimbai Masiya — $2 800 balance
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, wallet_balance, is_email_verified)
VALUES ('vimbai.masiya@mail.com', pwd, 'Vimbai', 'Masiya', '+263771200002', 'LENDER', 'APPROVED', 2800.00, TRUE)
RETURNING id INTO uid_vimbai;

INSERT INTO credit_scores (user_id, score, risk_tier, suggested_interest_rate)
VALUES (uid_vimbai, 750, 'A', 10.00);


-- 3. Blessing Maposa — $1 500 balance  |  KYC under review (admin KYC tab will show this)
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, wallet_balance, is_email_verified)
VALUES ('blessing.maposa@mail.com', pwd, 'Blessing', 'Maposa', '+263771200003', 'LENDER', 'PENDING_REVIEW', 1500.00, TRUE)
RETURNING id INTO uid_blessing;

INSERT INTO credit_scores (user_id, score, risk_tier, suggested_interest_rate)
VALUES (uid_blessing, 680, 'B', 13.00);

INSERT INTO kyc_documents (user_id, national_id_path, police_clearance_path, selfie_path, status)
VALUES (uid_blessing, '/uploads/kyc/blessing_national_id.jpg', '/uploads/kyc/blessing_police.pdf', '/uploads/kyc/blessing_selfie.jpg', 'PENDING_REVIEW');


-- 4. Tapiwa Chirwa — $5 000 balance
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, wallet_balance, is_email_verified)
VALUES ('tapiwa.chirwa@mail.com', pwd, 'Tapiwa', 'Chirwa', '+263771200004', 'LENDER', 'APPROVED', 5000.00, TRUE)
RETURNING id INTO uid_tapiwa;

INSERT INTO credit_scores (user_id, score, risk_tier, suggested_interest_rate)
VALUES (uid_tapiwa, 820, 'A', 8.50);


-- 5. Kudzai Mutasa — $4 200 balance
INSERT INTO users (email, password, first_name, last_name, phone_number, role, kyc_status, wallet_balance, is_email_verified)
VALUES ('kudzai.mutasa@mail.com', pwd, 'Kudzai', 'Mutasa', '+263771200005', 'LENDER', 'APPROVED', 4200.00, TRUE)
RETURNING id INTO uid_kudzai;

INSERT INTO credit_scores (user_id, score, risk_tier, suggested_interest_rate)
VALUES (uid_kudzai, 790, 'A', 9.50);


END $$;
