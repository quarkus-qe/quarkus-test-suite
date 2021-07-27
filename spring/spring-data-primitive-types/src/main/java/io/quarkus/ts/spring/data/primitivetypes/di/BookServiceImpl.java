package io.quarkus.ts.spring.data.primitivetypes.di;

import org.springframework.beans.factory.annotation.Autowired;

public class BookServiceImpl implements BookService {

    @Autowired(required = false)
    private AuthorService authorService;

}
