package cc.tonyhook.berry.backend.service.scheduled;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cc.tonyhook.berry.backend.entity.sms.SmsInboundLog;
import cc.tonyhook.berry.backend.service.sms.SmsService;

@Component
public class SmsInboundMonitorService {

    @Autowired
    private SmsService smsService;

    @Value("${app.sms.inbound-monitor:false}")
    private boolean monitor;

    @Scheduled(cron = "0 * * * * ?")
    public void getSmsInbound() {
        if (monitor) {
            List<SmsInboundLog> inboundList = smsService.receive();

            smsService.notifyInboundListener(inboundList);
        }
    }

}
