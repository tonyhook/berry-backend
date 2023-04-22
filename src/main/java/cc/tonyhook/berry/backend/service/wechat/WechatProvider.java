package cc.tonyhook.berry.backend.service.wechat;

public interface WechatProvider {

    public String getWechatAccessToken(String appid, String secret, Boolean forceUpdate);
    public String getWechatConfig(String appid, String secret, String pageUrl);

}
