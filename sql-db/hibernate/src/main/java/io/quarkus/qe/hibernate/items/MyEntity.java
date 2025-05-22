package io.quarkus.qe.hibernate.items;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "my_entity")
public class MyEntity {

    // The name of this property must be (alphabetically) before the name of "data" to trigger the bug.
    @EmbeddedId
    private MyEntityId anId;

    private String data;

    public MyEntityId getAnId() {
        return anId;
    }

    public void setAnId(MyEntityId anId) {
        this.anId = anId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "anId=" + anId.getVal() + ", data=" + data;
    }
}
