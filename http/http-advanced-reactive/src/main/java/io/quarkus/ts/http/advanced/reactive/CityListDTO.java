package io.quarkus.ts.http.advanced.reactive;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CityListDTO {

    @XmlElement(name = "cityList")
    private List<City> cities = new ArrayList<>();

    public CityListDTO() {
    }

    public CityListDTO(List<City> cities) {
        this.cities = cities;
    }

    public List<City> getCityList() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }

    @Override
    public String toString() {
        return cities.toString();
    }
}
