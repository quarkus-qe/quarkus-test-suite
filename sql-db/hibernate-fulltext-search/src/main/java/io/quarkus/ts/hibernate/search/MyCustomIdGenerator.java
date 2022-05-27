package io.quarkus.ts.hibernate.search;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * Don't use this ID generator in prod. Was developed just for testing proposes
 */
public class MyCustomIdGenerator implements IdentifierGenerator {

    private Random rand;
    private Set<Integer> ids = new HashSet<>();

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object obj) throws HibernateException {
        return unique(getRand().nextInt(Integer.MAX_VALUE));
    }

    private int unique(int id) {
        while (ids.contains(id)) {
            id = getRand().nextInt(Integer.MAX_VALUE);
        }
        ids.add(id);
        return id;
    }

    // java.util.Random lazy initialization in order to avoid a native error
    // "Error: Detected an instance of Random/SplittableRandom class in the image heap"
    private Random getRand() {
        if (this.rand == null) {
            synchronized (this) {
                if (this.rand == null) {
                    this.rand = new Random();
                }
            }
        }
        return this.rand;
    }
}
