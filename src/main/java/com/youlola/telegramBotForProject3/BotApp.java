package com.youlola.telegramBotForProject3;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BotApp {

    public static void main(String[] args) {
        SpringApplication.run(BotApp.class,args);
    }
    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

}


