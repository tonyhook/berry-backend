package cc.tonyhook.berry.backend.entity.wechat;

public class WechatTemplateMessageData {

    private String key;

    private String value;

    private String color;

    public WechatTemplateMessageData(String key, String value, String color) {
        this.key = key;
        this.value = value;
        this.color = color;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String color) {
        this.color = color;
    }

}
