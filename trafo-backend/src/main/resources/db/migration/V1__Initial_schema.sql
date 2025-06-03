-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'ENGINEER', 'OPERATOR', 'VIEWER')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transformers Table
CREATE TABLE transformers (
    id BIGSERIAL PRIMARY KEY,
    nama_trafo VARCHAR(100) NOT NULL,
    kode_trafo VARCHAR(50) UNIQUE NOT NULL,
    lokasi VARCHAR(200) NOT NULL,
    kapasitas_kva DECIMAL(10,2) NOT NULL,
    tegangan_kv DECIMAL(8,2) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPERATIONAL', 'MAINTENANCE', 'FAULT', 'OFFLINE')),
    priority VARCHAR(10) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    progress INTEGER DEFAULT 0 CHECK (progress >= 0 AND progress <= 100),
    pic_engineer BIGINT REFERENCES users(id),
    tanggal_mulai DATE,
    tanggal_target DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Status History Table
CREATE TABLE status_history (
    id BIGSERIAL PRIMARY KEY,
    transformer_id BIGINT NOT NULL REFERENCES transformers(id) ON DELETE CASCADE,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    changed_by BIGINT NOT NULL REFERENCES users(id),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- Work Orders Table
CREATE TABLE work_orders (
    id BIGSERIAL PRIMARY KEY,
    transformer_id BIGINT NOT NULL REFERENCES transformers(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    assigned_to BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- Notifications Table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(20) DEFAULT 'INFO' CHECK (type IN ('INFO', 'WARNING', 'ERROR', 'SUCCESS')),
    read_status BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better performance
CREATE INDEX idx_transformers_status ON transformers(status);
CREATE INDEX idx_transformers_pic_engineer ON transformers(pic_engineer);
CREATE INDEX idx_status_history_transformer_id ON status_history(transformer_id);
CREATE INDEX idx_status_history_changed_at ON status_history(changed_at);
CREATE INDEX idx_work_orders_transformer_id ON work_orders(transformer_id);
CREATE INDEX idx_work_orders_assigned_to ON work_orders(assigned_to);
CREATE INDEX idx_work_orders_status ON work_orders(status);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read_status ON notifications(read_status);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers to auto-update updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transformers_updated_at BEFORE UPDATE ON transformers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
