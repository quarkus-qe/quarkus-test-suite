package io.quarkus.ts.hibernate.search;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;

import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;

@Dependent
@Named("myAnalysisConfigurer")
public class AnalysisConfigurer implements ElasticsearchAnalysisConfigurer {

    @Override
    public void configure(ElasticsearchAnalysisConfigurationContext context) {
        context.analyzer("name").custom()
                .tokenizer("standard")
                .tokenFilters("asciifolding", "lowercase");

        context.normalizer("sort").custom()
                .tokenFilters("asciifolding", "lowercase");
    }
}
