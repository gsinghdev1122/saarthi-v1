-- Seed data so the app is useful on first run (Delhi Cantt canteen), matching the original prototype.

INSERT INTO inventory (index_no, name, division, closing_stock, reorder_level, value, trend, status, canteen) VALUES
('GRO-1042', 'Basmati Rice 5kg', 'Grocery', 210, 60, 189000.00, 'up', 'healthy', 'Delhi Cantt'),
('GRO-1088', 'Refined Sunflower Oil 1L', 'Grocery', 34, 50, 8500.00, 'down', 'low', 'Delhi Cantt'),
('GRO-1120', 'Toor Dal 1kg', 'Grocery', 150, 40, 22500.00, 'stable', 'healthy', 'Delhi Cantt'),
('LIQ-2031', 'Single Malt Whisky 750ml', 'Liquor', 18, 25, 90000.00, 'down', 'low', 'Delhi Cantt'),
('LIQ-2075', 'Premium Beer Case (24)', 'Liquor', 88, 30, 66000.00, 'up', 'healthy', 'Delhi Cantt'),
('GRO-1204', 'Toothpaste 200g', 'Grocery', 5, 20, 900.00, 'down', 'low', 'Delhi Cantt');

INSERT INTO employees (name, employee_code, category, designation, attendance, contract_end, status, canteen) VALUES
('Naik R. Kumar', 'CSD-001', 'Permanent', 'Store Supervisor', 98.20, NULL, 'active', 'Delhi Cantt'),
('Havildar S. Singh', 'CSD-002', 'Permanent', 'Canteen Manager', 96.50, NULL, 'active', 'Delhi Cantt'),
('A. Verma', 'CSD-014', 'Contractual', 'Store Assistant', 91.00, CURRENT_DATE + INTERVAL '45 days', 'expiring', 'Delhi Cantt'),
('P. Sharma', 'CSD-021', 'Daily Wage', 'Counter Helper', 88.40, CURRENT_DATE + INTERVAL '15 days', 'expiring', 'Delhi Cantt');

INSERT INTO expenses (category, vendor, amount, date, status, submitted_by, canteen) VALUES
('Repairs & maintenance', 'S. K. Electricals', 12500.00, CURRENT_DATE - INTERVAL '3 days', 'approved', 'Canteen Manager', 'Delhi Cantt'),
('Transport', 'Delhi Logistics Co.', 34200.00, CURRENT_DATE - INTERVAL '7 days', 'pending', 'Canteen Manager', 'Delhi Cantt');

INSERT INTO approvals (type, reference, amount, submitted_by, status) VALUES
('expense', 'Transport - Delhi Logistics Co.', 34200.00, 'Canteen Manager', 'pending'),
('demand', 'Q3 Grocery Demand Note', 210000.00, 'Store Supervisor', 'pending');

INSERT INTO activity (title, detail, kind) VALUES
('Source file processed', 'inventory_delhi_cantt_aug.csv \u00b7 612 rows', 'import'),
('Expense recorded', 'Transport \u00b7 \u20b934,200', 'expense');
