# Reclaim ~32 GB disk space, otherwise we do not have enough disk space for TS execution
df -h /
free -h

echo "Reclaim disk space."

sudo docker image prune --all --force || true

du -cskh /usr/share/swift /usr/share/gradle-* /usr/share/miniconda
sudo rm -rf /usr/share/swift
sudo rm -rf /usr/share/gradle-*
sudo rm -rf /usr/share/miniconda

du -cskh /opt/az /opt/google /opt/microsoft /opt/pipx
sudo rm -rf /opt/az
sudo rm -rf /opt/google
sudo rm -rf /opt/microsoft
sudo rm -rf /opt/pipx

du -cskh /usr/local/lib/android /usr/local/julia* /usr/local/.ghcup /usr/local/share/powershell /usr/lib/google-cloud-sdk
sudo rm -rf /usr/local/lib/android
sudo rm -rf /usr/local/julia*
sudo rm -rf /usr/local/.ghcup || true
sudo rm -rf /usr/local/share/powershell || true
sudo rm -rf /usr/lib/google-cloud-sdk || true

du -cskh /opt/hostedtoolcache/CodeQL /opt/hostedtoolcache/PyPy /opt/hostedtoolcache/Python /opt/hostedtoolcache/Ruby /opt/hostedtoolcache/go /opt/hostedtoolcache/node
sudo rm -rf /opt/hostedtoolcache/CodeQL
sudo rm -rf /opt/hostedtoolcache/PyPy
sudo rm -rf /opt/hostedtoolcache/Python
sudo rm -rf /opt/hostedtoolcache/Ruby
sudo rm -rf /opt/hostedtoolcache/go
sudo rm -rf /opt/hostedtoolcache/node

echo "Reclaim disk space end."

df -h /
free -h
