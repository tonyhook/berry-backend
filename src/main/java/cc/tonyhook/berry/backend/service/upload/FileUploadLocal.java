package cc.tonyhook.berry.backend.service.upload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cc.tonyhook.berry.backend.service.shared.HashHelperService;
import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(prefix = "app.file", name = "provider", havingValue = "local")
public class FileUploadLocal implements FileUploadService {

    @Value("${app.file.server-path}")
    private String serverPath;
    @Value("${app.file.local.save-path}")
    private String savePath;

    private Map<String, Long> groupUpdateTimestamp;

    private Map<String, MultipartFile> fileMap;

    private Map<String, byte[]> blobMap;

    @PostConstruct
    private void initFileFragment() {
        groupUpdateTimestamp = new HashMap<String, Long>();
        fileMap = new HashMap<String, MultipartFile>();
        blobMap = new HashMap<String, byte[]>();
    }

    @Scheduled(cron = "0 * * * * ?")
    private void removeOutdatedFragments() {
        Long now = System.currentTimeMillis();

        synchronized (groupUpdateTimestamp) {
            for (Iterator<Entry<String, Long>> iterator = groupUpdateTimestamp.entrySet().iterator(); iterator.hasNext(); ){
                Entry<String, Long> item = iterator.next();
                String group = item.getKey();
                Long timestamp = item.getValue();

                if (now - timestamp > 3600000) {
                    iterator.remove();

                    for (Iterator<Entry<String, MultipartFile>> itf = fileMap.entrySet().iterator(); itf.hasNext();) {
                        Entry<String, MultipartFile> itemf = itf.next();
                        if (itemf.getKey().split("#")[0].equals(group)) {
                            itf.remove();
                        }
                    }
                    for (Iterator<Entry<String, byte[]>> itb = blobMap.entrySet().iterator(); itb.hasNext();) {
                        Entry<String, byte[]> itemb = itb.next();
                        if (itemb.getKey().split("#")[0].equals(group)) {
                            itb.remove();
                        }
                    }
                }
            }
        }
    }

    @Override
    public ObjectNode upload(String type, String id, MultipartFile upload) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        ObjectNode error = JsonNodeFactory.instance.objectNode();

        FileOutputStream fos = null;
        FileOutputStream fosThumbnail = null;

        try {
            String hashtext = HashHelperService.hash(upload.getBytes(), "MD5");
            String ext = getProperExtension(upload);

            File d = new File(savePath);
            if (!d.exists()) {
                d.mkdir();
            }
            d = new File(savePath + "/" + type);
            if (!d.exists()) {
                d.mkdir();
            }
            d = new File(savePath + "/" + type + "/" + id);
            if (!d.exists()) {
                d.mkdir();
            }

            File f = new File(savePath + "/" + type + "/" + id + "/" + hashtext + ext);
            f.createNewFile();
            fos = new FileOutputStream(f);
            fos.write(upload.getBytes());

            byte[] thumbnail = getThumbnail(upload.getBytes());
            if (thumbnail != null) {
                f = new File(savePath + "/" + type + "/" + id + "/" + hashtext + ext + ".thumbnail");
                f.createNewFile();
                fosThumbnail = new FileOutputStream(f);
                fosThumbnail.write(thumbnail);

                node.put("uploaded", 1);
                node.put("fileName", hashtext + ext);
                node.put("url", serverPath + type + "/" + id + "/" + hashtext);
            } else {
                node.put("uploaded", 1);
                node.put("fileName", hashtext + ext);
                node.put("url", serverPath + type + "/" + id + "/" + hashtext);
            }
        } catch (Exception e) {
            e.printStackTrace();
            node.put("uploaded", 0);
            error.put("message", e.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                node.put("uploaded", 0);
                error.put("message", e.getMessage());
            } finally {
                node.set("error", error);
            }
            try {
                if (fosThumbnail != null) {
                    fosThumbnail.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                node.put("uploaded", 0);
                error.put("message", e.getMessage());
            } finally {
                node.set("error", error);
            }
        }

        return node;
    }

    @Override
    public ObjectNode uploadEx(String type, String id, String group, Integer total, Integer page, MultipartFile upload) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        ObjectNode error = JsonNodeFactory.instance.objectNode();

        Long now = System.currentTimeMillis();

        if (page < 0 || page >= total) {
            node.put("uploaded", 0);
            error.put("message", "index out of range");
            node.set("error", error);
            return node;
        }

        synchronized (groupUpdateTimestamp) {
            try {
                groupUpdateTimestamp.put(group, now);
                fileMap.put(group + "#" + page, upload);
                blobMap.put(group + "#" + page, upload.getBytes());

                int count = 0;
                for (String key : fileMap.keySet()) {
                    if (key.split("#")[0].equals(group)) {
                        count++;
                    }
                }

                if (count < total) {
                    node.put("uploaded", 2);
                    node.put("page", page);
                    node.put("total", total);
                } else {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    for (int i = 0; i < total; i++) {
                        outputStream.write(blobMap.get(group + "#" + i));
                    }

                    MultipartFile file = new MockMultipartFile(
                        fileMap.get(group + "#0").getName(),
                        fileMap.get(group + "#0").getOriginalFilename(),
                        fileMap.get(group + "#0").getContentType(),
                        outputStream.toByteArray()
                    );

                    node = upload(type, id, file);

                    for (Iterator<Entry<String, MultipartFile>> itf = fileMap.entrySet().iterator(); itf.hasNext();) {
                        Entry<String, MultipartFile> itemf = itf.next();
                        if (itemf.getKey().split("#")[0].equals(group)) {
                            itf.remove();
                        }
                    }
                    for (Iterator<Entry<String, byte[]>> itb = blobMap.entrySet().iterator(); itb.hasNext();) {
                        Entry<String, byte[]> itemb = itb.next();
                        if (itemb.getKey().split("#")[0].equals(group)) {
                            itb.remove();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                node.put("uploaded", 0);
                error.put("message", e.getMessage());
                node.set("error", error);
            }
        }

        return node;
    }

    @Override
    public Set<String> listId(String type) {
        Set<String> idSet = new HashSet<String>();

        File d = new File(savePath + "/" + type);
        if (!d.exists()) {
            return idSet;
        }

        for (File f : d.listFiles()) {
            if (f.isDirectory()) {
                idSet.add(f.getAbsolutePath().substring(d.getAbsolutePath().length() + 1).split("/")[1]);
            }
        }

        return idSet;
    }

    @Override
    public Set<String> listFile(String type, String id) {
        Set<String> fileSet = new HashSet<String>();

        File d = new File(savePath + "/" + type + "/" + id);
        if (!d.exists()) {
            return fileSet;
        }

        for (File f : d.listFiles()) {
            if (!f.isDirectory()) {
                fileSet.add(f.getAbsolutePath().substring(d.getAbsolutePath().length() + 1).split("/")[2]);
            }
        }

        return fileSet;
    }

    @Override
    public void upload(String type, String id, String name, byte[] blob) {
        FileOutputStream fos = null;

        try {
            File d = new File(savePath);
            if (!d.exists()) {
                d.mkdir();
            }
            d = new File(savePath + "/" + type);
            if (!d.exists()) {
                d.mkdir();
            }
            d = new File(savePath + "/" + type + "/" + id);
            if (!d.exists()) {
                d.mkdir();
            }

            File f = new File(savePath + "/" + type + "/" + id + "/" + name);
            f.createNewFile();
            fos = new FileOutputStream(f);
            fos.write(blob);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public byte[] download(String type, String id, String name) {
        FileInputStream fis = null;
        byte[] blob = null;

        try {
            File f = new File(savePath + "/" + type + "/" + id + "/" + name);
            if (!f.exists()) {
                return blob;
            }

            blob = new byte[(int) f.length()];
            fis = new FileInputStream(f);
            fis.read(blob);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return blob;
    }

    @Override
    public void delete(String type, String id) {
        File d = new File(savePath + "/" + type + "/" + id);
        if (!d.exists()) {
            return;
        }

        for (File f : d.listFiles()) {
            if (!f.isDirectory()) {
                f.delete();
            }
        }
    }

    @Override
    public void delete(String type, String id, String name) {
        File f = new File(savePath + "/" + type + "/" + id + "/" + name);
        if (!f.exists()) {
            return;
        }
        f.delete();
    }

}
