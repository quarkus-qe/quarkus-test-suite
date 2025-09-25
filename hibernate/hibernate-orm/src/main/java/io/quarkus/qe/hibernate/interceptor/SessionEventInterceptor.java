package io.quarkus.qe.hibernate.interceptor;

import org.hibernate.Interceptor;
import org.hibernate.type.Type;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.qe.hibernate.items.MyEntity;

@PersistenceUnitExtension("named")
public class SessionEventInterceptor implements Interceptor {

    private volatile MyEntity entity = null;
    private volatile String changedPropertyName = null;
    private volatile String originalState = null;
    private volatile String targetState = null;

    public record MergeData(MyEntity entity, String changedPropertyName, String originalState, String targetState) {
    }

    @Override
    public void postMerge(Object source, Object target, Object id, Object[] targetState, Object[] originalState,
            String[] propertyNames, Type[] propertyTypes) {
        this.targetState = (String) targetState[0];
        this.originalState = (String) originalState[0];
        Interceptor.super.postMerge(source, target, id, targetState, originalState, propertyNames, propertyTypes);
    }

    @Override
    public void preMerge(Object entity, Object[] state, String[] propertyNames, Type[] propertyTypes) {
        this.entity = (MyEntity) entity;
        this.changedPropertyName = propertyNames[0];
        Interceptor.super.preMerge(entity, state, propertyNames, propertyTypes);
    }

    public MergeData getMergeData() {
        return new MergeData(entity, changedPropertyName, originalState, targetState);
    }
}
