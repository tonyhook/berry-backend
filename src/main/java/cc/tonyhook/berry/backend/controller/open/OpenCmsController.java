package cc.tonyhook.berry.backend.controller.open;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.cms.Carousel;
import cc.tonyhook.berry.backend.entity.cms.Column;
import cc.tonyhook.berry.backend.entity.cms.Content;
import cc.tonyhook.berry.backend.entity.cms.Gallery;
import cc.tonyhook.berry.backend.entity.cms.Picture;
import cc.tonyhook.berry.backend.entity.cms.Popup;
import cc.tonyhook.berry.backend.entity.cms.Tag;
import cc.tonyhook.berry.backend.entity.cms.Topic;
import cc.tonyhook.berry.backend.service.cms.CarouselService;
import cc.tonyhook.berry.backend.service.cms.ColumnService;
import cc.tonyhook.berry.backend.service.cms.ContentService;
import cc.tonyhook.berry.backend.service.cms.GalleryService;
import cc.tonyhook.berry.backend.service.cms.PictureService;
import cc.tonyhook.berry.backend.service.cms.PopupService;
import cc.tonyhook.berry.backend.service.cms.TagService;
import cc.tonyhook.berry.backend.service.cms.TopicService;

@RestController
public class OpenCmsController {

    @Autowired
    private CarouselService carouselService;
    @Autowired
    private ColumnService columnService;
    @Autowired
    private ContentService contentService;
    @Autowired
    private GalleryService galleryService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private PopupService popupService;
    @Autowired
    private TagService tagService;
    @Autowired
    private TopicService topicService;

    @RequestMapping(value = "/api/open/carousel", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Carousel>> getCarouselList(
            @RequestParam(defaultValue = "") String list) {
        List<Carousel> carouselList = carouselService.getCarouselList(list, false);

        return ResponseEntity.ok().body(carouselList);
    }

    @RequestMapping(value = "/api/open/carousel/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Carousel> getCarousel(
            @PathVariable Integer id) {
        Carousel carousel = carouselService.getCarousel(id, false);

        if (carousel != null) {
            return ResponseEntity.ok().body(carousel);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/column", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Column>> getColumnListByParent(
            @RequestParam(defaultValue = "0") Integer parentColumnId) {
        Column column;

        if (parentColumnId <= 0) {
            column = null;
        } else {
            column = columnService.getColumn(parentColumnId);
            if (column == null) {
                return ResponseEntity.notFound().build();
            }
        }

        List<Column> columnList = columnService.getColumnList(column, false);

        return ResponseEntity.ok().body(columnList);
    }

    @RequestMapping(value = "/api/open/column/topic", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Column>> getColumnListByTopic(
            @RequestParam(defaultValue = "0") Integer topicId) {
        Topic topic = topicService.getTopic(topicId);
        if (topic == null) {
            return ResponseEntity.notFound().build();
        }

        List<Column> columnList = columnService.getColumnList(topic, false);

        return ResponseEntity.ok().body(columnList);
    }

    @RequestMapping(value = "/api/open/column/{nameOrId}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Column> getColumn(
            @PathVariable String nameOrId,
            @RequestParam(defaultValue = "0") Integer rootColumnId) {
        Column column = null;
        Integer columnId = null;
        String columnName = null;

        if (NumberUtils.isCreatable(nameOrId)) {
            columnId = NumberUtils.toInt(nameOrId);
            columnName = nameOrId;
        } else {
            columnId = null;
            columnName = nameOrId;
        }

        Column rootColumn;

        if (rootColumnId <= 0) {
            rootColumn = null;
        } else {
            rootColumn = columnService.getColumn(rootColumnId, false);
            if (rootColumn == null) {
                return ResponseEntity.notFound().build();
            }
        }

        if (columnId != null && rootColumn == null) {
            column = columnService.getColumn(columnId, false);
            if (column.getParentId() != null) {
                column = null;
            }
        }
        if (columnId != null && rootColumn != null) {
            column = columnService.getColumn(columnId, false);

            Boolean isChild = false;
            Column ancestor = column;
            while (ancestor != null) {
                if (ancestor.getParentId() != null && ancestor.getParentId().equals(rootColumnId)) {
                    isChild = true;
                    break;
                }

                ancestor = columnService.getColumn(ancestor.getParentId(), false);
            }

            if (!isChild) {
                column = null;
            }
        }
        if (columnId == null && rootColumn == null) {
            column = columnService.getColumn(columnName, false);
            if (column.getParentId() != null) {
                column = null;
            }
        }
        if (columnId == null && rootColumn != null) {
            column = columnService.getColumn(columnName, false);

            Boolean isChild = false;
            Column ancestor = column;
            while (ancestor != null) {
                if (ancestor.getParentId() != null && ancestor.getParentId().equals(rootColumnId)) {
                    isChild = true;
                    break;
                }

                ancestor = columnService.getColumn(ancestor.getParentId(), false);
            }

            if (!isChild) {
                column = null;
            }
        }

        if (column != null) {
            return ResponseEntity.ok().body(column);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/content", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Page<Content>> getContentList(
            @RequestParam(defaultValue = "0") Integer columnId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Column column;

        if (columnId <= 0) {
            column = null;
        } else {
            column = columnService.getColumn(columnId, false);
        }

        if (column == null) {
            return ResponseEntity.notFound().build();
        }

        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        Page<Content> contentPage = contentService.getContentList(column, false, pageable);

        if (contentPage != null) {
            return ResponseEntity.ok().body(contentPage);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/content/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Content> getContent(
            @PathVariable Integer id) {
        Content content = contentService.getContent(id, false);

        if (content != null) {
            return ResponseEntity.ok().body(content);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/gallery/type", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Page<Gallery>> getGalleryListByType(
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        Page<Gallery> galleryPage = galleryService.getGalleryList(type, false, pageable);

        return ResponseEntity.ok().body(galleryPage);
    }

    @RequestMapping(value = "/api/open/gallery/tag", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Page<Gallery>> getGalleryListByTag(
            @RequestParam(defaultValue = "0") Integer tagId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Tag tag = tagService.getTag(tagId);
        if (tag == null) {
            return ResponseEntity.notFound().build();
        }

        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        Page<Gallery> galleryPage = galleryService.getGalleryList(tag, false, pageable);

        return ResponseEntity.ok().body(galleryPage);
    }

    @RequestMapping(value = "/api/open/gallery/topic", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Page<Gallery>> getGalleryListByTopic(
            @RequestParam(defaultValue = "0") Integer topicId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Topic topic = topicService.getTopic(topicId);
        if (topic == null) {
            return ResponseEntity.notFound().build();
        }

        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        Page<Gallery> galleryPage = galleryService.getGalleryList(topic, false, pageable);

        return ResponseEntity.ok().body(galleryPage);
    }

    @RequestMapping(value = "/api/open/gallery/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Gallery> getGallery(
            @PathVariable Integer id) {
        Gallery gallery = galleryService.getGallery(id, false);

        if (gallery != null) {
            return ResponseEntity.ok().body(gallery);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/picture", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Picture>> getPictureList(
            @RequestParam(defaultValue = "0") Integer galleryId) {
        Gallery gallery = galleryService.getGallery(galleryId);

        if (gallery == null) {
            return ResponseEntity.notFound().build();
        }

        List<Picture> pictureList = pictureService.getPictureList(gallery);

        return ResponseEntity.ok().body(pictureList);
    }

    @RequestMapping(value = "/api/open/picture/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Picture> getPicture(
            @PathVariable Integer id) {
        Picture picture = pictureService.getPicture(id, false);

        if (picture != null) {
            return ResponseEntity.ok().body(picture);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/popup", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Popup>> getPopupList(
            @RequestParam(defaultValue = "") String list,
            @RequestParam(defaultValue = "") String openid) {
        List<Popup> popupList = popupService.getPopupList(list, false, openid);

        return ResponseEntity.ok().body(popupList);
    }

    @RequestMapping(value = "/api/open/popup/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Popup> getPopup(
            @PathVariable Integer id) {
        Popup popup = popupService.getPopup(id, false);

        if (popup != null) {
            return ResponseEntity.ok().body(popup);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/tag/type", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Page<Tag>> getTagListByType(
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        Page<Tag> tagPage = tagService.getTagList(type, false, pageable);

        return ResponseEntity.ok().body(tagPage);
    }

    @RequestMapping(value = "/api/open/tag/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Tag> getTag(
            @PathVariable Integer id) {
        Tag tag = tagService.getTag(id, false);

        if (tag != null) {
            return ResponseEntity.ok().body(tag);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/topic/type", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Page<Topic>> getTopicListByType(
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        Page<Topic> topicPage = topicService.getTopicList(type, false, pageable);

        return ResponseEntity.ok().body(topicPage);
    }

    @RequestMapping(value = "/api/open/topic/{nameOrId}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Topic> getTopic(
            @PathVariable String nameOrId) {
        Topic topic = null;
        Integer topicId = null;
        String topicName = null;

        if (NumberUtils.isCreatable(nameOrId)) {
            topicId = NumberUtils.toInt(nameOrId);
            topicName = nameOrId;
        } else {
            topicId = null;
            topicName = nameOrId;
        }

        if (topicId != null) {
            topic = topicService.getTopic(topicId, false);
        } else {
            topic = topicService.getTopic(topicName, false);
        }

        if (topic != null) {
            return ResponseEntity.ok().body(topic);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
