INSERT INTO account (id, name, lastName, accountNumber, amount, updatedAt, createdAt) VALUES (NEXT VALUE FOR Account_SEQ, 'Garcilaso', 'de la Vega', 'CZ9250512252717368964232', 100, null, CURRENT_TIMESTAMP);
INSERT INTO account (id, name, lastName, accountNumber, amount, updatedAt, createdAt) VALUES (NEXT VALUE FOR Account_SEQ, 'Miguel', 'de Cervantes', 'SK0389852379529966291984', 100, null, CURRENT_TIMESTAMP);
INSERT INTO account (id, name, lastName, accountNumber, amount, updatedAt, createdAt) VALUES (NEXT VALUE FOR Account_SEQ, 'Luis', 'de Góngora', 'ES8521006742088984966816', 100, null, CURRENT_TIMESTAMP);
INSERT INTO account (id, name, lastName, accountNumber, amount, updatedAt, createdAt) VALUES (NEXT VALUE FOR Account_SEQ, 'Lope', 'de Vega', 'FR9317569000409377431694J37', 100, null, CURRENT_TIMESTAMP);
INSERT INTO account (id, name, lastName, accountNumber, amount, updatedAt, createdAt) VALUES (NEXT VALUE FOR Account_SEQ, 'Francisco', 'Quevedo', 'ES8521006742088984966817', 100, null, CURRENT_TIMESTAMP);
INSERT INTO account (id, name, lastName, accountNumber, amount, updatedAt, createdAt) VALUES (NEXT VALUE FOR account_SEQ, 'Eduardo', 'Mendoza', 'ES8521006742088984966899', 100, null, CURRENT_TIMESTAMP);

INSERT INTO client (id, name, lastName, account_number) VALUES (100, 'Francisco', 'Quevedo', 'ES8521006742088984966817');
INSERT INTO client (id, name, lastName, account_number) VALUES (101, 'Eduardo', 'Mendoza', 'ES8521006742088984966899');

CREATE TABLE recovery_log (id INT);