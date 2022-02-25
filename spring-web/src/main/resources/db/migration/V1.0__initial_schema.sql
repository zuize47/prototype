-- create User table
create table "users"
(
    "id" bigint
        constraint user_pk
            primary key,
    "username" varchar(50) not null,
    "password" varchar(120) not null,
    "email" varchar(50)
);

create unique index user_email_uindex
    on "users" ("email");

create unique index user_username_uindex
    on "users" ("username");

-- create Role
create table "roles"
(
    "id" bigint
        constraint roles_pk
            primary key,
    "name" varchar(30)
);

create unique index "roles_name_uindex"
    on "roles" ("name");


CREATE SEQUENCE user_sequence
    start 1000
    increment 2;


CREATE SEQUENCE role_sequence
    start 100
    increment 2;

create table "user_roles"
(
    "user_id" bigint,
    "role_id" bigint,

    constraint user_role_pk
        primary key ("user_id", "role_id"),
    constraint role_fk
        FOREIGN KEY("user_id")
            REFERENCES "users"("id"),
    constraint user_fk
        FOREIGN KEY("role_id")
            REFERENCES "roles"("id")

);