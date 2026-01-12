-- XmlValidatedCustomer is not the thing we're testing we need to have this entity because of validation
create table XmlValidatedCustomer (
    id number(19,0) not null,
    email varchar2(255 char),
    name varchar2(50 char) not null,
    primary key (id)
);
create sequence XmlValidatedCustomer_SEQ start with 1 increment by 50;


create sequence fruit_seq start with 1 increment by 50;
create table fruit (
  id number(10,0) not null,
  something_name nvarchar2(20) not null,
  primary key (id)
);

INSERT INTO fruit(id, something_name) VALUES (1, 'Cherry');
INSERT INTO fruit(id, something_name) VALUES (2, 'Apple');
INSERT INTO fruit(id, something_name) VALUES (3, 'Banana');
ALTER SEQUENCE fruit_seq RESTART start with 4;