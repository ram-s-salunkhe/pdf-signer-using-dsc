package com.example.config;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

public class SignatureImageGenerator {
    public static ImageData createSignatureImage(float width, float height) throws Exception {
        // Create a blank BufferedImage
        BufferedImage bufferedImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();

        // Set background color (white)
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, (int) width, (int) height);

        // Draw a border (optional)
        graphics.setColor(Color.BLACK);
        graphics.drawRect(0, 0, (int) width - 1, (int) height - 1);

        // Custom text (optional)
        graphics.setFont(new Font("Arial", Font.BOLD, 12));
        graphics.setColor(Color.BLUE);
        graphics.drawString("Signed", 10, (int) (height / 2));

        graphics.dispose();

        // Convert BufferedImage to ImageData
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return ImageDataFactory.create(baos.toByteArray());
    }
}
