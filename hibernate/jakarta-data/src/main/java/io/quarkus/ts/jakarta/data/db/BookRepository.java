package io.quarkus.ts.jakarta.data.db;

import java.util.List;

import jakarta.data.Limit;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Repository;

import org.hibernate.StatelessSession;
import org.hibernate.query.range.Range;

@Repository
public interface BookRepository extends BasicRepository<Book, Long> {

    @OrderBy(io.quarkus.ts.jakarta.data.db._Book.TITLE)
    @Find
    List<Book> findByTitle(Range<String> title, Limit limit);

    StatelessSession session();
}
