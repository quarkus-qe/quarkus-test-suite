# Reclaim disk space, otherwise we only have 13 GB free at the start of a job
echo "Reclaim disk space."
df -h
docker images
time docker rmi node:10 node:12 node:14 node:16 buildpack-deps:stretch buildpack-deps:buster buildpack-deps:bullseye mcr.microsoft.com/azure-pipelines/node8-typescript:latest
time sudo rm -rf /usr/share/dotnet
time sudo rm -rf /usr/share/swift
echo "Reclaim disk space end."
df -h
docker images
