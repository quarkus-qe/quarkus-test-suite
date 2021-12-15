package io.quarkus.ts.reactive.database;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ISBNConverter implements AttributeConverter<Long, String> {

    private static final int ISBN_MAX_LENGTH = 13;

    @Override
    /**
     * Canonical ISBN format: 978-3-16-148410-0, can be prefixed with zeroes if there is less than 13 digits
     */
    public String convertToDatabaseColumn(Long number) {
        String s = number.toString();
        StringBuilder result = new StringBuilder(s);
        if (s.length() > ISBN_MAX_LENGTH) {
            throw new IllegalStateException("ISBN " + s + " has a wrong length: " + number);
        }
        int paddingLength = ISBN_MAX_LENGTH - s.length();
        result.insert(0, "0".repeat(paddingLength));
        result.insert(3, '-');
        result.insert(5, '-');
        result.insert(8, '-');
        result.insert(15, '-');
        return result.toString();
    }

    @Override
    public Long convertToEntityAttribute(String s) {
        if (s == null) {
            return 0L;
        } else {
            return Long.parseLong(s.replaceAll("-", ""));
        }
    }
}
