# Permissions
This module covers the TP described into https://github.com/quarkus-qe/quarkus-test-plans/blob/main/QUARKUS-5622.md and
https://github.com/quarkus-qe/quarkus-test-plans/blob/main/QUARKUS-5614
## Required key generation steps to the JwtBeanParamPermissionIT

```shell
openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -keyout private-key.pem -out public-key.pem
```