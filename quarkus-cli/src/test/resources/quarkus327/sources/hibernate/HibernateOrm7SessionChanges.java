package org.acme;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.annotations.CascadeType;

public class HibernateOrm7SessionChanges {

    private final Session session;

    public HibernateOrm7SessionChanges(Session session) {
        this.session = session;
    }

    private void rewriteMethodLoadToGetReference() {
        session.load(MyEntity.class, 1L);
        session.load("MyEntity", 2L);
    }

    private void rewriteMethodLoadToGet() {
        session.load(MyEntity.class, 3L, LockMode.OPTIMISTIC);
        session.load("MyEntity", 4L, LockMode.OPTIMISTIC);
        session.load(MyEntity.class, 5L, LockOptions.READ);
        session.load("MyEntity", 6L, LockOptions.READ);
    }

    private void rewriteMethodGetToFind() {
        session.get(MyEntity.class, 7L);
        session.get("MyEntity", 8L);
    }

    private void rewriteMethodDeleteToRemove() {
        session.delete(9L);
    }

    private void rewriteMethodSaveToPersist() {
        session.save(new MyEntity(10L));
        session.save("MyEntity", new MyEntity(11L));
    }

    private void rewriteMethodUpdateToMerge() {
        session.update(new MyEntity(12L));
    }

    private void rewriteCascadeConstantDeleteToRemove() {
        var ignored = CascadeType.DELETE;
    }

    @Entity
    public static class MyEntity {

        public MyEntity() {
        }

        public MyEntity(Long id) {
            this.id = id;
        }

        @Id
        Long id;
    }
}
