package cc.tonyhook.berry.backend.entity.cms;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cc.tonyhook.berry.backend.entity.ManagedResource;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "cms_tag", indexes = {
    @Index(columnList = "type"),
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tag extends ManagedResource {

    private String type;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<Content> contents;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<Gallery> galleries;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<Content> getContents() {
        return this.contents;
    }

    public void setContents(Set<Content> contents) {
        this.contents = contents;
    }

    public Set<Gallery> getGalleries() {
        return this.galleries;
    }

    public void setGalleries(Set<Gallery> galleries) {
        this.galleries = galleries;
    }

}
