package cc.tonyhook.berry.backend.entity.cms;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cc.tonyhook.berry.backend.entity.ManagedResource;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "cms_gallery", indexes = {
    @Index(columnList = "type"),
    @Index(columnList = "topic_id"),
    @Index(columnList = "updateTime"),
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Gallery extends ManagedResource {

    private String type;

    private String image;

    @ManyToOne
    private Topic topic;

    @OneToMany(mappedBy = "gallery")
    @OrderBy("sequence ASC")
    @JsonIgnore
    private List<Picture> pictures;

    @ManyToMany
    private Set<Tag> tags;

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

    public Topic getTopic() {
        return this.topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public List<Picture> getPictures() {
        return this.pictures;
    }

    public void setPictures(List<Picture> pictures) {
        this.pictures = pictures;
    }

    public Set<Tag> getTags() {
        return this.tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

}
