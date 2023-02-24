package cc.tonyhook.berry.backend.entity.cms;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cc.tonyhook.berry.backend.entity.SequenceManagedResource;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "cms_popup", indexes = {
    @Index(columnList = "list"),
    @Index(columnList = "code"),
    @Index(columnList = "sequence"),
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Popup extends SequenceManagedResource {

    private String list;

    private String freq;

    private String terminate;

    private String image;

    private String link;

    private String code;

    private Timestamp startTime;

    private Timestamp endTime;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getList() {
        return this.list;
    }

    public void setList(String list) {
        this.list = list;
    }

    public Integer getSequence() {
        return this.sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getFreq() {
        return this.freq;
    }

    public void setFreq(String freq) {
        this.freq = freq;
    }

    public String getTerminate() {
        return this.terminate;
    }

    public void setTerminate(String terminate) {
        this.terminate = terminate;
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

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Timestamp getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

}
