package io.quarkus.qe.hibernate.analyze;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "analyze")
public class Analyze {

    @Id
    public Long id;

    public String author;

    public Analyze(Long id, String author) {
        this.id = id;
        this.author = author;
    }

    public Analyze() {
        // default contractor
    }
}
