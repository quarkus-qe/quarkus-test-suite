package io.quarkus.ts.http.advanced.reactive;

import java.util.Date;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class FootballTeam {

    private String name;
    private String colorTShirt;
    private int euroCups;
    private Date foundationDate;
    private String stadium;
    private List<String> keyPlayers;

    public FootballTeam() {
    }

    public FootballTeam(String name, String colorTShirt, int euroCups, Date foundationDate, String stadium,
            List<String> keyPlayers) {
        this.name = name;
        this.colorTShirt = colorTShirt;
        this.euroCups = euroCups;
        this.foundationDate = foundationDate;
        this.stadium = stadium;
        this.keyPlayers = keyPlayers;
    }

    public Date getFoundationDate() {
        return foundationDate;
    }

    public void setFoundationDate(Date foundationDate) {
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
