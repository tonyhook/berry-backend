package cc.tonyhook.berry.backend.controller.open;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.ManagedResource;
import cc.tonyhook.berry.backend.entity.visitor.Agreement;
import cc.tonyhook.berry.backend.entity.visitor.ProfileLog;
import cc.tonyhook.berry.backend.entity.visitor.SearchLog;
import cc.tonyhook.berry.backend.entity.visitor.Visitor;
import cc.tonyhook.berry.backend.entity.wechat.WechatUser;
import cc.tonyhook.berry.backend.service.visitor.AgreementService;
import cc.tonyhook.berry.backend.service.visitor.ProfileLogService;
import cc.tonyhook.berry.backend.service.visitor.SearchLogService;
import cc.tonyhook.berry.backend.service.visitor.VisitorService;
import cc.tonyhook.berry.backend.service.wechat.WechatUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class OpenVisitorController {

    @Value("${app.sms.verify-interval}")
    private Integer interval;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private AgreementService agreementService;
    @Autowired
    private ProfileLogService profileLogService;
    @Autowired
    private SearchLogService searchLogService;
    @Autowired
    private VisitorService visitorService;
    @Autowired
    private WechatUserService wechatUserService;

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

    @RequestMapping(value = "/api/open/visitor/{openid}/profilelog", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Page<ProfileLog>> getProfileLogList(
            @PathVariable String openid,
            @RequestParam(defaultValue = "") String resourceType,
            @RequestParam(required = false) Integer resourceId,
            @RequestParam(required = false) Integer action,
            @RequestParam(required = false) String value,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        Page<ProfileLog> profileLogPage = profileLogService.getProfileLogList(openid, resourceType, resourceId, action, value, pageable);

        return ResponseEntity.ok().body(profileLogPage);
    }

    @RequestMapping(value = "/api/open/visitor/{openid}/profilelog", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> mergeProfileLog(
            @PathVariable String openid,
            @RequestBody ProfileLog profileLog) {
        WechatUser wechatUser = wechatUserService.getWechatUser(openid);
        if (wechatUser == null) {
            return ResponseEntity.notFound().build();
        }

        if (profileLog.getResourceType() != null && !applicationContext.containsBean(profileLog.getResourceType() + "Repository")) {
            return ResponseEntity.badRequest().build();
        }
        if (profileLog.getOpenid() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!profileLog.getOpenid().equals(openid)) {
            return ResponseEntity.badRequest().build();
        }

        profileLogService.mergeProfileLog(profileLog);

        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/api/open/visitor/{openid}/search", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public synchronized ResponseEntity<Page<ManagedResource>> search(
            @PathVariable String openid,
            @RequestParam(defaultValue = "") String resourceType,
            @RequestParam(defaultValue = "") String keywords,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);

        Object rawRepository = applicationContext.getBean(resourceType + "Repository");
        ListCrudRepository<? extends ManagedResource, Integer> repository = (ListCrudRepository<? extends ManagedResource, Integer>) rawRepository;

        List<? extends ManagedResource> resourceList = repository.findAll();
        Set<Integer> resourceIdSet = new HashSet<Integer>();
        List<ManagedResource> matchedResourceList = new ArrayList<ManagedResource>();

        for (ManagedResource resource : resourceList) {
            for (String keyword : keywords.split(" ")) {
                if (resource.getName().toUpperCase().indexOf(keyword.toUpperCase()) >= 0 && !resourceIdSet.contains(resource.getId())) {
                    resourceIdSet.add(resource.getId());
                    matchedResourceList.add(resource);
                    break;
                }
            }
        }

        matchedResourceList.sort((ManagedResource r1, ManagedResource r2) -> r2.getUpdateTime().compareTo(r1.getUpdateTime()));
        Page<ManagedResource> resourcePage = new PageImpl<ManagedResource>(matchedResourceList, pageable, matchedResourceList.size());

        SearchLog searchLog = searchLogService.getSearchLog(openid, resourceType, keywords);
        if (searchLog == null) {
            searchLog = new SearchLog();
            searchLog.setOpenid(openid);
            searchLog.setResourceType(resourceType);
            searchLog.setKeywords(keywords);
            searchLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
            searchLog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        } else {
            searchLog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        }
        searchLogService.addSearchLog(searchLog);

        return ResponseEntity.ok().body(resourcePage);
    }

    @RequestMapping(value = "/api/open/visitor/{openid}/searchlog", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Page<String>> getSearchKeywordsList(
            @PathVariable String openid,
            @RequestParam(defaultValue = "") String resourceType,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<String> searchKeywordsPage = searchLogService.getSearchKeywordsList(openid, resourceType, pageable);

        return ResponseEntity.ok().body(searchKeywordsPage);
    }

    @RequestMapping(value = "/api/open/visitor/{openid}/searchlog", method = RequestMethod.DELETE, produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> clearSearchLog(
            @PathVariable String openid,
            @RequestParam(defaultValue = "") String resourceType) {
        searchLogService.clearSearchLog(openid, resourceType);

        return ResponseEntity.ok().build();
    }

}
