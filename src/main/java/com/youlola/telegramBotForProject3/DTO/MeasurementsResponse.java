package com.youlola.telegramBotForProject3.DTO;

import java.util.List;

public class MeasurementsResponse {
    private List<MeasurementsDTO> measurements;

    public List<MeasurementsDTO> getMeasurements(){
        return measurements;
    }
    public void setMeasurements(List<MeasurementsDTO> measurements){
        this.measurements=measurements;
    }
}
