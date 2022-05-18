docker build -t nginx-proxy .
docker tag nginx-proxy quay.io/quarkusqeteam/proxy:latest
docker push quay.io/quarkusqeteam/proxy:latest
