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
 CURRENT_DATE - INTERVAL '89 day'),

(2, 0, 'IT01', 'JUDG-2222-2222', to_timestamp('2024-02-02 02:02:02', 'YYYY-MM-DD HH24:MI:SS'),
 '222', 'CASE-REF-2222', 'CASE2222', 2.22, to_date('2024-02-02', 'YYYY-MM-DD'),
 'R', null, 'Defendant Name 2', 'Address 2 Line 1', null,
 null, null, null, 'AA2 2AA', null,
 CURRENT_DATE - INTERVAL '90 day'),

(3, 0, 'IT02', 'JUDG-3333-3333', to_timestamp('2024-03-03 03:03:03', 'YYYY-MM-DD HH24:MI:SS'),
 '333', 'CASE-REF-3333', 'CASE3333', 3.33, to_date('2024-03-03', 'YYYY-MM-DD'),
 'R', null, 'Defendant Name 3', 'Address 3 Line 1', null,
 null, null, null, 'AA3 3AA', null,
 CURRENT_DATE - INTERVAL '91 day');
