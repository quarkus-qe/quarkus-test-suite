CREATE DATABASE IF NOT EXISTS app_scope_db;
CREATE DATABASE IF NOT EXISTS req_scope_db;
CREATE DATABASE IF NOT EXISTS own_conn_db;
GRANT ALL PRIVILEGES ON app_scope_db.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON req_scope_db.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON own_conn_db.* TO 'user'@'%';
