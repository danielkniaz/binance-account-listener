CREATE TABLE if not exists
        signals
(
    id SERIAL primary key,
    created bigint not null,
    symbol varchar(255) not null,
    action varchar(255) not null,
    type varchar(255) not null,
    direction int not null default 0,
    is_in boolean,
    is_out boolean,
    price numeric(16,8),
    relative_price numeric(4,0),
    risk numeric(10,4)
);