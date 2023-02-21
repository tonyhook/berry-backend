package cc.tonyhook.berry.backend.controller.open;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.sms.SmsOutboundLog;
import cc.tonyhook.berry.backend.service.sms.SmsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class OpenSecurityController {

    @Autowired
    private SmsService smsService;

    @Value("${app.sms.verify-interval}")
    private Integer interval;
    @Value("${app.sms.verify-signature}")
    private String signature;
    @Value("${app.sms.verify-template}")
    private String template;

    @RequestMapping(value = "/api/open/security/user-details", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public UserDetails getUserDetails() {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return userDetails;
        } catch (Exception e) {
            return null;
        }
    }

    @RequestMapping(value = "/api/open/security/sms-verify", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<?> getSmsVerify(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") String phone) {
        if (phone.length() != 11) {
            return ResponseEntity.badRequest().body("BADNUMBER");
        }

        try {
            BigInteger phoneNumber = new BigInteger(phone);
            if (phoneNumber.compareTo(new BigInteger("13000000000")) < 0) {
                return ResponseEntity.badRequest().body("BADNUMBER");
            }
            if (phoneNumber.compareTo(new BigInteger("19999999999")) > 0) {
                return ResponseEntity.badRequest().body("BADNUMBER");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("BADNUMBER");
        }

        HttpSession session = request.getSession();
        Long timestamp = (Long) session.getAttribute("sms-verify-timestamp");

        if ((session.getAttribute("sms-verify-code") != null)
            && (session.getAttribute("sms-verify-timestamp") != null)) {
            Long actualInterval = (System.currentTimeMillis() - timestamp.longValue()) / 1000;
            if (actualInterval < interval) {
                return ResponseEntity.badRequest().body("FREQUENTLY" + Long.toString(interval - actualInterval));
            }
        }

        String code = String.format("%06d", Double.valueOf(Math.random() * 1000000).longValue());

        session.setAttribute("sms-verify-timestamp", System.currentTimeMillis());
        session.setAttribute("sms-verify-code", code);
        session.setAttribute("sms-verify-phone", phone);

        Map<String, Object> templateParam = new HashMap<String, Object>();
        templateParam.put("otpcode", code);

        SmsOutboundLog result = smsService.send(signature, template, templateParam, "01", phone);

        if (result != null) {
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.badRequest().body("FAILED");
        }
    }

}
