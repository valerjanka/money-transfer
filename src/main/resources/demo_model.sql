DROP TABLE IF EXISTS user;

-- sample User table with only id and name, as we are not working with it
CREATE TABLE user (
id LONG PRIMARY KEY NOT NULL,
name VARCHAR(50) NOT NULL
);

INSERT INTO user (id, name) VALUES (1, 'user1');
INSERT INTO user (id, name) VALUES (2, 'user2');

DROP TABLE IF EXISTS account;

CREATE TABLE account (
id LONG PRIMARY KEY AUTO_INCREMENT NOT NULL,
userId LONG NOT NULL,
balance DECIMAL(16,2),
currency VARCHAR(10),
FOREIGN KEY(userId) REFERENCES user(id)
);

INSERT INTO account (userId,balance,currency) VALUES (1,1000.00,'USD');
INSERT INTO account (userId,balance,currency) VALUES (1,2000.00,'EUR');
INSERT INTO account (userId,balance,currency) VALUES (2,1000.00,'USD');
INSERT INTO account (userId,balance,currency) VALUES (2,2000.00,'EUR');
INSERT INTO account (userId,balance,currency) VALUES (2,1.00,'EUR');


