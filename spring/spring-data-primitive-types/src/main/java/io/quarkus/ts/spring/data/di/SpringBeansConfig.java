package io.quarkus.ts.spring.data.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringBeansConfig {

    @Bean
    public AudioBookService audioBookServiceGenerator() {
        return new AudioBookServiceImpl();
    }

}
