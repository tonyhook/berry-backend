package cc.tonyhook.berry.backend.service.cms;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.cms.ContentRepository;
import cc.tonyhook.berry.backend.dao.security.PermissionRepository;
import cc.tonyhook.berry.backend.entity.cms.Column;
import cc.tonyhook.berry.backend.entity.cms.Content;
import jakarta.transaction.Transactional;

@Service
public class ContentService {

    @Autowired
    private ContentRepository contentRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Content> getContentList() {
        List<Content> contentList = contentRepository.findAll();

        return contentList;
    }

    @PreAuthorize("hasPermission(#column.id, 'column', 'r')")
    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Content> getContentList(Column column) {
        List<Content> contentList = null;
        if (column != null) {
            contentList = contentRepository.findByColumnOrderBySequence(column);
        }

        return contentList;
    }

    @PreAuthorize("hasPermission(#id, 'content', 'r')")
    public Content getContent(Integer id) {
        Content content = contentRepository.findById(id).orElse(null);

        return content;
    }

    @PreAuthorize("hasPermission(#newContent, 'c')")
    public Content addContent(Content newContent) {
        newContent.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newContent.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        Content updatedContent = contentRepository.save(newContent);

        return updatedContent;
    }

    @PreAuthorize("hasPermission(#id, 'content', 'u')")
    public void updateContent(Integer id, Content newContent) {
        newContent.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        contentRepository.save(newContent);
    }

    @Transactional
    @PreAuthorize("hasPermission(#id, 'content', 'd')")
    public void removeContent(Integer id) {
        Content deletedContent = contentRepository.findById(id).orElse(null);

        permissionRepository.deleteByResourceTypeAndResourceId("content", id);
        contentRepository.delete(deletedContent);
    }

}
