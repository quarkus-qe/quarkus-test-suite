# Security JPA test coverage
Use MariaDB, MySQL, OracleDB with MD5, SHA-256 and SHA-512 hashing algorithms for custom password providers.
 
## How to generate hashed passwords:
- MD5: `echo -n 'user-pass' | openssl md5`
- SHA256: `echo -n 'user-pass' | sha256sum`
- SHA512: `echo -n 'user-pass' | sha512sum`