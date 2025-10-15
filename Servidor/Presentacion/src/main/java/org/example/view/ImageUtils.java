package org.example.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageUtils {
    public static ImageIcon createCircularImageIcon(String path, int size) {
        try {
            BufferedImage original = ImageIO.read(new File(path));
            return createCircularIconFromBuffered(original, size);
        } catch (Exception e) {
            e.printStackTrace();
            return createPlaceholderIcon(size);
        }
    }

    public static ImageIcon createPlaceholderIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillOval(0, 0, size, size);
        g2.setColor(Color.DARK_GRAY);
        g2.drawOval(0, 0, size-1, size-1);
        g2.dispose();
        return new ImageIcon(img);
    }

    private static ImageIcon createCircularIconFromBuffered(BufferedImage original, int size) {
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, size, size, null);

        BufferedImage mask = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gMask = mask.createGraphics();
        gMask.fill(new Ellipse2D.Double(0,0,size,size));
        gMask.dispose();

        BufferedImage circleBuffer = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gCircle = circleBuffer.createGraphics();
        gCircle.setClip(new Ellipse2D.Float(0,0,size,size));
        gCircle.drawImage(scaled, 0, 0, null);
        gCircle.dispose();

        return new ImageIcon(circleBuffer);
    }
}
