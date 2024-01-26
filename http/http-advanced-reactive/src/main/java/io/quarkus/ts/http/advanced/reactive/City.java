package io.quarkus.ts.http.advanced.reactive;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class City {
    @XmlElement
    private String name;

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    @XmlElement
    private String country;

    public City(String name, String country) {
        this.name = name;
        this.country = country;
    }

    public City() {
    }

}
