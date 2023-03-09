package cc.tonyhook.berry.backend.service.cms;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.cms.TagRepository;
import cc.tonyhook.berry.backend.dao.security.PermissionRepository;
import cc.tonyhook.berry.backend.entity.cms.Tag;
import jakarta.transaction.Transactional;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Tag> getTagList() {
        List<Tag> tagList = tagRepository.findAll();

        return tagList;
    }

    public Page<Tag> getTagList(String type, Pageable pageable) {
        Page<Tag> tagPage = tagRepository.findByType(type, pageable);

        return tagPage;
    }

    public Page<Tag> getTagList(String type, Boolean disabled, Pageable pageable) {
        Page<Tag> tagPage = tagRepository.findByTypeAndDisabled(type, disabled, pageable);

        return tagPage;
    }

    @PreAuthorize("hasPermission(#id, 'tag', 'r')")
    public Tag getTag(Integer id) {
        Tag tag = tagRepository.findById(id).orElse(null);

        return tag;
    }

    @PreAuthorize("hasPermission(#id, 'tag', 'r')")
    public Tag getTag(Integer id, Boolean disabled) {
        Tag tag = tagRepository.findByIdAndDisabled(id, disabled);

        return tag;
    }

    @PreAuthorize("hasPermission(#newTag, 'c')")
    public Tag addTag(Tag newTag) {
        newTag.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newTag.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        Tag updatedTag = tagRepository.save(newTag);

        return updatedTag;
    }

    @PreAuthorize("hasPermission(#id, 'tag', 'u')")
    public void updateTag(Integer id, Tag newTag) {
        newTag.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        tagRepository.save(newTag);
    }

    @Transactional
    @PreAuthorize("hasPermission(#id, 'tag', 'd')")
    public void removeTag(Integer id) {
        Tag deletedTag = tagRepository.findById(id).orElse(null);

        deletedTag.getContents().clear();
        deletedTag.getGalleries().clear();
        tagRepository.save(deletedTag);

        permissionRepository.deleteByResourceTypeAndResourceId("tag", id);
        tagRepository.delete(deletedTag);
    }

}
