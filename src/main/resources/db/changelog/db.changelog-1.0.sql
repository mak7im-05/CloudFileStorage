--liquibase formatted sql

--changeset maxim:1
CREATE TABLE person
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255)        NOT NULL
);