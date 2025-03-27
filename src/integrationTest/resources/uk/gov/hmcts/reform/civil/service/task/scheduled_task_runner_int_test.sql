INSERT INTO Judgments
(id, version_number, service_id, judgment_id, judgment_event_timestamp,
 court_code, ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date,
 registration_type, cancellation_date, defendant_name, defendant_address_line_1, defendant_address_line_2,
 defendant_address_line_3, defendant_address_line_4, defendant_address_line_5, defendant_address_postcode, defendant_dob,
 reported_to_rtl)
VALUES
(1, 0, 'IT01', 'JUDG-1111-1111', to_timestamp('2024-01-01 01:01:01', 'YYYY-MM-DD HH24:MI:SS'),
 '111', 'CASE-REF-1111', 'CASE1111', 1.11, to_date('2024-01-01', 'YYYY-MM-DD'),
 'R', null, 'Defendant Name 1', 'Address 1 Line 1', null,
 null, null, null, 'AA1 1AA', null,
 null),

(2, 0, 'IT01', 'JUDG-2222-2222', to_timestamp('2024-02-02 02:02:02', 'YYYY-MM-DD HH24:MI:SS'),
 '222', 'CASE-REF-2222', 'CASE2222', 2.22, to_date('2024-02-02', 'YYYY-MM-DD'),
 'R', null, 'Defendant Name 2', 'Address 2 Line 1', null,
 null, null, null, 'AA2 2AA', null,
 CURRENT_DATE - INTERVAL '1 day'),

(3, 0, 'IT02', 'JUDG-3333-3333', to_timestamp('2024-03-03 03:03:03', 'YYYY-MM-DD HH24:MI:SS'),
 '333', 'CASE-REF-3333', 'CASE3333', 3.33, to_date('2024-03-03', 'YYYY-MM-DD'),
 'R', null, 'Defendant Name 3', 'Address 3 Line 1', null,
 null, null, null, 'AA3 3AA', null,
 null),

(4, 0, 'IT02', 'JUDG-4444-4444', to_timestamp('2024-04-04 04:04:04', 'YYYY-MM-DD HH24:MI:SS'),
 '444', 'CASE-REF-4444', 'CASE4444', 4.44, to_date('2024-04-04', 'YYYY-MM-DD'),
 'R', null, 'Defendant Name 4', 'Address 4 Line 1', null,
 null, null, null, 'AA4 4AA', null,
 CURRENT_DATE - INTERVAL '1 day'),

(5, 0, 'IT03', 'JUDG-5555-5555', to_timestamp('2024-05-05 05:05:05', 'YYYY-MM-DD HH24:MI:SS'),
 '555', 'CASE-REF-5555', 'CASE5555', 5.55, to_date('2024-05-05', 'YYYY-MM-DD'),
 'R', null, 'Defendant Name 5', 'Address 5 Line 1', null,
 null, null, null, 'AA5 5AA', null,
 CURRENT_DATE - INTERVAL '91 day');
