package com.youlola.telegramBotForProject3.DTO;

import java.util.List;

public class SensorsResponse {
    List<SensorDTO> sensors;

    public List<SensorDTO> getSensors(){
        return sensors;
    }
    public void setSensors(List<SensorDTO> sensors){
        this.sensors=sensors;
    }

}


