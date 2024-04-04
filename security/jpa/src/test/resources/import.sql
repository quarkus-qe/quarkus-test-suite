INSERT INTO md5_entity (id, username, password, role) VALUES (2, 'admin', '21232f297a57a5a743894a0e4a801fc3', 'admin');
INSERT INTO md5_entity (id, username, password, role) VALUES (3, 'user','ee11cbb19052e40b07aac0ca060c23ee', 'user');

INSERT INTO sha256_entity (id, username, password, role) VALUES (2, 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'admin');
INSERT INTO sha256_entity (id, username, password, role) VALUES (3, 'user','04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb', 'user');

INSERT INTO sha512_entity (id, username, password, role) VALUES (2, 'admin', 'c7ad44cbad762a5da0a452f9e854fdc1e0e7a52a38015f23f3eab1d80b931dd472634dfac71cd34ebc35d16ab7fb8a90c81f975113d6c7538dc69dd8de9077ec', 'admin');
INSERT INTO sha512_entity (id, username, password, role) VALUES (3, 'user','b14361404c078ffd549c03db443c3fede2f3e534d73f78f77301ed97d4a436a9fd9db05ee8b325c0ad36438b43fec8510c204fc1c1edb21d0941c00e9e2c1ce2', 'user');

INSERT INTO test_user (id, username, password) VALUES (1, 'admin', 'admin');
INSERT INTO test_user (id, username, password) VALUES (2, 'user','user');

INSERT INTO test_role (id, role_name) VALUES (1, 'admin');
INSERT INTO test_role (id, role_name) VALUES (2, 'user');

INSERT INTO test_user_role (user_id, role_id) VALUES (1, 1);
INSERT INTO test_user_role (user_id, role_id) VALUES (2, 2);