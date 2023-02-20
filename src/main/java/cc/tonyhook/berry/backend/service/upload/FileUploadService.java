package cc.tonyhook.berry.backend.service.upload;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Set;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.node.ObjectNode;

import cc.tonyhook.berry.backend.service.shared.ImageHelperService;

public interface FileUploadService {

    default String getProperExtension(MultipartFile upload) {
        String ext = "";
        String type = upload.getContentType();

        if (type == null) {
            return ext;
        }

        if (type.equals("video/quicktime")) {
            ext = ".mov";
        }
        if (type.equals("video/mp4")) {
            ext = ".mp4";
        }
        if (type.equals("video/x-m4v")) {
            ext = ".m4v";
        }
        if (type.equals("video/3gpp")) {
            ext = ".3gp";
        }
        if (type.equals("application/pdf")) {
            ext = ".pdf";
        }

        return ext;
    }

    default byte[] getThumbnail(byte[] media) {
        try {
            InputStream in = new ByteArrayInputStream(media);
            BufferedImage image = ImageIO.read(in);

            int width = image.getWidth();
            int height = image.getHeight();
            int newWidth = image.getWidth();
            int newHeight = image.getHeight();

            if (width > 240 || height > 320) {
                float ratio = Math.max(1.0f * width / 240, 1.0f * height / 320);
                newWidth = Math.round(1.0f * width / ratio);
                newHeight = Math.round(1.0f * height / ratio);
            }

            BufferedImage newImage = ImageHelperService.resize(image, newWidth, newHeight);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(newImage, "jpg", out);

            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    abstract ObjectNode upload(String type, String id, MultipartFile upload);

    abstract ObjectNode uploadEx(String type, String id, String group, Integer total, Integer page, MultipartFile upload);

    abstract Set<String> listId(String type);

    abstract Set<String> listFile(String type, String id);

    abstract void upload(String type, String id, String name, byte[] blob);

    abstract byte[] download(String type, String id, String name);

    abstract void delete(String type, String id);

    abstract void delete(String type, String id, String name);

}
