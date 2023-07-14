insert into account (id, email) values (1, 'foo@bar.com');
insert into role (id, name) values  (1, 'admin');
insert into account_in_role (accountid, roleid) values (1, 1);
insert into customer (id, version, account_id) values (1,  1, 1);
-- Table Item is created with customerId due to quote identifier strategy while unquoted Postgres
-- columns are transformed to lowercase, therefore we quote customer id column
insert into item (id, note, `customerId`) values (1, 'Item 1', 1);