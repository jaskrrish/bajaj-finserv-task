create table quiz_runs (
    id uuid primary key,
    reg_no varchar(64) not null,
    set_id varchar(128),
    status varchar(32) not null,
    polls_completed integer not null default 0,
    unique_events integer not null default 0,
    duplicate_events integer not null default 0,
    total_score integer not null default 0,
    failure_reason varchar(2000),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    completed_at timestamp with time zone
);

create table poll_messages (
    id uuid primary key,
    run_id uuid not null references quiz_runs(id) on delete cascade,
    poll_index integer not null,
    set_id varchar(128),
    events_count integer not null,
    raw_payload text not null,
    received_at timestamp with time zone not null
);

create table deduped_events (
    id uuid primary key,
    run_id uuid not null references quiz_runs(id) on delete cascade,
    round_id varchar(64) not null,
    participant varchar(255) not null,
    score integer not null,
    source_poll_index integer not null,
    ingested_at timestamp with time zone not null,
    constraint uk_deduped_events_run_round_participant unique (run_id, round_id, participant)
);

create table leaderboard_entries (
    id uuid primary key,
    run_id uuid not null references quiz_runs(id) on delete cascade,
    participant varchar(255) not null,
    total_score integer not null,
    rank_order integer not null
);

create table submission_records (
    id uuid primary key,
    run_id uuid not null unique references quiz_runs(id) on delete cascade,
    request_payload text not null,
    response_payload text not null,
    submitted_total integer not null,
    expected_total integer,
    is_correct boolean not null,
    is_idempotent boolean not null,
    message varchar(500) not null,
    submitted_at timestamp with time zone not null
);

create index idx_poll_messages_run_id_poll_index on poll_messages(run_id, poll_index);
create index idx_leaderboard_entries_run_id_rank_order on leaderboard_entries(run_id, rank_order);
