package main.service;

import main.dto.response.ResponseApiInit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class InitServiceTest {

  @Autowired
  InitService initService = new InitService();

  @Test
  public void getInitTest(){
    ResponseApiInit result = initService.getInit();
    ResponseApiInit responseApiInit = new ResponseApiInit();
    responseApiInit.setTitle("DevPub");
    responseApiInit.setSubtitle("Рассказы разработчиков");
    responseApiInit.setPhone("+9 999 999 99 99");
    responseApiInit.setEmail("meshkovo1977@mail.ru");
    responseApiInit.setCopyright("Блинов Филипп");
    responseApiInit.setCopyrightFrom("2020");
    Assertions.assertEquals(responseApiInit, result);
  }
}
