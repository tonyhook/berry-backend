package cc.tonyhook.berry.backend.controller.open;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.sms.SmsInboundLog;
import cc.tonyhook.berry.backend.entity.sms.SmsMessageAliyun;
import cc.tonyhook.berry.backend.service.sms.SmsProvider;

@RestController
public class OpenSmsAliyunController {

    @Autowired
    private SmsProvider sms;

    class Response {
        public Integer code;
        public String msg;
    }

    @RequestMapping(value = "/api/open/sms/aliyun", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Response> gather(
            @RequestBody List<SmsMessageAliyun> messages) {
        for (SmsMessageAliyun message : messages) {
            SmsInboundLog log = new SmsInboundLog();
            log.setProvider("Aliyun");
            log.setReceiveTime(new Timestamp(System.currentTimeMillis()));
            log.setPhone(message.getPhoneNumber());
            log.setExtend(message.getDestCode());
            log.setMessage(message.getContent());

            sms.gather(log);
        }

        Response response = new Response();
        response.code = 0;
        response.msg = "成功";

        return ResponseEntity.ok().body(response);
    }

}
