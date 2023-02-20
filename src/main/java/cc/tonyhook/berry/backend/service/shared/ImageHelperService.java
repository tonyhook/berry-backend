package cc.tonyhook.berry.backend.service.shared;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ImageHelperService {

    public static BufferedImage resize(BufferedImage image, Integer width, Integer height) {
        if ((width <= 0) && (height <= 0)) {
            width = image.getWidth();
            height = image.getHeight();
        }

        try {
            int owidth = image.getWidth();
            int oheight = image.getHeight();
            float rwidth = (float) (width * 1.0) / owidth;
            float rheight = (float) (height * 1.0) / oheight;
            float r = (float) 1.0;
            if ((height != 0) && (width != 0)) {
                r = rheight < rwidth ? rheight : rwidth;
            }
            if ((height == 0) && (width != 0)) {
                r = rwidth;
            }
            if ((height != 0) && (width == 0)) {
                r = rheight;
            }
            int twidth = Math.round(owidth * r);
            int theight = Math.round(oheight * r);

            BufferedImage bi = new BufferedImage(twidth, theight, BufferedImage.TYPE_INT_RGB);
            Graphics g = bi.getGraphics();
            g.drawImage(image, 0, 0, twidth, theight, Color.WHITE, null);
            g.dispose();

            return bi;
        } catch (Exception e) {
            return null;
        }
    }

}
