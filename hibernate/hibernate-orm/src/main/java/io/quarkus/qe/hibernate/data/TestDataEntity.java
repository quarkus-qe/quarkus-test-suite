package io.quarkus.qe.hibernate.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@NamedQuery(name = "TestDataEntity.FindByCharacter", query = "from TestDataEntity where character = :character order by id")
@Table(name = "test_data")
@Entity
public class TestDataEntity {

    @Id
    Long id;

    String content;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    String json;

    Character character;

    @Version
    int version;

    public TestDataEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public enum Character {
        NEW,
        OLD,
        UPDATED
    }
}
