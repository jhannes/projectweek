CREATE TABLE Issue_Worklogs (
id SERIAL PRIMARY KEY,
issue_id INTEGER NOT NULL,
work_started_at TIMESTAMP with time zone not null,
worker TEXT not null,
seconds_worked INTEGER NOT NULL
)