package cc.tonyhook.berry.backend.entity.cms;

import java.sql.Timestamp;
import java.util.Set;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cc.tonyhook.berry.backend.entity.ContainedManagedResource;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@DynamicUpdate
@DynamicInsert
@Entity
@Table(name = "cms_content", indexes = {
    @Index(columnList = "column_id"),
    @Index(columnList = "sequence"),
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Content extends ContainedManagedResource {

    private String type;

    private String title;

    private String subtitle;

    private String description;

    private String feedsThumb;

    private String headerImage;

    private String article;

    private String video;

    private String poster;

    private String pdf;

    private String link;

    private Timestamp validTime;

    @ManyToMany
    private Set<Tag> tags;

    @ManyToOne
    private Column column;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return this.subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFeedsThumb() {
        return this.feedsThumb;
    }

    public void setFeedsThumb(String feedsThumb) {
        this.feedsThumb = feedsThumb;
    }

    public String getHeaderImage() {
        return this.headerImage;
    }

    public void setHeaderImage(String headerImage) {
        this.headerImage = headerImage;
    }

    public String getArticle() {
        return this.article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getVideo() {
        return this.video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getPoster() {
        return this.poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getPdf() {
        return this.pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Timestamp getValidTime() {
        return this.validTime;
    }

    public void setValidTime(Timestamp validTime) {
        this.validTime = validTime;
    }

    public Set<Tag> getTags() {
        return this.tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Column getColumn() {
        return this.column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

}
