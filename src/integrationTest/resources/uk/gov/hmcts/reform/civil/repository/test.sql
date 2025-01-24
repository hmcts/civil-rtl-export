INSERT INTO Judgments
(id, version_number, service_id, judgment_id, judgment_event_timestamp,
 court_code, ccd_case_ref, case_number, judgment_admin_order_total, judgment_admin_order_date,
 registration_type, cancellation_date, defendant_name, defendant_address_line_1, defendant_address_line_2,
 defendant_address_line_3, defendant_address_line_4, defendant_address_line_5, defendant_address_postcode, defendant_dob,
 reported_to_rtl)
VALUES
(1, 0, 'Sid1', 'JUDG-1234-5678', CURRENT_TIMESTAMP,
 '123', 'CASE-REF-1234', 'CASE1234', '99.99', CURRENT_DATE,
 'R', null, 'John Smith', '123 Sample Street', null,
 null, null, null, 'ABC-123', null,
 to_timestamp('2024-11-1 12:00:00', 'YYYY-MM-DD HH24:MI:SS')),

(2, 1, 'Sid2', 'JUDG-4321-9876', CURRENT_TIMESTAMP,
 '222', 'CASE-REF-1777', 'CASE5678', '55.55', CURRENT_DATE,
 'K', null, 'Tom Doe', '77 Fake Street', null,
 null, null, null, 'TTT-4S4', null,
 now() - INTERVAL '1 day'),

(3, 2, 'Sid3', 'JUDG-4563-6346', CURRENT_TIMESTAMP,
 '333', 'CASE-REF-1536', 'CASE7777', '756.55', CURRENT_DATE,
 'K', null, 'Bill Braun', '88 Test Road', null,
 null, null, null, 'GXH-8TK', null,
 null),

(4, 3, 'Sid4', 'JUDG-1111-2222', CURRENT_TIMESTAMP,
 '444', 'CASE-REF-1111', 'CASE1111', '100.00', CURRENT_DATE,
 'K', null, 'NAME1', 'ADD1', null,
 null, null, null, 'XXX-XXX', null,
 now() - INTERVAL '89 day'),

(5, 4, 'Sid5', 'JUDG-2222-3333', CURRENT_TIMESTAMP,
 '555', 'CASE-REF-2222', 'CASE2222', '200.00', CURRENT_DATE,
 'K', null, 'NAME2', 'ADD2', null,
 null, null, null, 'YYY-YYY', null,
 now() - INTERVAL '90 day'),

(6, 5, 'Sid6', 'JUDG-3333-4444', CURRENT_TIMESTAMP,
 '666', 'CASE-REF-3333', 'CASE3333', '300.00', CURRENT_DATE,
 'K', null, 'NAME3', 'ADD3', null,
 null, null, null, 'ZZZ-ZZZ', null,
 now() - INTERVAL '91 day');
