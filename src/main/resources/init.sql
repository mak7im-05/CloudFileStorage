CREATE TABLE  person (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

INSERT INTO person (username, password) VALUES ('user1', 'password1');
INSERT INTO person (username, password) VALUES ('user2', 'password2');
INSERT INTO person (username, password) VALUES ('user3', 'password3');
