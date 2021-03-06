CREATE TABLE airports (
  id            INT NOT NULL IDENTITY(1,1),
  iata_code     VARCHAR(100) NOT NULL,
  city          VARCHAR(100) NOT NULL,
  PRIMARY KEY(id)
);

CREATE TABLE airlines (
  id            INT NOT NULL IDENTITY(1,1),
  iata_code     VARCHAR(100) NOT NULL,
  name          VARCHAR(100) NOT NULL,
  infant_price  FLOAT(3),
  PRIMARY KEY(id)
);

CREATE TABLE flights (
  id            INT NOT NULL IDENTITY(1,1),
  origin        VARCHAR(100) NOT NULL,
  destination   VARCHAR(100) NOT NULL,
  flight_code   VARCHAR(100) NOT NULL,
  base_price    INTEGER NOT NULL,
  PRIMARY KEY(id)
);

CREATE TABLE pricingRules (
  id                     INT NOT NULL IDENTITY(1,1),
  days_to_departure      INTEGER NOT NULL,
  until                  INTEGER NOT NULL,
  percentage             INTEGER NOT NULL,
  PRIMARY KEY(id)
);

CREATE TABLE address (
  id                     INT NOT NULL IDENTITY(1,1),
  street                 VARCHAR(300) NOT NULL,
  block_number           VARCHAR(20) NOT NULL,
  zip_code               VARCHAR(20) NOT NULL,
  city                   VARCHAR(150) NOT NULL,
  country                VARCHAR(200) NOT NULL,
  created_at             BIGINT NOT NULL,
  updated_at             BIGINT,
  PRIMARY KEY(id)
);

CREATE TABLE passenger (
  id              INT NOT NULL IDENTITY(1,1),
  nif             VARCHAR(10) NOT NULL,
  name            VARCHAR(25) NOT NULL,
  last_name       VARCHAR(55) NOT NULL,
  contact_number  VARCHAR(20) NOT NULL,
  created_at      BIGINT NOT NULL,
  updated_at      BIGINT,
  address_id      INT,
  PRIMARY KEY(id),
  FOREIGN KEY (address_id) REFERENCES address(id) ON DELETE SET NULL
);

CREATE TABLE basket (
  id              INT NOT NULL IDENTITY(1,1),
  flight          VARCHAR(10) NOT NULL,
  price           NUMERIC NOT NULL,
  created_at      BIGINT NOT NULL,
  updated_at      BIGINT,
  passenger_id    INT,
  PRIMARY KEY(id),
  FOREIGN KEY (passenger_id) REFERENCES passenger(id) ON DELETE SET NULL
);
