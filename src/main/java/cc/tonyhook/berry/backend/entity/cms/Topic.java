package cc.tonyhook.berry.backend.entity.cms;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cc.tonyhook.berry.backend.entity.ManagedResource;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "cms_topic", indexes = {
    @Index(columnList = "type"),
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = { "name" })
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Topic extends ManagedResource {

    private String type;

    private String image;

    @OneToMany(mappedBy = "topic")
    @JsonIgnore
    private Set<Column> columns;

    @OneToMany(mappedBy = "topic")
    @JsonIgnore
    private Set<Gallery> galleries;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Set<Column> getColumns() {
        return this.columns;
    }

    public void setColumns(Set<Column> columns) {
        this.columns = columns;
    }

    public Set<Gallery> getGalleries() {
        return this.galleries;
    }

    public void setGalleries(Set<Gallery> galleries) {
        this.galleries = galleries;
    }

}
