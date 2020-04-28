package main.service;

import main.api.response.ResponseApiInit;
import org.springframework.stereotype.Service;

@Service
public class InitService {

  public ResponseApiInit getInit() {
    ResponseApiInit responseApiInit = new ResponseApiInit();
    responseApiInit.setTitle("DevPub");
    responseApiInit.setSubtitle("Рассказы разработчиков");
    responseApiInit.setPhone("+7 903 444 55 55");
    responseApiInit.setEmail("mail@mail.ru");
    responseApiInit.setCopyright("Дмитрий Сергеев");
    responseApiInit.setCopyrightFrom("2020");
    return responseApiInit;
  }
}
