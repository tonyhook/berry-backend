package cc.tonyhook.berry.backend.entity.wechat;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "wechat_account", indexes = {
    @Index(columnList = "appid"),
    @Index(columnList = "verifyFilename"),
})
public class WechatAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String type;

    private String name;

    private String wechatid;

    private String originalid;

    private String email;

    private String password;

    private String appid;

    private String secret;

    private String messageToken;

    private String verifyFilename;

    private String verifyContent;

    private String authDomain;

    @OneToMany(mappedBy = "wechatAccount")
    @JsonIgnoreProperties({"wechatAccount"})
    private List<WechatSite> wechatSites;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWechatid() {
        return this.wechatid;
    }

    public void setWechatid(String wechatid) {
        this.wechatid = wechatid;
    }

    public String getOriginalid() {
        return this.originalid;
    }

    public void setOriginalid(String originalid) {
        this.originalid = originalid;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAppid() {
        return this.appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSecret() {
        return this.secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getMessageToken() {
        return this.messageToken;
    }

    public void setMessageToken(String messageToken) {
        this.messageToken = messageToken;
    }

    public String getVerifyFilename() {
        return this.verifyFilename;
    }

    public void setVerifyFilename(String verifyFilename) {
        this.verifyFilename = verifyFilename;
    }

    public String getVerifyContent() {
        return this.verifyContent;
    }

    public void setVerifyContent(String verifyContent) {
        this.verifyContent = verifyContent;
    }

    public String getAuthDomain() {
        return this.authDomain;
    }

    public void setAuthDomain(String authDomain) {
        this.authDomain = authDomain;
    }

    public List<WechatSite> getWechatSites() {
        return this.wechatSites;
    }

    public void setWechatSites(List<WechatSite> wechatSites) {
        this.wechatSites = wechatSites;
    }

}
