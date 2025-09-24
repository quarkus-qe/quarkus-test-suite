INSERT INTO address (id, street, city) VALUES (1, 'Dejvicka', 'Prague');
INSERT INTO address (id, street, city) VALUES (2, 'Vlnena', 'Brno');
INSERT INTO address (id, street, city) VALUES (3, 'Krancowa', 'Poznan');

INSERT INTO orders (id, customerName, address_id) VALUES (1, 'Alice', 1);
INSERT INTO orders (id, customerName, address_id) VALUES (2, 'Bob', 2);
INSERT INTO orders (id, customerName, address_id) VALUES (3, 'Charlie', 3);

INSERT INTO order_item (id, productName, quantity, orders_id) VALUES (1, 'Laptop', 1, 1);
INSERT INTO order_item (id, productName, quantity, orders_id) VALUES (2, 'Mouse', 2, 1);
INSERT INTO order_item (id, productName, quantity, orders_id) VALUES (3, 'Keyboard', 1, 1);
INSERT INTO order_item (id, productName, quantity, orders_id) VALUES (4, 'Monitor', 2, 2);

INSERT INTO order_item (id, productName, quantity, orders_id) VALUES (5, 'Phone', 3, 2);
INSERT INTO order_item (id, productName, quantity, orders_id) VALUES (6, 'Tablet', 1, 2);
INSERT INTO order_item (id, productName, quantity, orders_id) VALUES (7, 'Headphones', 1, 3);
INSERT INTO order_item (id, productName, quantity, orders_id) VALUES (8, 'Printer', 1, 3);

INSERT INTO order_item (id, productName, quantity, orders_id) VALUES (9, 'Camera', 1, 3);
