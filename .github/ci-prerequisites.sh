# Reclaim ~14 GB disk space, otherwise we do not have enough disk space for TS execution
echo "Reclaim disk space."
df -h /
docker images
time docker rmi node:12 node:14 node:16 buildpack-deps:buster buildpack-deps:bullseye ubuntu:18.04 ubuntu:20.04 debian:10 debian:11 moby/buildkit node:16-alpine node:14-alpine alpine:3.16 alpine:3.17

du -cskh /usr/share/dotnet /usr/share/swift /usr/share/gradle-*
sudo rm -rf /usr/share/dotnet
sudo rm -rf /usr/share/swift
sudo rm -rf /usr/share/gradle-*

du -cskh /opt/az /opt/google /opt/hostedtoolcache/CodeQL /opt/microsoft /usr/local/julia*
sudo rm -rf /opt/az
sudo rm -rf /opt/google
sudo rm -rf /opt/hostedtoolcache/CodeQL
sudo rm -rf /opt/microsoft
sudo rm -rf /usr/local/julia*

du -cskh /usr/local/lib/android /opt/pipx
sudo rm -rf /usr/local/lib/android
sudo rm -rf /opt/pipx

du -cskh /imagegeneration/installers/*.tar.gz /opt/hostedtoolcache/PyPy /opt/hostedtoolcache/Python /opt/hostedtoolcache/Ruby /opt/hostedtoolcache/go /opt/hostedtoolcache/node
sudo rm -rf /imagegeneration/installers/*.tar.gz
sudo rm -rf /opt/hostedtoolcache/PyPy
sudo rm -rf /opt/hostedtoolcache/Python
sudo rm -rf /opt/hostedtoolcache/Ruby
sudo rm -rf /opt/hostedtoolcache/go
sudo rm -rf /opt/hostedtoolcache/node

echo "Reclaim disk space end."
df -h /
docker images
