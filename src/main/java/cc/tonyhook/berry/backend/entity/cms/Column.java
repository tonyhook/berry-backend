package cc.tonyhook.berry.backend.entity.cms;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cc.tonyhook.berry.backend.entity.HierarchyManagedResource;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "cms_column", indexes = {
    @Index(columnList = "parentId"),
    @Index(columnList = "topic_id"),
    @Index(columnList = "sequence"),
    @Index(columnList = "updateTime"),
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = { "name" })
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Column extends HierarchyManagedResource {

    private String description;

    @ManyToOne
    private Topic topic;

    @OneToMany(mappedBy = "column")
    @OrderBy("sequence ASC")
    @JsonIgnore
    private List<Content> contents;

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Content> getContents() {
        return this.contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }

}
