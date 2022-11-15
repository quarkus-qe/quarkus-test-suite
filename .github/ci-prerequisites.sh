# Reclaim ~14 GB disk space, otherwise we do not have enough disk space for TS execution
echo "Reclaim disk space."
df -h /
docker images
time docker rmi node:12 node:14 node:16 buildpack-deps:stretch buildpack-deps:buster buildpack-deps:bullseye ubuntu:18.04 ubuntu:16.04 debian:10 debian:11 debian:9 moby/buildkit node:16-alpine node:14-alpine node:12-alpine alpine:3.14 alpine:3.15 alpine:3.16

du -cskh /usr/share/dotnet /usr/share/swift /usr/share/gradle-7.5.1
sudo rm -rf /usr/share/dotnet
sudo rm -rf /usr/share/swift
sudo rm -rf /usr/share/gradle-7.5.1

du -cskh /opt/az /opt/google /opt/hhvm /opt/hostedtoolcache/CodeQL /opt/microsoft /usr/local/graalvm /usr/local/julia*
sudo rm -rf /opt/az
sudo rm -rf /opt/google
sudo rm -rf /opt/hhvm
sudo rm -rf /opt/hostedtoolcache/CodeQL
sudo rm -rf /opt/microsoft
sudo rm -rf /usr/local/graalvm
sudo rm -rf /usr/local/julia*

echo "Reclaim disk space end."
df -h /
docker images
