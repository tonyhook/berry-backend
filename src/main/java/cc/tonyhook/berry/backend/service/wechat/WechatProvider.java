package cc.tonyhook.berry.backend.service.wechat;

public interface WechatProvider {

    public String getWechatAppid();
    public String getWechatSecret();
    public String getWechatAccessToken(Boolean forceUpdate);
    public String getWechatConfig(String pageUrl);
    public String getWechatMessageToken();

}
