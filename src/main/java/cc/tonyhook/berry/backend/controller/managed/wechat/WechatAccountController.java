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

import cc.tonyhook.berry.backend.entity.wechat.WechatAccount;
import cc.tonyhook.berry.backend.service.wechat.WechatAccountService;

@RestController
public class WechatAccountController {

    @Autowired
    private WechatAccountService wechatAccountService;

    @RequestMapping(value = "/api/managed/wechataccount", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Page<WechatAccount>> getWechatAccountList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        Page<WechatAccount> wechatAccountPage = wechatAccountService.getWechatAccountList(pageable);

        return ResponseEntity.ok().body(wechatAccountPage);
    }

    @RequestMapping(value = "/api/managed/wechataccount/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<WechatAccount> getWechatAccount(
            @PathVariable Integer id) {
        WechatAccount wechatAccount = wechatAccountService.getWechatAccount(id);

        if (wechatAccount != null) {
            return ResponseEntity.ok().body(wechatAccount);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/wechataccount", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<WechatAccount> addWechatAccount(
            @RequestBody WechatAccount newWechatAccount) throws URISyntaxException {
        WechatAccount updatedWechatAccount = wechatAccountService.addWechatAccount(newWechatAccount);

        return ResponseEntity
                .created(new URI("/api/managed/wechataccount/" + updatedWechatAccount.getId()))
                .body(updatedWechatAccount);
    }

    @RequestMapping(value = "/api/managed/wechataccount/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateWechatAccount(
            @PathVariable Integer id,
            @RequestBody WechatAccount newWechatAccount) {
        if (!id.equals(newWechatAccount.getId())) {
            return ResponseEntity.badRequest().build();
        }

        WechatAccount targetWechatAccount = wechatAccountService.getWechatAccount(id);
        if (targetWechatAccount == null) {
            return ResponseEntity.notFound().build();
        }

        wechatAccountService.updateWechatAccount(id, newWechatAccount);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/wechataccount/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeWechatAccount(
            @PathVariable Integer id) {
        WechatAccount deletedWechatAccount = wechatAccountService.getWechatAccount(id);
        if (deletedWechatAccount == null) {
            return ResponseEntity.notFound().build();
        }

        wechatAccountService.removeWechatAccount(id);

        return ResponseEntity.ok().build();
    }

}
