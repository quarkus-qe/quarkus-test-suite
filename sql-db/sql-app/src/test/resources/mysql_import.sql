INSERT INTO book (id, title, author) SELECT IFNULL(MAX(id) + 1, 1), 'Foundation', 'Isaac Asimov' FROM book;
INSERT INTO book (id, title, author) SELECT MAX(id) + 1, '2001: A Space Odyssey', 'Arthur C. Clarke' FROM book;
INSERT INTO book (id, title, author) SELECT MAX(id) + 1, 'Stranger in a Strange Land', 'Robert A. Heinlein' FROM book;
INSERT INTO book (id, title, author) SELECT MAX(id) + 1, 'Ender''s Game', 'Orson Scott Card' FROM book;
INSERT INTO book (id, title, author) SELECT MAX(id) + 1, 'Hyperion', 'Dan Simmons' FROM book;
INSERT INTO book (id, title, author) SELECT MAX(id) + 1, 'Anathem', 'Neal Stephenson' FROM book;
INSERT INTO book (id, title, author) SELECT MAX(id) + 1, 'Perdido Street Station', 'China Miéville' FROM book;
UPDATE SEQ_Book SET next_val=(SELECT max(id) + 1 FROM book);