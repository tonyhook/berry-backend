package cc.tonyhook.berry.backend.entity.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cc.tonyhook.berry.backend.entity.SequenceManagedResource;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "cms_carousel", indexes = {
    @Index(columnList = "list"),
    @Index(columnList = "sequence"),
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Carousel extends SequenceManagedResource {

    private String list;

    private String image;

    private String link;

    public String getList() {
        return this.list;
    }

    public void setList(String list) {
        this.list = list;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

}
