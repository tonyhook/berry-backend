package cc.tonyhook.berry.backend.service.sms;

import java.util.List;
import java.util.Map;

import cc.tonyhook.berry.backend.entity.sms.SmsInboundLog;
import cc.tonyhook.berry.backend.entity.sms.SmsOutboundLog;
import cc.tonyhook.berry.backend.entity.sms.SmsReport;

public interface SmsProvider {

    public SmsOutboundLog send(String signature, String templateName, Map<String, Object> templateParam, String extend, String phone);
    public List<SmsInboundLog> receive();
    public SmsReport report(SmsOutboundLog outbound);

}
