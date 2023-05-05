DROP TABLE IF EXISTS noteBook;
CREATE TABLE noteBook (id SERIAL PRIMARY KEY, title TEXT NOT NULL, author TEXT NOT NULL);
INSERT INTO noteBook (title, author) VALUES ('Foundation', 'Isaac Asimov');
INSERT INTO noteBook (title, author) VALUES ('2001: A Space Odyssey', 'Arthur C. Clarke');
INSERT INTO noteBook (title, author) VALUES ('Stranger in a Strange Land', 'Robert A. Heinlein');