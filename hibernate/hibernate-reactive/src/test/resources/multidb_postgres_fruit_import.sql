DROP TABLE IF EXISTS fruits;
CREATE TABLE fruits(id SERIAL PRIMARY KEY, name TEXT NOT NULL);
INSERT INTO fruits(id,name) VALUES (1, 'Apple');
INSERT INTO fruits(id,name) VALUES (2, 'Banana');
INSERT INTO fruits(id,name) VALUES (3, 'Orange');
