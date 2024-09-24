DROP TABLE IF EXISTS hardCoverBook;
CREATE TABLE hardCoverBook (id SERIAL PRIMARY KEY, title TEXT NOT NULL, author TEXT NOT NULL);
INSERT INTO hardCoverBook (title, author) VALUES ('Foundation', 'Isaac Asimov');
INSERT INTO hardCoverBook (title, author) VALUES ('2001: A Space Odyssey', 'Arthur C. Clarke');
INSERT INTO hardCoverBook (title, author) VALUES ('Stranger in a Strange Land', 'Robert A. Heinlein');
