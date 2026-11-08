create table ref_status
(
    code                     varchar(10)  primary key,
    name                     varchar(255) not null,
    gk_code                  varchar(1)   not null,
    gk_name                  varchar(255) not null,
    valid_for_person         boolean      not null,
    valid_for_organisation   boolean      not null,
    valid_for_multi_customer boolean      not null
);

create table party
(
    party_id         varchar(10) not null,
    start_date       date        not null,
    end_date         date        not null,
    status           varchar(10) not null references ref_status (code),
    status_date      date,
    soft_delete      boolean     not null default false,
    soft_delete_date date,
    origin           varchar(3)  not null,
    constraint pk_party primary key (party_id, end_date)
    -- note, constraint uq_party_id_end_date is not allowed with partitions, must include end_date, which makes the constraint redundant
    -- constraint uq_party_id_end_date unique (party_id, start_date)
) partition by range (end_date);

create table  party_c partition of party
    for values from ('9999-12-31') to (maxvalue);

create table  party_h partition of party
    for values from (minvalue) to ('9999-12-31');

create index partyx01 on party (party_id, start_date);

create sequence party_id_seq start with 1 increment by 1;
