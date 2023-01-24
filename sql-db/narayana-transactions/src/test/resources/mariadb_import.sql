INSERT INTO account (id, name, lastName, accountNumber, amount, updatedAt, createdAt) VALUES (0, 'Garcilaso', 'de la Vega', 'CZ9250512252717368964232', 100, null, CURRENT_TIMESTAMP);
INSERT INTO account (id, name, lastName, accountNumber, amount, updatedAt, createdAt) VALUES (1, 'Miguel', 'de Cervantes', 'SK0389852379529966291984', 100, null, CURRENT_TIMESTAMP);
INSERT INTO account (id, name, lastName, accountNumber, amount, updatedAt, createdAt) VALUES (2, 'Luis', 'de Góngora', 'ES8521006742088984966816', 100, null, CURRENT_TIMESTAMP);
INSERT INTO account (id, name, lastName, accountNumber, amount, updatedAt, createdAt) VALUES (3, 'Lope', 'de Vega', 'FR9317569000409377431694J37', 100, null, CURRENT_TIMESTAMP);
INSERT INTO account (id, name, lastName, accountNumber, amount, updatedAt, createdAt) VALUES (5, 'Francisco', 'Quevedo', 'ES8521006742088984966817', 100, null, CURRENT_TIMESTAMP);

UPDATE hibernate_sequence SET next_val = 5;
