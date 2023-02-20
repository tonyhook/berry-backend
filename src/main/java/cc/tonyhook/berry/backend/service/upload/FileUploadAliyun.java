package cc.tonyhook.berry.backend.service.upload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsV2Request;
import com.aliyun.oss.model.ListObjectsV2Result;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.PutObjectRequest;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cc.tonyhook.berry.backend.service.shared.HashHelperService;
import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(prefix = "app.file", name = "provider", havingValue = "aliyun")
public class FileUploadAliyun implements FileUploadService {

    @Value("${app.file.server-path}")
    private String serverPath;
    @Value("${app.file.aliyun.access-key-id}")
    private String accessKeyId;
    @Value("${app.file.aliyun.access-key-secret}")
    private String accessKeySecret;
    @Value("${app.file.aliyun.bucket-name}")
    private String bucketName;
    @Value("${app.file.aliyun.endpoint}")
    private String endpoint;

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

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            String hashtext = HashHelperService.hash(upload.getBytes(), "MD5");
            String ext = getProperExtension(upload);

            String blobName = type + "/" + id + "/" + hashtext + ext;

            if (!ossClient.doesObjectExist(bucketName, blobName)) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, blobName, upload.getInputStream());
                ossClient.putObject(putObjectRequest);

                byte[] thumbnail = getThumbnail(upload.getBytes());
                if (thumbnail != null) {
                    putObjectRequest = new PutObjectRequest(bucketName, blobName + ".thumbnail", new ByteArrayInputStream(thumbnail));
                    ossClient.putObject(putObjectRequest);

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
            if (ossClient != null) {
                ossClient.shutdown();
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

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            String keyPrefix = type + "/";

            String nextContinuationToken = null;
            ListObjectsV2Result result = null;

            do {
                ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request(bucketName);
                listObjectsV2Request.setPrefix(keyPrefix);
                listObjectsV2Request.setMaxKeys(1000);
                listObjectsV2Request.setContinuationToken(nextContinuationToken);
                result = ossClient.listObjectsV2(listObjectsV2Request);

                List<OSSObjectSummary> sums = result.getObjectSummaries();
                for (OSSObjectSummary s : sums) {
                    idSet.add(s.getKey().split("/")[1]);
                }

                nextContinuationToken = result.getNextContinuationToken();
            } while (result.isTruncated());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return idSet;
    }

    @Override
    public Set<String> listFile(String type, String id) {
        Set<String> fileSet = new HashSet<String>();

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            String keyPrefix = type + "/" + id + "/";

            String nextContinuationToken = null;
            ListObjectsV2Result result = null;

            do {
                ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request(bucketName);
                listObjectsV2Request.setPrefix(keyPrefix);
                listObjectsV2Request.setMaxKeys(1000);
                listObjectsV2Request.setContinuationToken(nextContinuationToken);
                result = ossClient.listObjectsV2(listObjectsV2Request);

                List<OSSObjectSummary> sums = result.getObjectSummaries();
                for (OSSObjectSummary s : sums) {
                    fileSet.add(s.getKey().split("/")[2]);
                }

                nextContinuationToken = result.getNextContinuationToken();
            } while (result.isTruncated());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return fileSet;
    }

    @Override
    public void upload(String type, String id, String name, byte[] blob) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            String blobName = type + "/" + id + "/" + name;

            if (!ossClient.doesObjectExist(bucketName, blobName)) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, blobName, new ByteArrayInputStream(blob));
                ossClient.putObject(putObjectRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public byte[] download(String type, String id, String name) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        byte[] blob = null;

        try {
            String blobName = type + "/" + id + "/" + name;

            OSSObject ossObject = ossClient.getObject(bucketName, blobName);
            blob = IOUtils.toByteArray(ossObject.getObjectContent());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return blob;
    }

    @Override
    public void delete(String type, String id) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            String keyPrefix = type + "/" + id + "/";

            String nextContinuationToken = null;
            ListObjectsV2Result result = null;

            do {
                ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request(bucketName);
                listObjectsV2Request.setPrefix(keyPrefix);
                listObjectsV2Request.setMaxKeys(1000);
                listObjectsV2Request.setContinuationToken(nextContinuationToken);

                result = ossClient.listObjectsV2(listObjectsV2Request);
                List<OSSObjectSummary> sums = result.getObjectSummaries();
                for (OSSObjectSummary s : sums) {
                    ossClient.deleteObject(bucketName, s.getKey());
                }

                nextContinuationToken = result.getNextContinuationToken();
            } while (result.isTruncated());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public void delete(String type, String id, String name) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            String blobName = type + "/" + id + "/" + name;

            ossClient.deleteObject(bucketName, blobName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

}
