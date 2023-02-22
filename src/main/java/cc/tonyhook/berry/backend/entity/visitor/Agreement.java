package cc.tonyhook.berry.backend.entity.visitor;

import java.sql.Timestamp;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "visitor_agreement", indexes = {
    @Index(columnList = "name"),
    @Index(columnList = "version"),
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = { "name", "version" })
})
public class Agreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Timestamp createTime;

    private String name;

    private Integer version;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] text;

    @ManyToMany(mappedBy = "agreements")
    @JsonIgnore
    private Set<Visitor> visitors;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public byte[] getText() {
        return this.text;
    }

    public void setText(byte[] text) {
        this.text = text;
    }

}
