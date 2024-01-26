package io.quarkus.ts.http.advanced.reactive;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CityListWrapper {

    private List<City> cities;

    @XmlElement(name = "cities")
    private List<City> citiesList;

    public CityListWrapper() {
    }

    public CityListWrapper(List<City> cities) {
        this.cities = cities;
    }

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.citiesList = cities;
    }
}
