create table work_items (
	id serial primary key,
    created_at timestamp with time zone not null,
    created_on_host varchar(40) not null,
    start_after timestamp with time zone not null,
    started_at timestamp with time zone null,
    started_on_host varchar(40) null,
    completed_at timestamp with time zone null,
    completed_on_host varchar(40) null,
    status int not null
);
