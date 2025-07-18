CREATE TABLE "base".known_fruits
(
  id   INT PRIMARY KEY,
  name VARCHAR(40),
  price REAL
);

CREATE TABLE "company1".known_fruits
(
  id   INT PRIMARY KEY,
  name VARCHAR(40),
  price REAL
);

CREATE TABLE "company2".known_fruits
(
  id   INT PRIMARY KEY,
  name VARCHAR(40),
  price REAL
);
CREATE TABLE "base".fruit_producer (
 id SERIAL PRIMARY KEY,
 name VARCHAR(40) UNIQUE,
 fruit_id INTEGER,
 CONSTRAINT fk_fruit_producer_fruit_id_base FOREIGN KEY (fruit_id) REFERENCES "base".known_fruits (id)
);
CREATE TABLE "company1".fruit_producer (
 id SERIAL PRIMARY KEY,
 name VARCHAR(40) UNIQUE,
 fruit_id INTEGER,
 CONSTRAINT fk_fruit_producer_fruit_id_company1 FOREIGN KEY (fruit_id) REFERENCES "company1".known_fruits (id)
);
CREATE TABLE "company2".fruit_producer (
 id SERIAL PRIMARY KEY,
 name VARCHAR(40) UNIQUE,
 fruit_id INTEGER,
 CONSTRAINT fk_fruit_producer_fruit_id_company2 FOREIGN KEY (fruit_id) REFERENCES "company2".known_fruits (id)
);
CREATE TABLE "company1".known_vegetables
(
    id   INT PRIMARY KEY,
    name VARCHAR(40),
    description VARCHAR(40)
);
CREATE SEQUENCE "company1".known_vegetables_SEQ START 101;
