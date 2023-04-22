package cc.tonyhook.berry.backend.controller.open;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import cc.tonyhook.berry.backend.entity.wechat.WechatAccount;
import cc.tonyhook.berry.backend.entity.wechat.WechatMessage;
import cc.tonyhook.berry.backend.entity.wechat.WechatSite;
import cc.tonyhook.berry.backend.entity.wechat.WechatUser;
import cc.tonyhook.berry.backend.service.shared.HashHelperService;
import cc.tonyhook.berry.backend.service.wechat.WechatAccountService;
import cc.tonyhook.berry.backend.service.wechat.WechatMessageService;
import cc.tonyhook.berry.backend.service.wechat.WechatService;
import cc.tonyhook.berry.backend.service.wechat.WechatSiteService;
import cc.tonyhook.berry.backend.service.wechat.WechatUserService;

@RestController
public class OpenWechatController {

    @Autowired
    private WechatUserService wechatUserService;
    @Autowired
    private WechatMessageService wechatMessageService;
    @Autowired
    private WechatService wechatService;
    @Autowired
    private WechatAccountService wechatAccountService;
    @Autowired
    private WechatSiteService wechatSiteService;

    @RequestMapping(value = "/api/open/wechat/appid", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> getWechatAccountAppid(
            @RequestParam String domain) {
        WechatSite wechatSite = wechatSiteService.getWechatSite(domain);
        if (wechatSite != null) {
            return ResponseEntity.ok().body(wechatSite.getWechatAccount().getAppid());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/wechat/{appid}/authdomain", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> getWechatAccountAuthDomain(
            @PathVariable String appid) {
        WechatAccount wechatAccount = wechatAccountService.getWechatAccountByAppid(appid);
        if (wechatAccount != null) {
            return ResponseEntity.ok().body(wechatAccount.getAuthDomain());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/wechat/{appid}/access_token", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> getAccessToken(
            @PathVariable String appid) {
        return ResponseEntity.ok().body(wechatService.getWechatAccessToken(appid, false));
    }

    @RequestMapping(value = "/api/open/wechat/{appid}/config", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> getConfig(
            @PathVariable String appid,
            @RequestParam String url) {
        return ResponseEntity.ok().body(wechatService.getWechatConfig(appid, url));
    }

    @RequestMapping(value = "/api/open/wechat/{appid}/oauth", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<WechatUser> authWechatUserOAuth(
            @PathVariable String appid,
            @RequestParam String code) {
        WechatUser authenticatedUser = wechatService.authWechatUserOAuth(appid, code);
        Boolean subscribed = false;

        if (authenticatedUser != null) {
            WechatUser wechatUserFromWechat = wechatService.getWechatUserInfo(appid, authenticatedUser.getOpenid());
            if (wechatUserFromWechat != null) {
                subscribed = wechatUserFromWechat.getSubscribed();
            }

            WechatUser wechatUser = wechatUserService.getWechatUser(authenticatedUser.getOpenid());

            if (wechatUser != null) {
                if (authenticatedUser.getAvatar() != null) {
                    wechatUser.setAvatar(authenticatedUser.getAvatar());
                }
                if (authenticatedUser.getGender() != null) {
                    wechatUser.setGender(authenticatedUser.getGender());
                }
                if (authenticatedUser.getNickname() != null) {
                    wechatUser.setNickname(authenticatedUser.getNickname());
                }
                if (authenticatedUser.getUnionid() != null) {
                    wechatUser.setUnionid(authenticatedUser.getUnionid());
                }
                wechatUser.setScope(authenticatedUser.getScope());
                wechatUser.setSubscribed(subscribed);

                wechatUserService.updateWechatUser(authenticatedUser.getOpenid(), wechatUser);
            } else {
                wechatUser = new WechatUser();
                wechatUser.setOpenid(authenticatedUser.getOpenid());
                wechatUser.setAppid(appid);
                wechatUser.setAvatar(authenticatedUser.getAvatar());
                wechatUser.setGender(authenticatedUser.getGender());
                wechatUser.setNickname(authenticatedUser.getNickname());
                wechatUser.setUnionid(authenticatedUser.getUnionid());
                wechatUser.setScope(authenticatedUser.getScope());
                wechatUser.setSubscribed(subscribed);

                wechatUserService.addWechatUser(wechatUser);
            }

            return ResponseEntity.ok().body(wechatUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/wechat/{appid}/jscode", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<WechatUser> authWechatUserJsCode(
            @PathVariable String appid,
            @RequestParam String code) {
        WechatUser authenticatedUser = wechatService.authWechatUserJsCode(appid, code);

        if (authenticatedUser != null) {
            WechatUser wechatUser = wechatUserService.getWechatUser(authenticatedUser.getOpenid());

            if (wechatUser != null) {
                if (authenticatedUser.getUnionid() != null) {
                    wechatUser.setUnionid(authenticatedUser.getUnionid());
                }
                wechatUser.setScope("session");

                wechatUserService.updateWechatUser(authenticatedUser.getOpenid(), wechatUser);
            } else {
                wechatUser = new WechatUser();
                wechatUser.setOpenid(authenticatedUser.getOpenid());
                wechatUser.setAppid(appid);
                wechatUser.setUnionid(authenticatedUser.getUnionid());
                wechatUser.setScope("session");

                wechatUserService.addWechatUser(wechatUser);
            }

            return ResponseEntity.ok().body(wechatUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/wechat/{appid}/message", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<?> verifyMessageProcessor(
            @PathVariable String appid,
            @RequestParam String signature,
            @RequestParam String timestamp,
            @RequestParam String nonce,
            @RequestParam String echostr) {
        List<String> params = new ArrayList<String>();
        params.add(timestamp);
        params.add(nonce);
        params.add(wechatService.getWechatMessageToken(appid));
        Collections.sort(params);
        String code = params.get(0) + params.get(1) + params.get(2);

        String hashtext = HashHelperService.hash(code.getBytes(), "SHA1");

        if (hashtext.equals(signature)) {
            return ResponseEntity.ok().body(echostr);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/api/open/wechat/{appid}/message", method = RequestMethod.POST, consumes = "text/xml; charset=UTF-8")
    public ResponseEntity<?> receiveMessage(
            @PathVariable String appid,
            @RequestBody String message) {
        try {
            XmlMapper mapper = new XmlMapper();
            WechatMessage newWechatMessage = mapper.readValue(message, WechatMessage.class);

            WechatMessage wechatMessage = wechatMessageService.getWechatMessage(newWechatMessage.getId());

            if (wechatMessage == null) {
                wechatMessageService.addWechatMessage(newWechatMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().body("success");
    }

    @RequestMapping(value = "/api/open/wechat/wechatuser/{openid}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<WechatUser> getWechatUser(
            @PathVariable String openid) {
        WechatUser wechatUser = wechatUserService.getWechatUser(openid);

        if (wechatUser != null) {
            return ResponseEntity.ok().body(wechatUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/wechat/wechatuser", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<WechatUser> setWechatUser(
            @RequestBody WechatUser newWechatUser) throws URISyntaxException {
        if (newWechatUser.getOpenid() == null) {
            return ResponseEntity.badRequest().build();
        }

        WechatUser wechatUser = wechatUserService.getWechatUser(newWechatUser.getOpenid());
        if (wechatUser != null) {
            if ((newWechatUser.getAvatar() != null) || (newWechatUser.getGender() != null) || (newWechatUser.getNickname() != null) || (newWechatUser.getUnionid() != null)) {
                wechatUser.setAvatar(newWechatUser.getAvatar());
                wechatUser.setGender(newWechatUser.getGender());
                wechatUser.setNickname(newWechatUser.getNickname());
                wechatUser.setUnionid(newWechatUser.getUnionid());
                wechatUser.setScope("snsapi_userinfo");

                wechatUserService.updateWechatUser(wechatUser.getOpenid(), wechatUser);
            }
        } else {
            if ((newWechatUser.getAvatar() != null) || (newWechatUser.getGender() != null) || (newWechatUser.getNickname() != null) || (newWechatUser.getUnionid() != null)) {
                newWechatUser.setAvatar(newWechatUser.getAvatar());
                newWechatUser.setGender(newWechatUser.getGender());
                newWechatUser.setNickname(newWechatUser.getNickname());
                newWechatUser.setUnionid(newWechatUser.getUnionid());
                newWechatUser.setScope("snsapi_userinfo");
            } else {
                newWechatUser.setScope("snsapi_base");
            }

            wechatUser = wechatUserService.addWechatUser(newWechatUser);
        }

        return ResponseEntity.ok().body(wechatUser);
    }

    @RequestMapping(value = "/api/open/wechat/avatar", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> getAvatar(
            @RequestParam String openid) {
        WechatUser wechatUser = wechatUserService.getWechatUser(openid);
        if (wechatUser != null) {
            // not authorized
            if (wechatUser.getScope().equals("snsapi_base")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // no avatar
            if (wechatUser.getAvatar() == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok().body(wechatUser.getAvatar());
        } else {
            // not authorized
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/wechat/gender", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<Integer> getGender(
            @RequestParam String openid) {
        WechatUser wechatUser = wechatUserService.getWechatUser(openid);
        if (wechatUser != null) {
            // not authorized
            if (wechatUser.getScope().equals("snsapi_base")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // no gender
            if (wechatUser.getGender() == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok().body(wechatUser.getGender());
        } else {
            // not authorized
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/wechat/nickname", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> getNickname(
            @RequestParam String openid) {
        WechatUser wechatUser = wechatUserService.getWechatUser(openid);
        if (wechatUser != null) {
            // not authorized
            if (wechatUser.getScope().equals("snsapi_base")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // no nickname
            if (wechatUser.getNickname() == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok().body(wechatUser.getNickname());
        } else {
            // not authorized
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/open/wechat/unionid", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> getUnionid(
            @RequestParam String openid) {
        WechatUser wechatUser = wechatUserService.getWechatUser(openid);
        if (wechatUser != null) {
            // not authorized
            if (wechatUser.getScope().equals("snsapi_base")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // no unionid
            if (wechatUser.getUnionid() == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok().body(wechatUser.getUnionid());
        } else {
            // not authorized
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/{filename}.txt", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> getVerifyContent(
            @PathVariable String filename) {
        WechatAccount wechatAccount = wechatAccountService.getWechatAccountByVerifyFilename(filename);
        if (wechatAccount != null) {
            return ResponseEntity.ok().body(wechatAccount.getVerifyContent());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
