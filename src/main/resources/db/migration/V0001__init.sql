SET search_path TO public;

------------------------------------------------
-- Create tables
------------------------------------------------
CREATE TABLE judgements
(id BIGINT,
service_id VARCHAR(4),
judgement_id VARCHAR(42),
judgement_event_timestamp TIMESTAMP(2),
court_code VARCHAR(3),
ccd_case_ref VARCHAR(16),
case_number VARCHAR(8),
judgement_admin_order_total NUMERIC(8,2),
judgement_admin_order_date DATE,
registration_type VARCHAR(1),
cancellation_date DATE,
defendant_name VARCHAR(70),
defendant_address_line_1 VARCHAR(35),
defendant_address_line_2 VARCHAR(35),
defendant_address_line_3 VARCHAR(35),
defendant_address_line_4 VARCHAR(35),
defendant_address_line_5 VARCHAR(35),
defendant_address_postcode VARCHAR(8),
defendant_dob DATE,
reported_to_rtl TIMESTAMP(2)
);


------------------------------------------------
-- Create indices
------------------------------------------------
CREATE UNIQUE INDEX judgements_pk ON judgements (id);


------------------------------------------------
-- Create Primary Keys
------------------------------------------------
ALTER TABLE judgements ADD CONSTRAINT j_i_pk PRIMARY KEY USING INDEX judgements_pk;


------------------------------------------------
-- Create Check Constraints
------------------------------------------------
ALTER TABLE judgements ADD CONSTRAINT j_si_nn CHECK (service_id IS NOT NULL);
ALTER TABLE judgements ADD CONSTRAINT judgement_id CHECK (judgement_id IS NOT NULL);
ALTER TABLE judgements ADD CONSTRAINT judgement_event_timestamp CHECK (judgement_event_timestamp IS NOT NULL);
ALTER TABLE judgements ADD CONSTRAINT court_code CHECK (court_code IS NOT NULL);
ALTER TABLE judgements ADD CONSTRAINT ccd_case_ref CHECK (ccd_case_ref IS NOT NULL);
ALTER TABLE judgements ADD CONSTRAINT case_number CHECK (case_number IS NOT NULL);
ALTER TABLE judgements ADD CONSTRAINT judgement_admin_order_total CHECK (judgement_admin_order_total IS NOT NULL);
ALTER TABLE judgements ADD CONSTRAINT judgement_admin_order_date CHECK (judgement_admin_order_date IS NOT NULL);
ALTER TABLE judgements ADD CONSTRAINT registration_type CHECK (registration_type IS NOT NULL);
ALTER TABLE judgements ADD CONSTRAINT defendant_name CHECK (defendant_name IS NOT NULL);
ALTER TABLE judgements ADD CONSTRAINT defendant_address_line_1 CHECK (defendant_address_line_1 IS NOT NULL);
ALTER TABLE judgements ADD CONSTRAINT defendant_address_postcode CHECK (defendant_address_postcode IS NOT NULL);


------------------------------------------------
-- Create Unique constraints for PUBLIC
------------------------------------------------
ALTER TABLE judgements ADD CONSTRAINT j_si_ji_uniq UNIQUE (service_id, judgement_id);


------------------------------------------------
-- Create Sequences for PUBLIC
------------------------------------------------
CREATE SEQUENCE judg_seq MINVALUE 1 MAXVALUE 999999999999999999 INCREMENT BY 1 START WITH 1 NO CYCLE;
