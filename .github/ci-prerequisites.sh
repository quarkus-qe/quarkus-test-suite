# Reclaim disk space, otherwise we only have 13 GB free at the start of a job
echo "Reclaim disk space."
df -h
docker images
time docker rmi node:12 node:14 node:16 buildpack-deps:stretch buildpack-deps:buster buildpack-deps:bullseye ubuntu:18.04 ubuntu:16.04 debian:10 debian:11 debian:9 moby/buildkit node:16-alpine node:14-alpine node:12-alpine alpine:3.14 alpine:3.15 alpine:3.16

du -cskh /usr/share/dotnet /usr/share/swift
time sudo rm -rf /usr/share/dotnet
time sudo rm -rf /usr/share/swift

echo "Reclaim disk space end."
df -h
docker images
