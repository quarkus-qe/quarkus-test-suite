DROP TABLE IF EXISTS cars;
CREATE TABLE cars (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(31) NOT NULL, PRIMARY KEY(id));
INSERT INTO cars(id,name) VALUES (1, 'BMW');
INSERT INTO cars(id,name) VALUES (2, 'Audi');
INSERT INTO cars(id,name) VALUES (3, 'Volkswagen');
INSERT INTO cars(id,name) VALUES (4, 'Kia');