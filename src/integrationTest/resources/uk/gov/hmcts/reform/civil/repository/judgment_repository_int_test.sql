-- Select criteria test: Record to be selected
INSERT INTO judgments
(id, service_id, judgment_id, judgment_event_timestamp, court_code,
 ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date, registration_type,
 defendant_name, defendant_address_line_1, defendant_address_postcode)
VALUES
(nextval('jud_seq'), 'IT01', '1001-1', to_timestamp('2024-01-01 01:00:00', 'YYYY-MM-DD HH24:MI:SS'), '101',
 '10000001', '0AA10001', 11.0, to_date('2024-01-01', 'YYYY-MM-DD'), 'R',
 'Jud1Def1FirstName Jud1Def1LastName', 'Jud1Def1 Address Line 1', 'JD1 1DD');

-- Select criteria test: Different service_id to record to be selected
INSERT INTO judgments
(id, service_id, judgment_id, judgment_event_timestamp, court_code,
 ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date, registration_type,
 defendant_name, defendant_address_line_1, defendant_address_postcode)
VALUES
(nextval('jud_seq'), 'IT02', '1001-1', to_timestamp('2024-01-01 01:00:00', 'YYYY-MM-DD HH24:MI:SS'), '101',
 '10000001', '0AA10001', 11.0, to_date('2024-01-01', 'YYYY-MM-DD'), 'R',
 'Jud1Def1FirstName Jud1Def1LastName', 'Jud1Def1 Address Line 1', 'JD1 1DD');

-- Select criteria test: Different judgment_id to record to be selected
INSERT INTO judgments
(id, service_id, judgment_id, judgment_event_timestamp, court_code,
 ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date, registration_type,
 defendant_name, defendant_address_line_1, defendant_address_postcode)
VALUES
(nextval('jud_seq'), 'IT01', '1002-1', to_timestamp('2024-01-01 01:00:00', 'YYYY-MM-DD HH24:MI:SS'), '101',
 '10000001', '0AA10001', 11.0, to_date('2024-01-01', 'YYYY-MM-DD'), 'R',
 'Jud1Def1FirstName Jud1Def1LastName', 'Jud1Def1 Address Line 1', 'JD1 1DD');

-- Select criteria test: Different judgment_event_timestamp to record to be selected
INSERT INTO judgments
(id, service_id, judgment_id, judgment_event_timestamp, court_code,
 ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date, registration_type,
 defendant_name, defendant_address_line_1, defendant_address_postcode)
VALUES
(nextval('jud_seq'), 'IT01', '1001-1', to_timestamp('2024-01-01 02:00:00', 'YYYY-MM-DD HH24:MI:SS'), '101',
 '10000001', '0AA10001', 11.0, to_date('2024-01-01', 'YYYY-MM-DD'), 'R',
 'Jud1Def1FirstName Jud1Def1LastName', 'Jud1Def1 Address Line 1', 'JD1 1DD');

-- Select criteria test: Different case_number to record to be selected
INSERT INTO judgments
(id, service_id, judgment_id, judgment_event_timestamp, court_code,
 ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date, registration_type,
 defendant_name, defendant_address_line_1, defendant_address_postcode)
VALUES
(nextval('jud_seq'), 'IT01', '1001-1', to_timestamp('2024-01-01 01:00:00', 'YYYY-MM-DD HH24:MI:SS'), '101',
 '10000001', '0AA10002', 11.0, to_date('2024-01-01', 'YYYY-MM-DD'), 'R',
 'Jud1Def1FirstName Jud1Def1LastName', 'Jud1Def1 Address Line 1', 'JD1 1DD');

-- Two defendants test: Record with judgment id suffix of "-3"
INSERT INTO judgments
(id, service_id, judgment_id, judgment_event_timestamp, court_code,
 ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date, registration_type,
 defendant_name, defendant_address_line_1, defendant_address_postcode)
VALUES
(nextval('jud_seq'), 'IT02', '2002-3', to_timestamp('2024-02-02 02:00:00', 'YYYY-MM-DD HH24:MI:SS'), '202',
 '20000002', '0AA20002', 22.0, to_date('2024-02-02', 'YYYY-MM-DD'), 'R',
 'Jud2Def3FirstName Jud2Def3LastName', 'Jud2Def3 Address Line 1', 'JD2 3DD');

-- Two defendants test: Record with judgment id suffix of "-2"
INSERT INTO judgments
(id, service_id, judgment_id, judgment_event_timestamp, court_code,
 ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date, registration_type,
 defendant_name, defendant_address_line_1, defendant_address_postcode)
VALUES
(nextval('jud_seq'), 'IT02', '2002-2', to_timestamp('2024-02-02 02:00:00', 'YYYY-MM-DD HH24:MI:SS'), '202',
 '20000002', '0AA20002', 22.0, to_date('2024-02-02', 'YYYY-MM-DD'), 'R',
 'Jud2Def2FirstName Jud2Def2LastName', 'Jud2Def2 Address Line 1', 'JD2 2DD');

-- Two defendants test: Record with judgment id suffix of "-1"
INSERT INTO judgments
(id, service_id, judgment_id, judgment_event_timestamp, court_code,
 ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date, registration_type,
 defendant_name, defendant_address_line_1, defendant_address_postcode)
VALUES
(nextval('jud_seq'), 'IT02', '2002-1', to_timestamp('2024-02-02 02:00:00', 'YYYY-MM-DD HH24:MI:SS'), '202',
 '20000002', '0AA20002', 22.0, to_date('2024-02-02', 'YYYY-MM-DD'), 'R',
 'Jud2Def1FirstName Jud2Def1LastName', 'Jud2Def1 Address Line 1', 'JD2 1DD');
