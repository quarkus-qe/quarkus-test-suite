package io.quarkus.ts.hibernate.startup.offline.test;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import io.quarkus.test.bootstrap.LocalhostManagedResource;
import io.quarkus.test.bootstrap.ManagedResource;
import io.quarkus.test.bootstrap.ServiceContext;
import io.quarkus.test.configuration.Configuration;
import io.quarkus.test.configuration.PropertyLookup;
import io.quarkus.test.logging.Log;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Mount;
import io.quarkus.test.services.containers.ContainerManagedResourceBuilder;
import io.quarkus.test.services.containers.GenericDockerContainerManagedResource;
import io.quarkus.test.utils.DockerUtils;
import io.smallrye.common.os.OS;

public final class FixedPortResourceBuilder extends ContainerManagedResourceBuilder {

    private ServiceContext context;
    private Mount[] mounts;

    @Override
    public ServiceContext getContext() {
        return context;
    }

    @Override
    public ManagedResource build(ServiceContext context) {
        this.context = context;
        var managedResource = new FixedPortManagedResource(this, () -> mounts);
        if (OS.WINDOWS.isCurrent()) {
            return new LocalhostManagedResource(managedResource);
        }
        return managedResource;
    }

    @Override
    public void init(Annotation annotation) {
        super.init(annotation);
        Container metadata = (Container) annotation;
        mounts = metadata.mounts();
    }

    private static final class FixedPortManagedResource extends GenericDockerContainerManagedResource {

        private static final PropertyLookup DB2_IMAGE_PROPERTY = new PropertyLookup("db2.image");
        private static final PropertyLookup MARIADB_IMAGE_PROPERTY = new PropertyLookup("mariadb.11.image");
        private final FixedPortResourceBuilder model;
        private final Supplier<Mount[]> mounts;

        private FixedPortManagedResource(FixedPortResourceBuilder model, Supplier<Mount[]> mounts) {
            super(model);
            this.model = model;
            this.mounts = mounts;
        }

        @Override
        protected GenericContainer<?> initContainer() {
            var container = new FixedPortContainer(model.getImage());

            container.setFixedPort(model.getPort());

            if (StringUtils.isNotBlank(model.getExpectedLog())) {
                container.waitingFor(new LogMessageWaitStrategy().withRegEx(".*" + model.getExpectedLog() + ".*\\s"));
            }

            if (model.getCommand() != null && model.getCommand().length > 0) {
                container.withCommand(model.getCommand());
            }

            if (isDb2Image()
                    || model.getContext().getOwner().getConfiguration().isTrue(Configuration.Property.PRIVILEGED_MODE)) {
                Log.info(model.getContext().getOwner(), "Running container on Privileged mode");
                container.setPrivilegedMode(true);
            }

            container.withCreateContainerCmdModifier(cmd -> cmd.withName(DockerUtils.generateDockerContainerName()));

            boolean requiresDynamicMounting = isRedHatMariaDbImage();
            if (requiresDynamicMounting) {
                // TODO: drop this when https://issues.redhat.com/browse/QUARKUS-5984 is resolved
                //   it adapts mounting paths when we use MariaDB 10.11 image provided by Red Hat
                //   and activated by our POM.xml profile in FIPS-enabled environment
                Log.info(
                        "Detected Red Hat MariaDB container image, mounting the database config to the '/etc/my.cnf.d/my.cnf' location instead");
                container.withClasspathResourceMapping("mysql-init.sql", "/tmp/init.sql", BindMode.READ_ONLY,
                        SelinuxContext.SHARED);
                container.withClasspathResourceMapping("mysql-my-conf.config", "/etc/my.cnf.d/my.cnf", BindMode.READ_ONLY,
                        SelinuxContext.SHARED);
            } else {
                if (mounts.get() != null) {
                    for (var mount : mounts.get()) {
                        Log.info(model.getContext().getOwner(), "Mounting " + mount.from() + " to " + mount.to());
                        container.withClasspathResourceMapping(mount.from(), mount.to(), BindMode.READ_ONLY,
                                SelinuxContext.SHARED);
                    }
                }
            }

            return container;
        }

        private boolean isDb2Image() {
            return DB2_IMAGE_PROPERTY.get().equals(model.getImage());
        }

        private boolean isRedHatMariaDbImage() {
            String mariaDbImage = MARIADB_IMAGE_PROPERTY.get();
            return mariaDbImage.equals(model.getImage()) && mariaDbImage.contains("redhat");
        }
    }

    private static final class FixedPortContainer extends GenericContainer<FixedPortContainer> {
        public FixedPortContainer(String dockerImageName) {
            super(dockerImageName);
        }

        private void setFixedPort(int port) {
            addFixedExposedPort(port, port);
        }
    }
}
