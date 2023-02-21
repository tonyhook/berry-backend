package cc.tonyhook.berry.backend.service.sms;

import cc.tonyhook.berry.backend.entity.sms.SmsInboundLog;

public interface SmsInboundListener {

    public void process(SmsInboundLog inbound);

}
