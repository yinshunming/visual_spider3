-- H2 PostgreSQL-compatible schema for testing

-- Spider tasks table
CREATE TABLE IF NOT EXISTS spider_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    url_mode VARCHAR(50) NOT NULL,
    list_page_url VARCHAR(2048),
    list_page_rule TEXT,
    seed_urls TEXT,
    content_page_rule TEXT,
    schedule_cron VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Spider fields table
CREATE TABLE IF NOT EXISTS spider_fields (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_label VARCHAR(255),
    field_type VARCHAR(50) NOT NULL,
    selector VARCHAR(500),
    selector_type VARCHAR(50),
    extract_type VARCHAR(50),
    attr_name VARCHAR(100),
    required BOOLEAN DEFAULT FALSE,
    default_value VARCHAR(500),
    display_order INT
);

-- Content items table
CREATE TABLE IF NOT EXISTS content_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    source_url VARCHAR(2048),
    fields TEXT,
    raw_html TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    published_at TIMESTAMP,
    created_at TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_spider_fields_task_id ON spider_fields(task_id);
CREATE INDEX IF NOT EXISTS idx_content_items_task_id ON content_items(task_id);
CREATE INDEX IF NOT EXISTS idx_content_items_status ON content_items(status);