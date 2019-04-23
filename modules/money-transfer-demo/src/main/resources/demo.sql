DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS account;

CREATE TABLE user (id LONG PRIMARY KEY NOT NULL,
 name VARCHAR(30) NOT NULL);

INSERT INTO User (id, name) VALUES (1, 'test1');
INSERT INTO User (id, name) VALUES (2, 'test2');

CREATE TABLE account (accountNumber VARCHAR(16) PRIMARY KEY NOT NULL,
userId LONG,
balance DECIMAL(16,2),
currency VARCHAR(10)
);

CREATE UNIQUE INDEX idx_acc_accountNumber on account(accountNumber);

INSERT INTO Account (accountNumber,userId,balance,currency) VALUES ('111',1, 500.00,'USD');
INSERT INTO Account (accountNumber,userId,balance,currency) VALUES ('112',1, 200.00,'USD');
INSERT INTO Account (accountNumber,userId,balance,currency) VALUES ('211',2, 500.00,'USD');
INSERT INTO Account (accountNumber,userId,balance,currency) VALUES ('212',2, 200.00,'USD');
