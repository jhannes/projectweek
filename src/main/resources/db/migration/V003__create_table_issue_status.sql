CREATE TABLE issue_status (
id SERIAL PRIMARY KEY,
issue_id INTEGER,
created_at TIMESTAMP with time zone not null,
status TEXT not null
)