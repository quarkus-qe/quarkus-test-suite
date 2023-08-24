echo "Installing Quarkus CLI"
jbang app install --force --name quarkus "io.quarkus:quarkus-cli:${QUARKUS_VERSION}:runner"
if "${HOME}/.jbang/bin/quarkus" --version; then
  echo "Quarkus CLI installed successfully"
else
  echo "Failed to install Quarkus CLI"
  exit 1
fi
