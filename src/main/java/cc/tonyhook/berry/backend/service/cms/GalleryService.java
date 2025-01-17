package cc.tonyhook.berry.backend.service.cms;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.cms.GalleryRepository;
import cc.tonyhook.berry.backend.dao.security.PermissionRepository;
import cc.tonyhook.berry.backend.entity.cms.Gallery;
import cc.tonyhook.berry.backend.entity.cms.Tag;
import cc.tonyhook.berry.backend.entity.cms.Topic;
import jakarta.transaction.Transactional;

@Service
public class GalleryService {

    @Autowired
    private GalleryRepository galleryRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Gallery> getGalleryList() {
        List<Gallery> galleryList = galleryRepository.findAll();

        return galleryList;
    }

    public PagedModel<Gallery> getGalleryList(String type, Pageable pageable) {
        PagedModel<Gallery> galleryPage = new PagedModel<>(galleryRepository.findByType(type, pageable));

        return galleryPage;
    }

    public PagedModel<Gallery> getGalleryList(String type, Boolean disabled, Pageable pageable) {
        PagedModel<Gallery> galleryPage = new PagedModel<>(galleryRepository.findByTypeAndDisabled(type, disabled, pageable));

        return galleryPage;
    }

    public PagedModel<Gallery> getGalleryList(Topic topic, Pageable pageable) {
        PagedModel<Gallery> galleryPage = new PagedModel<>(galleryRepository.findByTopic(topic, pageable));

        return galleryPage;
    }

    public PagedModel<Gallery> getGalleryList(Topic topic, Boolean disabled, Pageable pageable) {
        PagedModel<Gallery> galleryPage = new PagedModel<>(galleryRepository.findByTopicAndDisabledOrderByUpdateTimeDesc(topic, disabled, pageable));

        return galleryPage;
    }

    public PagedModel<Gallery> getGalleryList(Tag tag, Pageable pageable) {
        PagedModel<Gallery> galleryPage = new PagedModel<>(galleryRepository.findByTagsContains(tag, pageable));

        return galleryPage;
    }

    public PagedModel<Gallery> getGalleryList(Tag tag, Boolean disabled, Pageable pageable) {
        PagedModel<Gallery> galleryPage = new PagedModel<>(galleryRepository.findByTagsContainsAndDisabledOrderByUpdateTimeDesc(tag, disabled, pageable));

        return galleryPage;
    }

    @PreAuthorize("hasPermission(#id, 'gallery', 'r')")
    public Gallery getGallery(Integer id) {
        Gallery gallery = galleryRepository.findById(id).orElse(null);

        return gallery;
    }

    @PreAuthorize("hasPermission(#id, 'gallery', 'r')")
    public Gallery getGallery(Integer id, Boolean disabled) {
        Gallery gallery = galleryRepository.findByIdAndDisabled(id, disabled);

        return gallery;
    }

    @PreAuthorize("hasPermission(#newGallery, 'c')")
    public Gallery addGallery(Gallery newGallery) {
        newGallery.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newGallery.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        Gallery updatedGallery = galleryRepository.save(newGallery);

        return updatedGallery;
    }

    @PreAuthorize("hasPermission(#id, 'gallery', 'u')")
    public void updateGallery(Integer id, Gallery newGallery) {
        newGallery.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        galleryRepository.save(newGallery);
    }

    @Transactional
    @PreAuthorize("hasPermission(#id, 'gallery', 'd')")
    public void removeGallery(Integer id) {
        Gallery deletedGallery = galleryRepository.findById(id).orElse(null);

        permissionRepository.deleteByResourceTypeAndResourceId("gallery", id);
        galleryRepository.delete(deletedGallery);
    }

}
