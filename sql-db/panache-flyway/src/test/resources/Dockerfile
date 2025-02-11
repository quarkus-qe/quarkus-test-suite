#!/usr/bin/env -S podman build . --tag quay.io/quarkusqeteam/wait:0.0.2 --file
FROM ubi9:latest

COPY <<EOF /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://pkgs.k8s.io/core:/stable:/v1.29/rpm/
enabled=1
gpgcheck=1
gpgkey=https://pkgs.k8s.io/core:/stable:/v1.29/rpm/repodata/repomd.xml.key
EOF
RUN dnf -y install kubectl

ADD --chmod=777 https://raw.githubusercontent.com/groundnuty/k8s-wait-for/master/wait_for.sh /usr/local/bin/wait_for.sh

ENTRYPOINT ["wait_for.sh"]