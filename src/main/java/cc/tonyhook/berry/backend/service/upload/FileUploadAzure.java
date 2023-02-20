package cc.tonyhook.berry.backend.service.upload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cc.tonyhook.berry.backend.service.shared.HashHelperService;
import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(prefix = "app.file", name = "provider", havingValue = "azure")
public class FileUploadAzure implements FileUploadService {

    @Value("${app.file.server-path}")
    private String serverPath;
    @Value("${app.file.azure.account-name}")
    private String azureAccountName;
    @Value("${app.file.azure.account-key}")
    private String azureAccountKey;
    @Value("${app.file.azure.endpoint-suffix}")
    private String azureEndpointSuffix;
    @Value("${app.file.azure.container-name}")
    private String azureContainerName;

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

        try {
            String hashtext = HashHelperService.hash(upload.getBytes(), "MD5");
            String ext = getProperExtension(upload);

            String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + azureAccountName + ";AccountKey=" + azureAccountKey + ";EndpointSuffix=" + azureEndpointSuffix;
            String blobName = type + "/" + id + "/" + hashtext + ext;

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(storageConnectionString).buildClient();
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(azureContainerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            if (!blobClient.exists()) {
                blobClient.upload(upload.getInputStream(), upload.getSize());

                byte[] thumbnail = getThumbnail(upload.getBytes());
                if (thumbnail != null) {
                    blobClient = containerClient.getBlobClient(blobName + ".thumbnail");
                    if (!blobClient.exists()) {
                        blobClient.upload(new ByteArrayInputStream(thumbnail), thumbnail.length);
                    }

                    node.put("uploaded", 1);
                    node.put("fileName", hashtext + ext);
                    node.put("url", serverPath + type + "/" + id + "/" + hashtext);
                } else {
                    node.put("uploaded", 1);
                    node.put("fileName", hashtext + ext);
                    node.put("url", serverPath + type + "/" + id + "/" + hashtext);
                }
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
            node.set("error", error);
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

        try {
            String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + azureAccountName + ";AccountKey=" + azureAccountKey + ";EndpointSuffix=" + azureEndpointSuffix;
            String keyPrefix = type + "/";
            String delimiter = "/";

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(storageConnectionString).buildClient();
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(azureContainerName);
            ListBlobsOptions options = new ListBlobsOptions().setPrefix(keyPrefix);

            containerClient.listBlobsByHierarchy(delimiter, options, null).forEach(blob -> {
                if (blob.isPrefix()) {
                    idSet.add(blob.getName().split("/")[1]);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return idSet;
    }

    @Override
    public Set<String> listFile(String type, String id) {
        Set<String> fileSet = new HashSet<String>();

        try {
            String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + azureAccountName + ";AccountKey=" + azureAccountKey + ";EndpointSuffix=" + azureEndpointSuffix;
            String keyPrefix = type + "/" + id + "/";
            String delimiter = "/";

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(storageConnectionString).buildClient();
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(azureContainerName);
            ListBlobsOptions options = new ListBlobsOptions().setPrefix(keyPrefix);

            containerClient.listBlobsByHierarchy(delimiter, options, null).forEach(blob -> {
                if (!blob.isPrefix()) {
                    fileSet.add(blob.getName().split("/")[2]);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileSet;
    }

    @Override
    public void upload(String type, String id, String name, byte[] blob) {
        try {
            String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + azureAccountName + ";AccountKey=" + azureAccountKey + ";EndpointSuffix=" + azureEndpointSuffix;
            String blobName = type + "/" + id + "/" + name;

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(storageConnectionString).buildClient();
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(azureContainerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            if (!blobClient.exists()) {
                blobClient.upload(new ByteArrayInputStream(blob), blob.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] download(String type, String id, String name) {
        byte[] blob = null;

        try {
            String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + azureAccountName + ";AccountKey=" + azureAccountKey + ";EndpointSuffix=" + azureEndpointSuffix;
            String blobName = type + "/" + id + "/" + name;

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(storageConnectionString).buildClient();
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(azureContainerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.downloadStream(outputStream);
            blob = outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return blob;
    }

    @Override
    public void delete(String type, String id) {
        try {
            String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + azureAccountName + ";AccountKey=" + azureAccountKey + ";EndpointSuffix=" + azureEndpointSuffix;
            String keyPrefix = type + "/" + id + "/";
            String delimiter = "/";

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(storageConnectionString).buildClient();
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(azureContainerName);
            ListBlobsOptions options = new ListBlobsOptions().setPrefix(keyPrefix);

            containerClient.listBlobsByHierarchy(delimiter, options, null).forEach(blob -> {
                if (!blob.isPrefix()) {
                    BlobClient blobClient = containerClient.getBlobClient(blob.getName());
                    blobClient.delete();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String type, String id, String name) {
        try {
            String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + azureAccountName + ";AccountKey=" + azureAccountKey + ";EndpointSuffix=" + azureEndpointSuffix;
            String blobName = type + "/" + id + "/" + name;

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(storageConnectionString).buildClient();
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(azureContainerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
