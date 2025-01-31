INSERT INTO Judgments
(id, version_number, service_id, judgment_id, judgment_event_timestamp,
 court_code, ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date,
 registration_type, cancellation_date, defendant_name, defendant_address_line_1, defendant_address_line_2,
 defendant_address_line_3, defendant_address_line_4, defendant_address_line_5, defendant_address_postcode, defendant_dob,
 reported_to_rtl)
VALUES
(1, 0, 'Sid1', 'JUDG-1111-1111', CURRENT_TIMESTAMP,
 '111', 'CASE-REF-1111', 'CASE1111', 11.11, CURRENT_DATE,
 'R', null, 'Defendant Name 1', 'Address 1 Line 1', null,
 null, null, null, 'AA1 1AA', null,
 null),

(2, 0, 'Sid1', 'JUDG-2222-2222', CURRENT_TIMESTAMP,
 '222', 'CASE-REF-2222', 'CASE2222', 22.22, CURRENT_DATE,
 'R', null, 'Defendant Name 2', 'Address 2 Line 1', null,
 null, null, null, 'AA2 2AA', null,
 CURRENT_DATE - INTERVAL '1 day'),

(3, 0, 'Sid2', 'JUDG-3333-3333', CURRENT_TIMESTAMP,
 '333', 'CASE-REF-3333', 'CASE3333', 33.33, CURRENT_DATE,
 'R', null, 'Defendant Name 3', 'Address 3 Line 1', null,
 null, null, null, 'AA3 3AA', null,
 null),

(4, 0, 'Sid2', 'JUDG-4444-4444', CURRENT_TIMESTAMP,
 '444', 'CASE-REF-4444', 'CASE4444', 44.44, CURRENT_DATE,
 'R', null, 'Defendant Name 4', 'Address 4 Line 1', null,
 null, null, null, 'AA4 4AA', null,
 CURRENT_DATE - INTERVAL '1 day'),

(5, 0, 'Sid3', 'JUDG-5555-5555', CURRENT_TIMESTAMP,
 '555', 'CASE-REF-5555', 'CASE5555', 55.55, CURRENT_DATE,
 'R', null, 'Defendant Name 5', 'Address 5 Line 1', null,
 null, null, null, 'AA5 5AA', null,
 now() - INTERVAL '89 day'),

(6, 0, 'Sid4', 'JUDG-6666-6666', CURRENT_TIMESTAMP,
 '666', 'CASE-REF-6666', 'CASE6666', 66.66, CURRENT_DATE,
 'R', null, 'Defendant Name 6', 'Address 6 Line 1', null,
 null, null, null, 'AA6 6AA', null,
 now() - INTERVAL '90 day'),

(7, 0, 'Sid5', 'JUDG-7777-7777', CURRENT_TIMESTAMP,
 '777', 'CASE-REF-7777', 'CASE7777', 77.77, CURRENT_DATE,
 'R', null, 'Defendant Name 7', 'Address 7 Line 1', null,
 null, null, null, 'AA7 7AA', null,
 now() - INTERVAL '91 day');
