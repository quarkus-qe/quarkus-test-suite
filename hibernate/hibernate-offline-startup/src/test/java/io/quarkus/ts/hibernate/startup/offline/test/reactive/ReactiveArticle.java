package io.quarkus.ts.hibernate.startup.offline.test.reactive;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "article")
@Entity
public class ReactiveArticle {

    private Long id;

    private String name;

    public ReactiveArticle() {
    }

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
