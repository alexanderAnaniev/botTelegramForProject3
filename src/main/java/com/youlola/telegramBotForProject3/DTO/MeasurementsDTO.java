package com.youlola.telegramBotForProject3.DTO;

public class MeasurementsDTO {
    private Double value;
    private Boolean raining;
    private SensorDTO sensor;
    public Double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Boolean getRaining() {
        return raining;
    }

    public void setRaining(boolean raining) {
        this.raining = raining;
    }

    public SensorDTO getSensor() {
        return sensor;
    }

    public void setSensor(SensorDTO sensor) {
        this.sensor = sensor;
    }

}
