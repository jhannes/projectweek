CREATE TABLE issues (
id SERIAL PRIMARY KEY,
key text not null,
project_key text not null,
updated_at TIMESTAMP with time zone not null
)
