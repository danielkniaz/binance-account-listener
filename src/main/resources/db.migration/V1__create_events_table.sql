CREATE TABLE if not exists
    events (
    id SERIAL primary key ,
    created bigint not null,
    data jsonb
);