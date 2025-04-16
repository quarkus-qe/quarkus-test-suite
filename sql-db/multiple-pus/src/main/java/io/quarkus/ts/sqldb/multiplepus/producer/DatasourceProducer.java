package io.quarkus.ts.sqldb.multiplepus.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.arc.InjectableInstance;

public class DatasourceProducer {

    @Inject
    @DataSource("pg")
    InjectableInstance<AgroalDataSource> pgDataSourceBean;

    @Inject
    @DataSource("mariadb")
    InjectableInstance<AgroalDataSource> mariadbDataSourceBean;

    @Produces
    @ApplicationScoped
    public AgroalDataSource dataSource() {
        if (pgDataSourceBean.getHandle().getBean().isActive()) {
            return pgDataSourceBean.get();
        } else if (mariadbDataSourceBean.getHandle().getBean().isActive()) {
            return mariadbDataSourceBean.get();
        } else {
            throw new RuntimeException("No active datasource!");
        }
    }
}
