package cc.tonyhook.berry.backend.service.sms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.sms.SmsInboundLogRepository;
import cc.tonyhook.berry.backend.dao.sms.SmsOutboundLogRepository;
import cc.tonyhook.berry.backend.entity.sms.SmsInboundLog;
import cc.tonyhook.berry.backend.entity.sms.SmsOutboundLog;
import cc.tonyhook.berry.backend.entity.sms.SmsReport;

@Service
public class SmsService {

    @Autowired
    private SmsProvider sms;

    @Autowired
    private SmsInboundLogRepository smsInboundLogRepository;
    @Autowired
    private SmsOutboundLogRepository smsOutboundLogRepository;

    private List<SmsInboundListener> inboundListenerList = new ArrayList<SmsInboundListener>();

    public Boolean registerInboundListener(SmsInboundListener inboundListener) {
        if (inboundListenerList.contains(inboundListener)) {
            return false;
        }
        if (inboundListenerList == null) {
            return false;
        }

        inboundListenerList.add(inboundListener);

        return true;
    }

    public SmsOutboundLog send(String signature, String templateName, Map<String, Object> templateParam, String extend, String phone) {
        SmsOutboundLog outbound = sms.send(signature, templateName, templateParam, extend, phone);

        if (outbound != null) {
            SmsOutboundLog updatedOutbound = smsOutboundLogRepository.save(outbound);

            return updatedOutbound;
        } else {
            return null;
        }
    }

    public List<SmsInboundLog> receive() {
        List<SmsInboundLog> inboundList = sms.receive();
        List<SmsInboundLog> updatedInboundList = new ArrayList<SmsInboundLog>();

        for (SmsInboundLog inbound : inboundList) {
            SmsInboundLog updatedInbound = smsInboundLogRepository.save(inbound);
            updatedInboundList.add(updatedInbound);
        }

        return updatedInboundList;
    }

    public SmsReport report(SmsOutboundLog outbound) {
        return sms.report(outbound);
    }

    public void notifyInboundListener(List<SmsInboundLog> inboundList) {
        inboundListenerList.forEach(inboundListener -> {
            inboundList.forEach(inbound -> {
                inboundListener.process(inbound);
            });
        });
    }

}
