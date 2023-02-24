package cc.tonyhook.berry.backend.entity.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cc.tonyhook.berry.backend.entity.ContainedManagedResource;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cms_picture", indexes = {
    @Index(columnList = "gallery_id"),
    @Index(columnList = "sequence"),
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Picture extends ContainedManagedResource {

    private String image;

    @ManyToOne
    private Gallery gallery;

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Gallery getGallery() {
        return this.gallery;
    }

    public void setGallery(Gallery gallery) {
        this.gallery = gallery;
    }

}
