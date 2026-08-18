INSERT INTO household_group (name, created_at) VALUES
    ('Room 12B', TIMESTAMP '2026-08-01 09:00:00'),
    ('Sharma Family', TIMESTAMP '2026-08-02 09:00:00');

INSERT INTO member (name, group_id) VALUES
    ('Alice', 1),
    ('Bob', 1),
    ('Charlie', 1),
    ('Diya', 2),
    ('Eshan', 2);

INSERT INTO bill_entry (description, amount, paid_by_id, bill_date, group_id, created_at) VALUES
    ('August rent', 2400.00, 1, DATE '2026-08-01', 1, TIMESTAMP '2026-08-01 10:00:00'),
    ('Weekly groceries', 100.00, 1, DATE '2026-08-05', 1, TIMESTAMP '2026-08-05 18:30:00'),
    ('Electricity bill', 150.00, 2, DATE '2026-08-08', 1, TIMESTAMP '2026-08-08 12:00:00'),
    ('Dinner out', 75.25, 3, DATE '2026-08-10', 1, TIMESTAMP '2026-08-10 21:15:00'),
    ('Apartment rent', 20000.00, 4, DATE '2026-08-01', 2, TIMESTAMP '2026-08-01 11:00:00'),
    ('WiFi and utilities', 850.50, 5, DATE '2026-08-06', 2, TIMESTAMP '2026-08-06 16:45:00');
