package cc.tonyhook.berry.backend.service.cms;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.cms.ColumnRepository;
import cc.tonyhook.berry.backend.dao.cms.ContentRepository;
import cc.tonyhook.berry.backend.dao.security.PermissionRepository;
import cc.tonyhook.berry.backend.entity.cms.Column;
import cc.tonyhook.berry.backend.entity.cms.Content;
import cc.tonyhook.berry.backend.entity.cms.Topic;
import jakarta.transaction.Transactional;

@Service
public class ColumnService {

    @Autowired
    private ColumnRepository columnRepository;
    @Autowired
    private ContentRepository contentRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Column> getColumnList() {
        List<Column> columnList = columnRepository.findAll();

        return columnList;
    }

    @PreAuthorize("hasPermission(#columnId, 'column', 'r')")
    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Column> getColumnList(Column column) {
        List<Column> columnList;

        if (column == null) {
            columnList = columnRepository.findByParentIdIsNullOrderBySequence();
        } else {
            columnList = columnRepository.findByParentIdOrderBySequence(column.getId());
        }

        return columnList;
    }

    @PreAuthorize("hasPermission(#columnId, 'column', 'r')")
    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Column> getColumnList(Column column, Boolean disabled) {
        List<Column> columnList;

        if (column == null) {
            columnList = columnRepository.findByParentIdIsNullAndDisabledOrderBySequence(disabled);
        } else {
            columnList = columnRepository.findByParentIdAndDisabledOrderBySequence(column.getId(), disabled);
        }

        return columnList;
    }

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Column> getColumnList(Topic topic, Boolean disabled) {
        List<Column> columnList = columnRepository.findByTopicAndDisabledOrderByUpdateTimeDesc(topic, disabled);

        return columnList;
    }

    @PreAuthorize("hasPermission(#id, 'column', 'r')")
    public Column getColumn(Integer id) {
        Column column = columnRepository.findById(id).orElse(null);

        return column;
    }

    @PreAuthorize("hasPermission(#id, 'column', 'r')")
    public Column getColumn(Integer id, Boolean disabled) {
        Column column = columnRepository.findByIdAndDisabled(id, disabled);

        return column;
    }

    @PreAuthorize("hasPermission(#id, 'column', 'r')")
    public Column getColumn(String name, Boolean disabled) {
        Column column = columnRepository.findByNameAndDisabled(name, disabled);

        return column;
    }

    @PreAuthorize("hasPermission(#rootColumnId, 'column', 'r')")
    @PostAuthorize("returnObject == null or hasPermission(returnObject, 'r')")
    public Column getColumn(String name, Column rootColumn) {
        List<Column> searchList = new ArrayList<Column>();
        if (rootColumn == null) {
            searchList.addAll(columnRepository.findByParentIdIsNullOrderBySequence());
        } else {
            searchList.addAll(columnRepository.findByParentIdOrderBySequence(rootColumn.getId()));
        }

        for (int i = 0; i < searchList.size(); i++) {
            Column column = searchList.get(i);

            if (column.getName().equals(name)) {
                return column;
            }

            searchList.addAll(columnRepository.findByParentIdOrderBySequence(column.getId()));
        }

        return null;
    }

    @PreAuthorize("hasPermission(#newColumn, 'c')")
    public Column addColumn(Column newColumn) {
        newColumn.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newColumn.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        Column updatedColumn = columnRepository.save(newColumn);

        return updatedColumn;
    }

    @PreAuthorize("hasPermission(#id, 'column', 'u')")

    public void updateColumn(Integer id, Column newColumn) {
        newColumn.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        columnRepository.save(newColumn);
    }

    @Transactional
    @PreAuthorize("hasPermission(#id, 'column', 'd')")
    public void removeColumn(Integer id) {
        Column deletedColumn = columnRepository.findById(id).orElse(null);

        Iterator<Content> iterator = deletedColumn.getContents().iterator();
        while (iterator.hasNext()) {
            Content content = iterator.next();

            permissionRepository.deleteByResourceTypeAndResourceId("content", content.getId());
            contentRepository.delete(content);
        }

        permissionRepository.deleteByResourceTypeAndResourceId("column", id);
        columnRepository.delete(deletedColumn);
    }

}
