-- Cleaning up judgments table
DELETE FROM judgments;

-- Inserting the test data (the not null fields)
INSERT INTO judgments (id,
    judgment_id,
    service_id,
    judgment_event_timestamp,
    court_code,
    ccd_case_ref,
    case_number,
    judgment_admin_order_total,
    judgment_admin_order_date,
    registration_type,
    defendant_name,
    defendant_address_line_1,
    defendant_address_postcode,
    reported_to_rtl
)
VALUES
-- Record is 91 days old and should be deleted
(1, 'JUDG-1', 'SID1', (now() - INTERVAL '91 day'), 'C01', 'CASE-REF-123', 'CASE-123', 100.00,
(now() - INTERVAL '91 day'), 'R', 'DEFENDANT1', 'ADDRESS1', 'ABC-DEF', (now() - INTERVAL '91 day')),

-- Record is 90 days old and should not be deleted
(2, 'JUDG-2', 'SID2', (now() - INTERVAL '90 day'), 'C02', 'CASE-REF-234', 'CASE-234', 200.00,
(now() - INTERVAL '90 day'), 'R', 'DEFENDANT2', 'ADDRESS2', 'GHI-JKL', (now() - INTERVAL '90 day')),

-- Record is 89 days old and should not be deleted
(3, 'JUDG-3', 'SID3', (now() - INTERVAL '89 day'), 'C03', 'CASE-REF-345', 'CASE-345', 300.00,
(now() - INTERVAL '89 day'), 'R', 'DEFENDANT3', 'ADDRESS3', 'MNO-PQR', (now() - INTERVAL '89 day')),

-- Record has reported_to_rtl set as NULL and should NOT be deleted
(4, 'JUDG-4', 'SID4', (now() - INTERVAL '100 day'), 'C04', 'CASE-REF-456', 'CASE-456', 400.00,
(now() - INTERVAL '100 day'), 'R', 'DEFENDANT4', 'ADDRESS4', 'STU-VWX', null);
