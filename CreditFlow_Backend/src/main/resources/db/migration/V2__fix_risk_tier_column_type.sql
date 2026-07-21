-- Fix risk_tier column type: CHAR(1)/bpchar → VARCHAR(1) to match Hibernate 6 String mapping
ALTER TABLE credit_scores ALTER COLUMN risk_tier TYPE VARCHAR(1) USING risk_tier::VARCHAR;
