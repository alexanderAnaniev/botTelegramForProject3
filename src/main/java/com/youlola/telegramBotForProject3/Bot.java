package com.youlola.telegramBotForProject3;


import com.youlola.telegramBotForProject3.DTO.MeasurementsDTO;
import com.youlola.telegramBotForProject3.DTO.MeasurementsResponse;
import com.youlola.telegramBotForProject3.DTO.SensorDTO;
import com.youlola.telegramBotForProject3.DTO.SensorsResponse;

import com.youlola.telegramBotForProject3.config.BotConfig;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.stream.Collectors;

import static com.youlola.telegramBotForProject3.BotState.*;


@Component
public class Bot extends TelegramLongPollingBot {
    final BotConfig config;
    static final String HELP_TEXT = "Этот бот создан для удобного взаимодействия с 3 проектом.\n\n" +
            "Нажми /start чтобы увидеть главную страницу";

    private ReplyKeyboardMarkup replyKeyboardMarkup;

      private final Map<Long,BotState> State = new HashMap<>();


    public Bot(BotConfig config) {
        this.config = config;
        initKeyboard();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }


    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        if (update.hasMessage() && update.getMessage().hasText()) {
            switch(getBotState(chatId)) {
                case WAIT_MESSAGE -> handleCommandsForWaitingBot(update);
                case REGISTER_SENSOR -> handleCommandsForRegisterSensor(update);
                case REGISTER_MEASUREMENT -> handleCommandsForRegisterMeasurement(update);
                default -> sendMessage(chatId,"error");
            }
            }
        }
        private BotState getBotState(Long chatId) {
        if (State.containsKey(chatId)) {
            return State.get(chatId);
        } else {
            State.put(chatId, WAIT_MESSAGE);
            return WAIT_MESSAGE;
        }
        }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет, " + name + ", для того чтобы воспользоваться ботом,нажми на интересующую тебя кнопку в меню";
        sendMessage(chatId, answer);
    }

    private void handleCommandsForWaitingBot(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            switch (messageText) {
                case ("/start"):
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case ("/help"):
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case ("Список сенсоров"):
                    sendMessage(chatId, getSensorsFromServer());
                    break;
                case("Зарегистрировать сенсор"):
                    sendMessage(chatId,"Введите имя сенсора");
                    State.put(chatId, REGISTER_SENSOR);
                    break;
                case("Список измерений"):
                    sendMessage(chatId, String.valueOf(getTemperaturesFromServer()));
                break;
                    case ("Отправить измерение"):
                    sendMessage(chatId,"Введите имя сенсора которому принадлежит измерение");
                        State.put(chatId,REGISTER_MEASUREMENT);
                    break;
                default:
                    sendMessage(chatId, "Не знаю такой команды");
            }
        }
    }
   private void handleCommandsForRegisterSensor(Update update) {
       if (update.hasMessage() && update.getMessage().hasText()) {
           long chatId = update.getMessage().getChatId();
           String messageText = update.getMessage().getText();
                   registerSensor(messageText);
                   sendMessage(chatId,"Сенсор зарегистрирован");
           State.put(chatId,WAIT_MESSAGE);
           }
       }

       private void handleCommandsForRegisterMeasurement(Update update) {
           if (update.hasMessage() && update.getMessage().hasText()) {
               long chatId = update.getMessage().getChatId();
               String messageText = update.getMessage().getText();
               sendMeasurements(27,true,messageText);
               sendMessage(chatId,"Измерение отправлено");
               State.put(chatId,WAIT_MESSAGE);
           }
       }


    public static String registerSensor(String sensorName) {
        final String url = "http://localhost:8080/sensors/registration";
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("name", sensorName);

        makePostRequestWithJSONData(url, jsonData);
        return "Сенсор зарегистрирован";
    }
    private String sendMeasurements (double value, boolean raining, String sensorName){
        final String url = "http://localhost:8080/measurements/add";
        Map<String,Object> jsonData = new HashMap<>();
        jsonData.put("value", value);
        jsonData.put("raining", raining);
        jsonData.put("sensor",Map.of("name",sensorName));

        makePostRequestWithJSONData(url,jsonData);
        return "Измерение отправлено";
    }

    public String getSensorsFromServer() {
        final RestTemplate restTemplate = new RestTemplate();
        final String url = "http://localhost:8080/sensors";

        SensorsResponse jsonResponse = restTemplate.getForObject(url, SensorsResponse.class);

        if (jsonResponse == null || jsonResponse.getSensors() == null)
            return Collections.emptyList().toString();

        return "Сенсоры :" + jsonResponse.getSensors().stream().map(SensorDTO::getName).collect(Collectors.toList());
    }

    public String getTemperaturesFromServer(){
        final RestTemplate restTemplate = new RestTemplate();
        final String url = "http://localhost:8080/measurements";

        MeasurementsResponse jsonResponse = restTemplate.getForObject(url,MeasurementsResponse.class);

        if (jsonResponse == null || jsonResponse.getMeasurements() == null)
            return Collections.emptyList().toString();

        return jsonResponse.getMeasurements().stream().map(MeasurementsDTO::getValue)
                .collect(Collectors.toList()).toString();
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setReplyMarkup(replyKeyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
        }
    }
    private static void makePostRequestWithJSONData(String url, Map<String, Object> jsonData) {
        final RestTemplate restTemplate = new RestTemplate();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> request = new HttpEntity<>(jsonData, headers);
        try {
            restTemplate.postForObject(url, request, String.class);

        } catch (HttpClientErrorException e) {
            System.out.println("ERROR!");
            System.out.println(e.getMessage());
        }
    }
    void initKeyboard() {
        replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRows.add(keyboardRow);
        keyboardRow.add(new KeyboardButton("Список измерений"));
        keyboardRow.add(new KeyboardButton("Список сенсоров"));
        keyboardRow.add(new KeyboardButton("Зарегистрировать сенсор"));
        keyboardRow.add(new KeyboardButton("Отправить измерение"));
        replyKeyboardMarkup.setKeyboard(keyboardRows);

    }
}

