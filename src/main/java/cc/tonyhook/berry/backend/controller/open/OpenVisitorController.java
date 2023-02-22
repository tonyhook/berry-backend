package cc.tonyhook.berry.backend.controller.open;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.visitor.Agreement;
import cc.tonyhook.berry.backend.entity.visitor.Visitor;
import cc.tonyhook.berry.backend.service.visitor.AgreementService;
import cc.tonyhook.berry.backend.service.visitor.VisitorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class OpenVisitorController {

    @Value("${app.sms.verify-interval}")
    private Integer interval;

    @Autowired
    private AgreementService agreementService;
    @Autowired
    private VisitorService visitorService;

    @RequestMapping(value = "/api/open/visitor", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> register(
            HttpServletRequest request,
            @RequestParam(defaultValue = "") String verify,
            @RequestBody Visitor newVisitor) throws URISyntaxException {
        HttpSession session = request.getSession();

        Long timestamp = 0L;
        String code = "";
        String phone = "";
        if (session.getAttribute("sms-verify-timestamp") != null) {
            timestamp = (Long) session.getAttribute("sms-verify-timestamp");
        }
        if (session.getAttribute("sms-verify-code") != null) {
            code = session.getAttribute("sms-verify-code").toString();
        }
        if (session.getAttribute("sms-verify-phone") != null) {
            phone = session.getAttribute("sms-verify-phone").toString();
        }

        if (System.currentTimeMillis() - timestamp.longValue() > interval * 1000 * 5) {
            System.out.println(newVisitor.getPhone() + ": verify code is not matched (timeout)");
            return ResponseEntity.badRequest().body("{\"error\": \"BADVERIFY\"}");
        }
        if (!phone.equals(newVisitor.getPhone())) {
            session.removeAttribute("sms-verify-timestamp");
            session.removeAttribute("sms-verify-code");
            session.removeAttribute("sms-verify-phone");
            System.out.println(newVisitor.getPhone() + ": verify code is not matched (phone)");
            return ResponseEntity.badRequest().body("{\"error\": \"BADVERIFY\"}");
        }
        if (!code.equals(verify)) {
            session.removeAttribute("sms-verify-timestamp");
            session.removeAttribute("sms-verify-code");
            session.removeAttribute("sms-verify-phone");
            System.out.println(newVisitor.getPhone() + ": verify code is not matched (code)");
            return ResponseEntity.badRequest().body("{\"error\": \"BADVERIFY\"}");
        }

        session.removeAttribute("sms-verify-timestamp");
        session.removeAttribute("sms-verify-code");
        session.removeAttribute("sms-verify-phone");

        if (newVisitor.getOpenid() == null) {
            System.out.println(newVisitor.getPhone() + ": openid is null");
            return ResponseEntity.badRequest().body("{\"error\": \"NULLOPENID\"}");
        }

        Visitor visitor = visitorService.getVisitor(newVisitor.getOpenid());
        if (visitor != null) {
            System.out.println(newVisitor.getPhone() + ": openid is duplicated");
            return ResponseEntity.badRequest().body("{\"error\": \"DUPLICATED\"}");
        }

        newVisitor.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newVisitor.setEnabled(true);

        Visitor updatedVisitor = visitorService.addVisitor(newVisitor);

        return ResponseEntity
                .created(new URI("/api/open/visitor/" + updatedVisitor.getOpenid()))
                .body(updatedVisitor);
    }

    @RequestMapping(value = "/api/open/visitor/{openid}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Visitor> login(
            @PathVariable String openid) {
        Visitor visitor = visitorService.getVisitor(openid);

        if (visitor != null) {
            Boolean enabled = visitor.isEnabled();

            if (enabled != null && enabled) {
                return ResponseEntity.ok().body(visitor);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @RequestMapping(value = "/api/open/visitor/{openid}/agreement/{name}/version/{version}", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> signAgreement(
            @PathVariable String openid,
            @PathVariable String name,
            @PathVariable Integer version) throws URISyntaxException {
        Visitor visitor = visitorService.getVisitor(openid);
        if (visitor == null) {
            return ResponseEntity.badRequest().build();
        }

        Agreement agreement = agreementService.getAgreement(name, version);
        if (agreement == null) {
            return ResponseEntity.badRequest().build();
        }

        if (visitor.getAgreements() == null) {
            Set<Agreement> newAgreementSet = new HashSet<Agreement>();
            newAgreementSet.add(agreement);
            visitor.setAgreements(newAgreementSet);
        } else {
            Boolean updated = false;
            Set<Agreement> newAgreementSet = new HashSet<Agreement>();

            for (Agreement oldAgreement : visitor.getAgreements()) {
                if (oldAgreement.getName().equals(agreement.getName())) {
                    newAgreementSet.add(agreement);
                    updated = true;
                } else {
                    newAgreementSet.add(oldAgreement);
                }
            }

            if (!updated) {
                newAgreementSet.add(agreement);
            }

            visitor.setAgreements(newAgreementSet);
        }
        visitorService.updateVisitor(visitor.getOpenid(), visitor);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/open/visitor/agreement/{name}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> getAgreement(
            @PathVariable String name) {
        Agreement agreement = agreementService.getLatestAgreement(name);

        if (agreement != null) {
            return ResponseEntity.ok().body(agreement);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
