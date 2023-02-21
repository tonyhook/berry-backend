package cc.tonyhook.berry.backend.service.wechat;

import cc.tonyhook.berry.backend.entity.wechat.WechatMessage;

public interface WechatMessageListener {

    public void process(WechatMessage wechatMessage);

}
