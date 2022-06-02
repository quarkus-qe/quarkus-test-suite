package io.quarkus.ts.spring.data.primitivetypes;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.quarkus.ts.spring.data.primitivetypes.model.Book;
import io.quarkus.ts.spring.data.primitivetypes.model.BookProjection;

@RepositoryRestResource(exported = false)
public interface BookRepository extends CrudRepository<Book, Integer> {

    //This is for regression test for https://github.com/quarkusio/quarkus/pull/9192
    @Query(value = "SELECT b.publicationYear FROM Book b where b.bid = :bid")
    int customFindPublicationYearPrimitive(@Param("bid") Integer bid);

    //This is for regression test for https://github.com/quarkusio/quarkus/pull/9192
    @Query(value = "SELECT b.publicationYear FROM Book b where b.bid = :bid")
    Integer customFindPublicationYearObject(@Param("bid") Integer bid);

    //This is for regression test for https://github.com/quarkusio/quarkus/pull/9192
    @Query(value = "SELECT b.isbn FROM Book b where b.bid = :bid")
    long customFindPublicationIsbnPrimitive(@Param("bid") Integer bid);

    //This is for regression test for https://github.com/quarkusio/quarkus/pull/9192
    @Query(value = "SELECT b.isbn FROM Book b where b.bid = :bid")
    Long customFindPublicationIsbnObject(@Param("bid") Integer bid);

    //This is for regression test for https://github.com/quarkusio/quarkus/pull/13015
    List<Book> findByPublisherAddressZipCode(@Param("zipCode") String zipCode);

    @Query("SELECT bid, publicationYear, isbn, name, publisherAddress FROM Book WHERE bid = :bid")
    BookProjection getByBid(Integer bid);
}
