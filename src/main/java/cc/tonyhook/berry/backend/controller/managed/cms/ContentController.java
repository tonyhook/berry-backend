package cc.tonyhook.berry.backend.controller.managed.cms;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.cms.Column;
import cc.tonyhook.berry.backend.entity.cms.Content;
import cc.tonyhook.berry.backend.service.cms.ColumnService;
import cc.tonyhook.berry.backend.service.cms.ContentService;
import cc.tonyhook.berry.backend.service.upload.FileUploadService;

@RestController
public class ContentController {

    @Autowired
    private ColumnService columnService;
    @Autowired
    private ContentService contentService;
    @Autowired
    private FileUploadService fileUploadService;

    @RequestMapping(value = "/api/managed/content", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Content>> getContentList(
            @RequestParam(defaultValue = "0") Integer columnId) {
        Column column = columnService.getColumn(columnId);

        if (column == null) {
            return ResponseEntity.notFound().build();
        }

        List<Content> contentList = contentService.getContentList(column);

        return ResponseEntity.ok().body(contentList);
    }

    @RequestMapping(value = "/api/managed/content/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Content> getContent(
            @PathVariable Integer id) {
        Content content = contentService.getContent(id);

        if (content != null) {
            return ResponseEntity.ok().body(content);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/content", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Content> addContent(
            @RequestBody Content newContent) throws URISyntaxException {
        Content updatedContent = contentService.addContent(newContent);

        return ResponseEntity
                .created(new URI("/api/managed/content/" + updatedContent.getId()))
                .body(updatedContent);
    }

    @RequestMapping(value = "/api/managed/content/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateContent(
            @PathVariable Integer id,
            @RequestBody Content newContent) {
        if (!id.equals(newContent.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Content targetContent = contentService.getContent(id);
        if (targetContent == null) {
            return ResponseEntity.notFound().build();
        }

        if (targetContent.getHeaderImage() != null && !targetContent.getHeaderImage().equals(newContent.getHeaderImage())) {
            fileUploadService.delete("content", String.valueOf(id), targetContent.getHeaderImage());
            fileUploadService.delete("content", String.valueOf(id), targetContent.getHeaderImage() + ".thumbnail");
        }
        if (targetContent.getFeedsThumb() != null && !targetContent.getFeedsThumb().equals(newContent.getFeedsThumb())) {
            fileUploadService.delete("content", String.valueOf(id), targetContent.getFeedsThumb());
            fileUploadService.delete("content", String.valueOf(id), targetContent.getFeedsThumb() + ".thumbnail");
        }
        if (targetContent.getPoster() != null && !targetContent.getPoster().equals(newContent.getPoster())) {
            fileUploadService.delete("content", String.valueOf(id), targetContent.getPoster());
            fileUploadService.delete("content", String.valueOf(id), targetContent.getPoster() + ".thumbnail");
        }
        if (targetContent.getArticle() != null && !targetContent.getArticle().equals(newContent.getArticle())) {
            fileUploadService.delete("content", String.valueOf(id), targetContent.getArticle());
        }
        if (targetContent.getPdf() != null && !targetContent.getPdf().equals(newContent.getPdf())) {
            fileUploadService.delete("content", String.valueOf(id), targetContent.getPdf());
        }
        if (targetContent.getVideo() != null && !targetContent.getVideo().equals(newContent.getVideo())) {
            fileUploadService.delete("content", String.valueOf(id), targetContent.getVideo());
        }

        contentService.updateContent(id, newContent);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/content/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeContent(
            @PathVariable Integer id) {
        Content deletedContent = contentService.getContent(id);
        if (deletedContent == null) {
            return ResponseEntity.notFound().build();
        }

        if (deletedContent.getHeaderImage() != null) {
            fileUploadService.delete("content", String.valueOf(id), deletedContent.getHeaderImage());
            fileUploadService.delete("content", String.valueOf(id), deletedContent.getHeaderImage() + ".thumbnail");
        }
        if (deletedContent.getFeedsThumb() != null) {
            fileUploadService.delete("content", String.valueOf(id), deletedContent.getFeedsThumb());
            fileUploadService.delete("content", String.valueOf(id), deletedContent.getFeedsThumb() + ".thumbnail");
        }
        if (deletedContent.getPoster() != null) {
            fileUploadService.delete("content", String.valueOf(id), deletedContent.getPoster());
            fileUploadService.delete("content", String.valueOf(id), deletedContent.getPoster() + ".thumbnail");
        }
        if (deletedContent.getArticle() != null) {
            fileUploadService.delete("content", String.valueOf(id), deletedContent.getArticle());
        }
        if (deletedContent.getPdf() != null) {
            fileUploadService.delete("content", String.valueOf(id), deletedContent.getPdf());
        }
        if (deletedContent.getVideo() != null) {
            fileUploadService.delete("content", String.valueOf(id), deletedContent.getVideo());
        }

        contentService.removeContent(id);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/content/cleanup", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<String>> cleanupContent(
            @RequestParam(defaultValue = "false") Boolean delete) {
        List<Content> contentList = contentService.getContentList();
        Map<Integer, Content> contentMap = new HashMap<Integer, Content>();
        List<String> action = new ArrayList<String>();

        for (Content content : contentList) {
            contentMap.put(content.getId(), content);
        }

        Set<String> contentIDSet = fileUploadService.listId("content");

        for (String contentID : contentIDSet) {
            if (NumberUtils.isCreatable(contentID)) {
                if (!contentMap.containsKey(NumberUtils.toInt(contentID))) {
                    if (delete) {
                        fileUploadService.delete("content", contentID);
                    }
                    action.add("DELETE content: " + contentID);
                } else {
                    Boolean imageFoundFeedsThumb = false;
                    Boolean thumbnailFoundFeedsThumb = false;
                    Boolean imageFoundHeaderImage = false;
                    Boolean thumbnailFoundHeaderImage = false;
                    Boolean imageFoundPoster = false;
                    Boolean thumbnailFoundPoster = false;

                    Set<String> contentFileSet = fileUploadService.listFile("content", contentID);
                    for (String contentFile : contentFileSet) {
                        if (contentFile.equals(contentMap.get(NumberUtils.toInt(contentID)).getFeedsThumb())) {
                            imageFoundFeedsThumb = true;
                        } else if (contentFile.equals(contentMap.get(NumberUtils.toInt(contentID)).getFeedsThumb() + ".thumbnail")) {
                            thumbnailFoundFeedsThumb = true;
                        } else if (contentFile.equals(contentMap.get(NumberUtils.toInt(contentID)).getHeaderImage())) {
                            imageFoundHeaderImage = true;
                        } else if (contentFile.equals(contentMap.get(NumberUtils.toInt(contentID)).getHeaderImage() + ".thumbnail")) {
                            thumbnailFoundHeaderImage = true;
                        } else if (contentFile.equals(contentMap.get(NumberUtils.toInt(contentID)).getPoster())) {
                            imageFoundPoster = true;
                        } else if (contentFile.equals(contentMap.get(NumberUtils.toInt(contentID)).getPoster() + ".thumbnail")) {
                            thumbnailFoundPoster = true;
                        } else if (!contentFile.equals(contentMap.get(NumberUtils.toInt(contentID)).getArticle())
                                && !contentFile.equals(contentMap.get(NumberUtils.toInt(contentID)).getPdf())
                                && !contentFile.equals(contentMap.get(NumberUtils.toInt(contentID)).getVideo())) {
                            if (delete) {
                                fileUploadService.delete("content", contentID, contentFile);
                            }
                            action.add("DELETE content: " + contentID + "/" + contentFile);
                        }
                    }

                    if (imageFoundFeedsThumb && !thumbnailFoundFeedsThumb) {
                        byte[] image = fileUploadService.download("content", contentID, contentMap.get(NumberUtils.toInt(contentID)).getFeedsThumb());
                        byte[] thumbnail = fileUploadService.getThumbnail(image);
                        fileUploadService.upload("content", contentID, contentMap.get(NumberUtils.toInt(contentID)).getFeedsThumb() + ".thumbnail", thumbnail);
                        action.add("CREATE content: " + contentID + "/" + contentMap.get(NumberUtils.toInt(contentID)).getFeedsThumb() + ".thumbnail");
                    }
                    if (imageFoundHeaderImage && !thumbnailFoundHeaderImage) {
                        byte[] image = fileUploadService.download("content", contentID, contentMap.get(NumberUtils.toInt(contentID)).getHeaderImage());
                        byte[] thumbnail = fileUploadService.getThumbnail(image);
                        fileUploadService.upload("content", contentID, contentMap.get(NumberUtils.toInt(contentID)).getHeaderImage() + ".thumbnail", thumbnail);
                        action.add("CREATE content: " + contentID + "/" + contentMap.get(NumberUtils.toInt(contentID)).getHeaderImage() + ".thumbnail");
                    }
                    if (imageFoundPoster && !thumbnailFoundPoster) {
                        byte[] image = fileUploadService.download("content", contentID, contentMap.get(NumberUtils.toInt(contentID)).getPoster());
                        byte[] thumbnail = fileUploadService.getThumbnail(image);
                        fileUploadService.upload("content", contentID, contentMap.get(NumberUtils.toInt(contentID)).getPoster() + ".thumbnail", thumbnail);
                        action.add("CREATE content: " + contentID + "/" + contentMap.get(NumberUtils.toInt(contentID)).getPoster() + ".thumbnail");
                    }
                }
            }
        }

        return ResponseEntity.ok().body(action);
    }

}
