CREATE TABLE if not exists
    account_info
(
    id SERIAL primary key ,
    created bigint not null,
    data jsonb,
    type varchar(255),
    event jsonb
);
