CREATE TABLE ExecutionEntity
(
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  seconds       BIGINT NOT NULL,
  owner         VARCHAR(100) NOT NULL
);