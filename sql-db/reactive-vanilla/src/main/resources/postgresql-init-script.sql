DROP TABLE IF EXISTS softCoverBook;
CREATE TABLE softCoverBook (id SERIAL PRIMARY KEY, title TEXT NOT NULL, author TEXT NOT NULL);
INSERT INTO softCoverBook (title, author) VALUES ('Foundation', 'Isaac Asimov');
INSERT INTO softCoverBook (title, author) VALUES ('2001: A Space Odyssey', 'Arthur C. Clarke');
INSERT INTO softCoverBook (title, author) VALUES ('Stranger in a Strange Land', 'Robert A. Heinlein');