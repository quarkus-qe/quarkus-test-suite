package io.quarkus.ts.http.advanced.reactive;

import java.time.LocalDate;
import java.util.List;

import jakarta.json.bind.annotation.JsonbDateFormat;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class FootballTeam {

    private String name;
    private String colorTShirt;
    private int euroCups;
    @JsonbDateFormat(value = "yyyy-MM-dd")
    private LocalDate foundationDate;
    private String stadium;
    private List<String> keyPlayers;

    public FootballTeam() {
    }

    public FootballTeam(String name, String colorTShirt, int euroCups, LocalDate foundationDate, String stadium,
            List<String> keyPlayers) {
        this.name = name;
        this.colorTShirt = colorTShirt;
        this.euroCups = euroCups;
        this.foundationDate = foundationDate;
        this.stadium = stadium;
        this.keyPlayers = keyPlayers;
    }

    public LocalDate getFoundationDate() {
        return foundationDate;
    }

    public void setFoundationDate(LocalDate foundationDate) {
        this.foundationDate = foundationDate;
    }

    public String getStadium() {
        return stadium;
    }

    public void setStadium(String stadium) {
        this.stadium = stadium;
    }

    public List<String> getKeyPlayers() {
        return keyPlayers;
    }

    public void setKeyPlayers(List<String> keyPlayers) {
        this.keyPlayers = keyPlayers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColorTShirt() {
        return colorTShirt;
    }

    public void setColorTShirt(String colorTShirt) {
        this.colorTShirt = colorTShirt;
    }

    public int getEuroCups() {
        return euroCups;
    }

    public void setEuroCups(int euroCups) {
        this.euroCups = euroCups;
    }

}
