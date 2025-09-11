package io.quarkus.qe.hibernate.items;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "json_entity")
public class JsonEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String jsonData;

    public JsonEntity() {
    }

    public JsonEntity(String name, String jsonData) {
        this.name = name;
        this.jsonData = jsonData;
    }

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

    public String getJsonData() {
        return jsonData;
    }
}
