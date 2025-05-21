-- Таблица для сущности Document
CREATE TABLE document
(
    kks             VARCHAR(100) PRIMARY KEY,
    revision        VARCHAR(100),
    title           VARCHAR(100),
    number_of_pages INTEGER
);

-- Таблица для сущности PrimaryDocument
CREATE TABLE primary_document
(
    id              SERIAL PRIMARY KEY,
    title           VARCHAR(200),
    number_of_pages INTEGER
);

-- Таблица для сущности User
CREATE TABLE users
(
    user_id  SERIAL PRIMARY KEY,
    name     VARCHAR(50),
    password VARCHAR(150),
    roles    VARCHAR(50)
);

