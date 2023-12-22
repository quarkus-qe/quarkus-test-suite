package io.quarkus.ts.javaee.gettingstarted.dto;

public class FarewellPartOneDTO {
    private String data;

    public FarewellPartOneDTO(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
