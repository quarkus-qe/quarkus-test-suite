package io.quarkus.ts.spring.data.nameddatasource;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

@Entity
public class AlternativeArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Length(min = 2, max = 50, message = "length must be between {min} and {max}")
    @NotBlank(message = "Name may not be blank")
    private String name;

    @Length(min = 2, max = 50, message = "length must be between {min} and {max}")
    @NotBlank(message = "Author may not be blank")
    private String author;

    public AlternativeArticle() {
    }

    public AlternativeArticle(String name, String author) {
        this.name = name;
        this.author = author;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

}
