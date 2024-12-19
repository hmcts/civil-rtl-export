INSERT INTO judgments
(id, service_id, judgment_id, judgment_event_timestamp, court_code,
 ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date, registration_type,
 defendant_name, defendant_address_line_1, defendant_address_postcode)
VALUES
(nextval('jud_seq'), 'IT01', '5005-1', to_timestamp('2024-05-05 05:00:00', 'YYYY-MM-DD HH24:MI:SS'), '123',
 '50000005', '0AA50005', 55.0, to_date('2024-05-05', 'YYYY-MM-DD'), 'R',
 'Jud5Def1FirstName Jud5Def1LastName', 'Jud5Def1 Address Line 1', 'JD5 1DD');

INSERT INTO judgments
(id, service_id, judgment_id, judgment_event_timestamp, court_code,
 ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date, registration_type,
 defendant_name, defendant_address_line_1, defendant_address_postcode)
VALUES
(nextval('jud_seq'), 'IT01', '6006-1', to_timestamp('2024-06-06 06:00:00', 'YYYY-MM-DD HH24:MI:SS'), '123',
 '60000006', '0AA60006', 66.0, to_date('2024-06-06', 'YYYY-MM-DD'), 'R',
 'Jud6Def1FirstName Jud6Def1LastName', 'Jud6Def1 Address Line 1', 'JD6 1DD');

INSERT INTO judgments
(id, service_id, judgment_id, judgment_event_timestamp, court_code,
 ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date, registration_type,
 defendant_name, defendant_address_line_1, defendant_address_postcode)
VALUES
(nextval('jud_seq'), 'IT01', '7007-1', to_timestamp('2024-07-07 07:00:00', 'YYYY-MM-DD HH24:MI:SS'), '123',
 '70000007', '0AA70007', 77.0, to_date('2024-07-07', 'YYYY-MM-DD'), 'R',
 'Jud7Def1FirstName Jud7Def1LastName', 'Jud7Def1 Address Line 1', 'JD7 1DD');
