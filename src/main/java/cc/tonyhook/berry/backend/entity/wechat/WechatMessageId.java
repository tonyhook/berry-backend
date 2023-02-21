package cc.tonyhook.berry.backend.entity.wechat;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Embeddable;

@Embeddable
public class WechatMessageId implements Serializable {

    @JsonProperty(value = "FromUserName")
    private String FromUserName;

    @JsonProperty(value = "CreateTime")
    private String CreateTime;

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof WechatMessageId)) {
            return false;
        }
        WechatMessageId wechatMessageId = (WechatMessageId) o;
        return Objects.equals(FromUserName, wechatMessageId.FromUserName) && Objects.equals(CreateTime, wechatMessageId.CreateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(FromUserName, CreateTime);
    }

    public String getFromUserName() {
        return this.FromUserName;
    }

    public void setFromUserName(String FromUserName) {
        this.FromUserName = FromUserName;
    }

    public String getCreateTime() {
        return this.CreateTime;
    }

    public void setCreateTime(String CreateTime) {
        this.CreateTime = CreateTime;
    }

}
