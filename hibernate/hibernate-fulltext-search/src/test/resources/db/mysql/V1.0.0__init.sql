CREATE TABLE known_fruits (id INT PRIMARY KEY AUTO_INCREMENT,name VARCHAR(40), price FLOAT);
CREATE TABLE known_vegetables (id INT PRIMARY KEY,name VARCHAR(40));
CREATE TABLE fruit_producer (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(40) UNIQUE, fruit_id INT, PRIMARY KEY (id), CONSTRAINT FK_fruit_producer_fruit_id FOREIGN KEY (fruit_id) REFERENCES known_fruits (id));
