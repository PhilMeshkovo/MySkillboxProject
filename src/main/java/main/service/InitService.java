package main.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import main.api.response.ResponseApiInit;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InitService {

  public ResponseApiInit getInit() {
    ResponseApiInit responseApiInit = new ResponseApiInit();
    responseApiInit.setTitle("DevPub");
    responseApiInit.setSubtitle("Рассказы разработчиков");
    responseApiInit.setPhone("+7 903 444 55 55");
    responseApiInit.setEmail("mail@mail.ru");
    responseApiInit.setCopyright("Блинов Филипп");
    responseApiInit.setCopyrightFrom("2020");
    return responseApiInit;
  }

  public String uploadImage(MultipartFile image) throws IOException {
    byte[] byteArr = image.getBytes();
    InputStream inputStream = new ByteArrayInputStream(byteArr);
    BufferedImage bufferedImage = ImageIO.read(inputStream);
    List<String> listDirs = List.of(randomLetter(), randomLetter(), randomLetter());
    File dir = new File("src/main/resources/static/upload");
    if (!dir.exists()) {
      dir.mkdir();
    }
    for (String listDir : listDirs) {
      File theDir = new File(dir + "/" + listDir);
      if (!theDir.exists()) {
        theDir.mkdir();
      }
      dir = theDir;
    }
    Random random = new Random();
    File filePath = new File(dir + "/" + random.nextInt(100000) + ".jpg");
    ImageIO.write(bufferedImage, "jpg", filePath);
    return filePath.toString();
  }

  private String randomLetter() {
    Random random = new Random();
    char c = (char) (random.nextInt(26) + 'a');
    return String.valueOf(c);
  }
}
