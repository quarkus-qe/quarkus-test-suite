# Reclaim disk space, otherwise we only have 13 GB free at the start of a job
echo "Reclaim disk space."
df -h /
docker images
time docker rmi node:12 node:14 node:16 buildpack-deps:stretch buildpack-deps:buster buildpack-deps:bullseye ubuntu:18.04 ubuntu:16.04 debian:10 debian:11 debian:9 moby/buildkit node:16-alpine node:14-alpine node:12-alpine alpine:3.14 alpine:3.15 alpine:3.16

du -cskh /usr/share/dotnet /usr/share/swift /usr/share/gradle-7.5.1
sudo rm -rf /usr/share/dotnet
sudo rm -rf /usr/share/swift
sudo rm -rf /usr/share/gradle-7.5.1


du -cskh /opt/az/*
du -cskh /opt/google/*
du -cskh /opt/hhvm/*
du -cskh /opt/hostedtoolcache/*
du -cskh /opt/microsoft/*

du -cskh /usr/local/graalvm/*
du -cskh /usr/local/julia1.8.2/*




echo "Reclaim disk space end."
df -h /
docker images
