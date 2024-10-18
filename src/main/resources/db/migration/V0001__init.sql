SET search_path TO public;

------------------------------------------------
-- Create tables
------------------------------------------------
CREATE TABLE judgments
(id BIGINT,
version_number NUMERIC DEFAULT 0,
service_id VARCHAR(4),
judgment_id VARCHAR(42),
judgment_event_timestamp TIMESTAMP(2),
court_code VARCHAR(3),
ccd_case_ref VARCHAR(16),
case_number VARCHAR(8),
judgment_admin_order_total NUMERIC(8,2),
judgment_admin_order_date DATE,
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
CREATE UNIQUE INDEX jud_ind_pk ON judgments (id);


------------------------------------------------
-- Create Primary Keys
------------------------------------------------
ALTER TABLE judgments ADD CONSTRAINT jud_pk PRIMARY KEY USING INDEX jud_ind_pk;


------------------------------------------------
-- Create Check Constraints
------------------------------------------------
ALTER TABLE judgments ADD CONSTRAINT j_ser_id_nn CHECK (service_id IS NOT NULL);
ALTER TABLE judgments ADD CONSTRAINT j_jud_id_nn CHECK (judgment_id IS NOT NULL);
ALTER TABLE judgments ADD CONSTRAINT j_jud_eve_tim_nn CHECK (judgment_event_timestamp IS NOT NULL);
ALTER TABLE judgments ADD CONSTRAINT j_cou_cod_nn CHECK (court_code IS NOT NULL);
ALTER TABLE judgments ADD CONSTRAINT j_ccd_cas_ref_nn CHECK (ccd_case_ref IS NOT NULL);
ALTER TABLE judgments ADD CONSTRAINT j_cas_num_nn CHECK (case_number IS NOT NULL);
ALTER TABLE judgments ADD CONSTRAINT j_jud_adm_ord_tot_nn CHECK (judgment_admin_order_total IS NOT NULL);
ALTER TABLE judgments ADD CONSTRAINT j_jud_adm_ord_dat_nn CHECK (judgment_admin_order_date IS NOT NULL);
ALTER TABLE judgments ADD CONSTRAINT j_reg_typ_nn CHECK (registration_type IS NOT NULL);
ALTER TABLE judgments ADD CONSTRAINT j_def_nam_nn CHECK (defendant_name IS NOT NULL);
ALTER TABLE judgments ADD CONSTRAINT j_def_add_lin_1_nn CHECK (defendant_address_line_1 IS NOT NULL);
ALTER TABLE judgments ADD CONSTRAINT j_def_add_pos_nn CHECK (defendant_address_postcode IS NOT NULL);


------------------------------------------------
-- Create Unique constraints for PUBLIC
------------------------------------------------
ALTER TABLE judgments ADD CONSTRAINT j_ser_id_jud_id_uni UNIQUE (service_id, judgment_id);


------------------------------------------------
-- Create Sequences for PUBLIC
------------------------------------------------
CREATE SEQUENCE jud_seq MINVALUE 1 MAXVALUE 999999999999999999 INCREMENT BY 1 START WITH 1 NO CYCLE;
