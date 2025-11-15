CREATE TABLE parents(
    parent_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE kids(
    kid_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL
);

CREATE TABLE child_access_tokens(
    token_id SERIAL PRIMARY KEY,
    pin VARCHAR(255) NOT NULL,
    qr_hash VARCHAR(255) NOT NULL,
    parent_id INT NOT NULL REFERENCES parents(parent_id) ON DELETE CASCADE,
    kid_id INT NOT NULL REFERENCES kids(kid_id) ON DELETE CASCADE
);

CREATE TABLE parents_kids(
    parent_id NOT NULL INT REFERENCES parents(parent_id) ON DELETE CASCADE,
    kid_id NOT NULL INT REFERENCES kids(kid_id) ON DELETE CASCADE,
    PRIMARY KEY(parent_id, kid_id)
);

CREATE TABLE tasks(
    task_id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    task_start TIMESTAMP NOT NULL,
    task_end TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL,
    note VARCHAR(255),
    parent_id INT NOT NULL REFERENCES parents(parent_id) ON DELETE CASCADE
);

CREATE TABLE kids_tasks(
    task_id INT NOT NULL REFERENCES tasks(task_id) ON DELETE CASCADE,
    parent_id INT NOT NULL REFERENCES parents(parent_id) ON DELETE CASCADE,
    kid_id INT NOT NULL REFERENCES kids(kid_id) ON DELETE CASCADE,
);

CREATE TABLE kids_suggestions(
    suggestion_id SERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    proposed_start TIMESTAMP NOT NULL,
    proposed_end TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP,
    reviewed_by INT REFERENCES parents(parent_id) ON DELETE SET NULL,
    created_by INT NOT NULL REFERENCES kids(kid_id) ON DELETE CASCADE
);

CREATE TABLE media_gallery(
    media_id SERIAL PRIMARY KEY,
    media_type VARCHAR(50) NOT NULL,
    url VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    parent_id INT REFERENCES parents(parent_id) ON DELETE CASCADE,
    kid_id INT REFERENCES kids(kid_id) ON DELETE CASCADE
);

CREATE TABLE messages (
    message_id SERIAL PRIMARY KEY,
    sender_type VARCHAR(10) NOT NULL CHECK (sender_type IN ('PARENT', 'KID')),
    sender_id INT NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL
);

CREATE OR REPLACE FUNCTION update_statuses()
RETURNS void AS $$
BEGIN
UPDATE tasks
SET status = 'MISSED'
WHERE status = 'PENDING'
  AND task_end < (NOW() AT TIME ZONE 'Europe/Warsaw');

UPDATE kids_suggestions
SET status = 'REJECTED'
WHERE status = 'PENDING'
  AND proposed_end < (NOW() AT TIME ZONE 'Europe/Warsaw');
END;
$$ LANGUAGE plpgsql;

CREATE SEQUENCE parent_id_seq START WITH 1 INCREMENT BY 1;