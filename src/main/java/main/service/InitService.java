package main.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javassist.NotFoundException;
import javax.imageio.ImageIO;
import main.dto.response.ResponseApiInit;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InitService {

  public ResponseApiInit getInit() {
    ResponseApiInit responseApiInit = new ResponseApiInit();
    responseApiInit.setTitle("DevPub");
    responseApiInit.setSubtitle("Рассказы разработчиков");
    responseApiInit.setPhone("+7 984 888 10 10");
    responseApiInit.setEmail("meshkovo1977@mail.ru");
    responseApiInit.setCopyright("Блинов Филипп");
    responseApiInit.setCopyrightFrom("2020");
    return responseApiInit;
  }

  public String uploadImage(MultipartFile image) throws IOException {
    byte[] byteArr = image.getBytes();
    InputStream inputStream = new ByteArrayInputStream(byteArr);
    BufferedImage bufferedImage = ImageIO.read(inputStream);
    String listDirs = randomLetter() + "-" + randomLetter() + "-" + randomLetter();
    File dir = new File("upload");
    if (!dir.exists()) {
      dir.mkdir();
    }
    String pathToDir = dir + "/" + listDirs;
    File theDir = new File(pathToDir);
    if (!theDir.exists()) {
      theDir.mkdir();
    }
    dir = theDir;
    Random random = new Random();
    String filename = random.nextInt(100000) + ".jpg";
    File filePath = new File(dir + "/" + filename);
    ImageIO.write(bufferedImage, "jpg", filePath);
    return "/" + pathToDir + "/" + filename;
  }

  private String randomLetter() {
    Random random = new Random();
    char c = (char) (random.nextInt(26) + 'a');
    return String.valueOf(c);
  }

  public byte[] getImage(String link) throws Exception {
    List<String> files = getAllFiles("src/main/resources/static/avatars/");
    File newFile = null;
    for (String file : files) {
      String[] partsFile = file.split("[/\\\\]");
      if (partsFile[partsFile.length - 1].equals(link)) {
        newFile = new File(file);
      }
    }
    if (newFile != null) {
      return FileUtils.readFileToByteArray(newFile);
    } else {
      throw new NotFoundException("no such photo");
    }
  }

  private List<String> getAllFiles(String link) throws IOException {
    List<String> files = Files.walk(Paths.get(link))
        .filter(Files::isRegularFile)
        .map(Path::toString)
        .collect(Collectors.toList());
    return files;
  }
}
