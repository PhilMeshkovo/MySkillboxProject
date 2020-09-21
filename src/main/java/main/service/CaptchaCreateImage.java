package main.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public class CaptchaCreateImage {

  public static byte[] getCaptcha(String random_text, String image_extension) throws IOException {
    BufferedImage bufferedImage;
    int width = 100;
    int height = 35;
    bufferedImage = new BufferedImage(width, height,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = bufferedImage.createGraphics();
    Font font = new Font("Arial", Font.BOLD, 18);
    g2d.setFont(font);
    RenderingHints rh = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    rh.put(RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY);
    g2d.setRenderingHints(rh);
    GradientPaint gp = new GradientPaint(0, height / 2,
        new Color(218, 232, 215), 0, height, new Color(218, 232, 215), false);
    g2d.setPaint(new GradientPaint(0, height / 2, new Color(218, 232, 215), 0, height,
        new Color(254, 254, 254), true));
    g2d.fillRect(0, 0, width, height);
    g2d.setPaint(
        new GradientPaint(0, 0, new Color(180, 180, 180), 0, height / 2, new Color(222, 222, 222),
            true));
    g2d.fillRect(1, 1, 5, height - 2);
    g2d.setColor(new Color(119, 144, 131));
    g2d.drawLine(1, 0, 5, 5);
    g2d.drawLine(1, 5, 5, 0);
    g2d.drawLine(1, 5, 5, 10);
    g2d.drawLine(1, 10, 5, 5);
    g2d.drawLine(1, 10, 5, 15);
    g2d.drawLine(1, 15, 5, 10);
    g2d.drawLine(1, 15, 5, 20);
    g2d.drawLine(1, 20, 5, 15);
    g2d.drawLine(1, 20, 5, 25);
    g2d.drawLine(1, 25, 5, 20);
    g2d.drawLine(1, 25, 5, 30);
    g2d.drawLine(1, 30, 5, 25);
    g2d.drawLine(1, 30, 5, 35);
    g2d.drawLine(1, 35, 5, 30);
    g2d.drawLine(1, 35, 5, 40);
    g2d.drawLine(1, 40, 5, 35);
    g2d.drawLine(1, 40, 5, 45);
    g2d.drawLine(1, 45, 5, 40);
    g2d.drawLine(1, 45, 5, 50);
    g2d.drawLine(1, 50, 5, 45);
    g2d.drawLine(6, 0, 6, height);
    g2d.setColor(new Color(151, 100, 60));
    g2d.drawLine(8, 0, 8, height);
    // LOGO
    g2d.setPaint(
        new GradientPaint(0, 0, new Color(133, 112, 164), 0, height / 2, new Color(211, 198, 112),
            true));
    int xp[] = {14 + 10, 28 + 10, 14 + 10, 0 + 10};
    int yp[] = {0 + 11, 14 + 11, 28 + 11, 14 + 11};
    g2d.drawPolygon(xp, yp, 4);
    xp = new int[]{14 + 10, 28 + 9, 14 + 10, 0 + 11};
    yp = new int[]{0 + 10, 14 + 11, 28 + 10, 14 + 11};
    g2d.drawPolygon(xp, yp, 4);
    g2d.drawRect(14, 14, 20, 21);
    g2d.drawRect(15, 15, 18, 19);
    g2d.drawRect(0, 0, width - 1, height - 1);
    g2d.drawLine(18, 29, 30, 29);
    g2d.drawLine(18, 28, 30, 28);
    g2d.drawLine(18, 28, 24, 18);
    g2d.drawLine(19, 28, 24, 19);
    g2d.drawLine(24, 18, 30, 28);
    // -------------------------
    g2d.drawString(random_text, 40, 20);
    for (int i = 0; i < 100; i += 5) {
      g2d.drawLine(40 + i, 10, 50 + i, 38);
    }
    g2d.dispose();
    byte captchaByteStream[];
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ImageOutputStream imout = ImageIO.createImageOutputStream(bout);
    if (ImageIO.write(bufferedImage, image_extension, imout)) {
      captchaByteStream = bout.toByteArray();

      return captchaByteStream;

    } else {
      return null;
    }
  }
}
