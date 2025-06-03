-- Insert default users (password is "password123" hashed with BCrypt)
INSERT INTO users (username, password, email, role) VALUES 
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@pln.co.id', 'ADMIN'),
('engineer1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'engineer1@pln.co.id', 'ENGINEER'),
('operator1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'operator1@pln.co.id', 'OPERATOR'),
('viewer1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'viewer1@pln.co.id', 'VIEWER');

-- Insert sample transformers
INSERT INTO transformers (nama_trafo, kode_trafo, lokasi, kapasitas_kva, tegangan_kv, status, priority, progress, pic_engineer, tanggal_mulai, tanggal_target, notes) VALUES
('Trafo GI Jakarta Selatan 1', 'TRF-JKS-001', 'Gardu Induk Jakarta Selatan', 150000.00, 150.00, 'OPERATIONAL', 'HIGH', 100, 2, '2024-01-15', '2024-02-15', 'Trafo utama untuk wilayah Jakarta Selatan'),
('Trafo GI Bekasi 2', 'TRF-BKS-002', 'Gardu Induk Bekasi', 100000.00, 150.00, 'MAINTENANCE', 'MEDIUM', 75, 2, '2024-06-01', '2024-06-15', 'Maintenance rutin scheduled'),
('Trafo GI Tangerang 1', 'TRF-TNG-001', 'Gardu Induk Tangerang', 200000.00, 500.00, 'OPERATIONAL', 'CRITICAL', 100, 2, '2024-01-01', '2024-01-30', 'Trafo kritikal untuk industri'),
('Trafo GI Depok 1', 'TRF-DPK-001', 'Gardu Induk Depok', 75000.00, 150.00, 'FAULT', 'HIGH', 25, 2, '2024-06-03', '2024-06-10', 'Fault detected, under investigation');

-- Insert sample status history
INSERT INTO status_history (transformer_id, old_status, new_status, changed_by, notes) VALUES
(2, 'OPERATIONAL', 'MAINTENANCE', 1, 'Started scheduled maintenance'),
(4, 'OPERATIONAL', 'FAULT', 1, 'Fault detected during routine check');

-- Insert sample work orders
INSERT INTO work_orders (transformer_id, title, description, status, assigned_to) VALUES
(2, 'Routine Maintenance - Oil Change', 'Ganti oli transformator dan check isolasi', 'IN_PROGRESS', 3),
(4, 'Fault Investigation', 'Investigate reported fault and determine repair action', 'PENDING', 2),
(1, 'Annual Inspection', 'Annual comprehensive inspection', 'COMPLETED', 2);

-- Insert sample notifications
INSERT INTO notifications (user_id, title, message, type) VALUES
(2, 'Work Order Assigned', 'You have been assigned to investigate fault on TRF-DPK-001', 'INFO'),
(3, 'Maintenance Reminder', 'Routine maintenance for TRF-BKS-002 is due tomorrow', 'WARNING'),
(1, 'System Alert', 'Fault detected on transformer TRF-DPK-001', 'ERROR');
