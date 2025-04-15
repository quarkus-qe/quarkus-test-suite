# Reclaim ~30 GB disk space, otherwise we do not have enough disk space for TS execution
df -h /
free -h

#Filesystem      Size  Used Avail Use% Mounted on
# /dev/root        72G   48G   25G  67% /
# =>
#/dev/root        72G   18G   54G  25% /

sudo du -sh /* || true
echo "# docker images"
docker images || true
echo "# du -sh /var/lib/*"
sudo du -sh /var/lib/* || true
echo "# du -sh /opt/hostedtoolcache/*"
sudo du -sh /opt/hostedtoolcache/* || true
echo "# du -sh /imagegeneration/installers/*"
sudo du -sh /imagegeneration/installers/* || true



du -cskh /opt/* || true
du -cskh /usr/local/.ghcup || true

du -cskh /usr/share/miniconda || true
du -cskh /usr/local/share/powershell || true
du -cskh /usr/lib/google-cloud-sdk || true


echo "Reclaim disk space."

time sudo docker image prune --all --force || true

du -cskh /usr/share/swift /usr/share/gradle-*
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

du -cskh /opt/hostedtoolcache/PyPy /opt/hostedtoolcache/Python /opt/hostedtoolcache/Ruby /opt/hostedtoolcache/go /opt/hostedtoolcache/node
sudo rm -rf /opt/hostedtoolcache/PyPy
sudo rm -rf /opt/hostedtoolcache/Python
sudo rm -rf /opt/hostedtoolcache/Ruby
sudo rm -rf /opt/hostedtoolcache/go
sudo rm -rf /opt/hostedtoolcache/node

echo "Reclaim disk space end."

df -h /
free -h
