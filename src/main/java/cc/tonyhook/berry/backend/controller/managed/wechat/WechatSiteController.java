package cc.tonyhook.berry.backend.controller.managed.wechat;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.wechat.WechatSite;
import cc.tonyhook.berry.backend.service.wechat.WechatSiteService;

@RestController
public class WechatSiteController {

    @Autowired
    private WechatSiteService wechatSiteService;

    @RequestMapping(value = "/api/managed/wechatsite", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Page<WechatSite>> getWechatSiteList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        Page<WechatSite> wechatSitePage = wechatSiteService.getWechatSiteList(pageable);

        return ResponseEntity.ok().body(wechatSitePage);
    }

    @RequestMapping(value = "/api/managed/wechatsite/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<WechatSite> getWechatSite(
            @PathVariable Integer id) {
        WechatSite wechatSite = wechatSiteService.getWechatSite(id);

        if (wechatSite != null) {
            return ResponseEntity.ok().body(wechatSite);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/wechatsite", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<WechatSite> addWechatSite(
            @RequestBody WechatSite newWechatSite) throws URISyntaxException {
        WechatSite updatedWechatSite = wechatSiteService.addWechatSite(newWechatSite);

        return ResponseEntity
                .created(new URI("/api/managed/wechatsite/" + updatedWechatSite.getId()))
                .body(updatedWechatSite);
    }

    @RequestMapping(value = "/api/managed/wechatsite/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateWechatSite(
            @PathVariable Integer id,
            @RequestBody WechatSite newWechatSite) {
        if (!id.equals(newWechatSite.getId())) {
            return ResponseEntity.badRequest().build();
        }

        WechatSite targetWechatSite = wechatSiteService.getWechatSite(id);
        if (targetWechatSite == null) {
            return ResponseEntity.notFound().build();
        }

        wechatSiteService.updateWechatSite(id, newWechatSite);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/wechatsite/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeWechatSite(
            @PathVariable Integer id) {
        WechatSite deletedWechatSite = wechatSiteService.getWechatSite(id);
        if (deletedWechatSite == null) {
            return ResponseEntity.notFound().build();
        }

        wechatSiteService.removeWechatSite(id);

        return ResponseEntity.ok().build();
    }

}
