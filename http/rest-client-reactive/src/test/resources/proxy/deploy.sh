VERSION=1.0.1
docker build -t nginx-proxy .
docker tag nginx-proxy quay.io/quarkusqeteam/proxy:latest
docker tag nginx-proxy quay.io/quarkusqeteam/proxy:$VERSION
docker push quay.io/quarkusqeteam/proxy:latest
docker push quay.io/quarkusqeteam/proxy:$VERSION
