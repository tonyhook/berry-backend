package cc.tonyhook.berry.backend.service.wechat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import cc.tonyhook.berry.backend.dao.wechat.WechatAccountRepository;
import cc.tonyhook.berry.backend.entity.wechat.WechatAccount;
import cc.tonyhook.berry.backend.entity.wechat.WechatTemplateMessageData;
import cc.tonyhook.berry.backend.entity.wechat.WechatUser;
import cc.tonyhook.berry.backend.service.shared.ParameterStringBuilder;

@Service
public class WechatService {

    @Autowired
    private WechatProvider wechat;

    @Autowired
    private WechatAccountRepository wechatAccountRepository;

    private Map<Integer, String> wechatTagMap = null;

    public String getWechatSecret(String appid) {
        WechatAccount wechatAccount = wechatAccountRepository.findByAppid(appid);

        if (wechatAccount != null) {
            return wechatAccount.getSecret();
        } else {
            return null;
        }
    }

    public String getWechatMessageToken(String appid) {
        WechatAccount wechatAccount = wechatAccountRepository.findByAppid(appid);

        if (wechatAccount != null) {
            return wechatAccount.getMessageToken();
        } else {
            return null;
        }
    }

    public String getWechatAccessToken(String appid, Boolean forceUpdate) {
        return wechat.getWechatAccessToken(appid, getWechatSecret(appid), forceUpdate);
    }

    public String getWechatConfig(String appid, String pageUrl) {
        return wechat.getWechatConfig(appid, getWechatSecret(appid), pageUrl);
    }

    public WechatUser authWechatUserOAuth(String appid, String code) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("appid", appid);
        params.put("secret", getWechatSecret(appid));
        params.put("code", code);
        params.put("grant_type", "authorization_code");

        WechatUser wechatUser = new WechatUser();
        String access_token = null;

        try {
            URL url = new URL("https://api.weixin.qq.com/sns/oauth2/access_token?"
                + ParameterStringBuilder.getParamsString(params));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode token = mapper.readTree(content.toString());
            if (token.get("openid") != null) {
                wechatUser.setOpenid(token.get("openid").asText());
                wechatUser.setAppid(appid);
                wechatUser.setScope(token.get("scope").asText());
                access_token = token.get("access_token").asText();

                if (wechatUser.getScope().equals("snsapi_userinfo")) {
                    params = new TreeMap<>();
                    params.put("access_token", access_token);
                    params.put("openid", wechatUser.getOpenid());
                    params.put("lang", "zh_CN");

                    url = new URL("https://api.weixin.qq.com/sns/userinfo?"
                        + ParameterStringBuilder.getParamsString(params));
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");

                    in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    con.disconnect();

                    token = mapper.readTree(content.toString());
                    if (token.get("openid") != null) {
                        if (token.has("headimgurl")) {
                            wechatUser.setAvatar(token.get("headimgurl").asText());
                        }
                        if (token.has("sex")) {
                            wechatUser.setGender(token.get("sex").asInt());
                        }
                        if (token.has("nickname")) {
                            wechatUser.setNickname(token.get("nickname").asText());
                        }
                        if (token.has("unionid")) {
                            wechatUser.setUnionid(token.get("unionid").asText());
                        }
                    }
                }
            } else {
                System.out.println("authWechatUser: " + code + " " + token.get("errcode").asInt(-1));

                return null;
            }

            return wechatUser;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public WechatUser authWechatUserJsCode(String appid, String code) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("appid", appid);
        params.put("secret", getWechatSecret(appid));
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");

        WechatUser wechatUser = new WechatUser();

        try {
            URL url = new URL("https://api.weixin.qq.com/sns/jscode2session?"
                + ParameterStringBuilder.getParamsString(params));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode token = mapper.readTree(content.toString());
            if (token.get("openid") != null) {
                wechatUser.setOpenid(token.get("openid").asText());
                wechatUser.setAppid(appid);
                if (token.get("unionid") != null) {
                    wechatUser.setUnionid(token.get("unionid").asText());
                }
            } else {
                System.out.println("authWechatUser: " + code + " " + token.get("errcode").asInt(-1));

                return null;
            }

            return wechatUser;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String doWechatRequestOnce(String method, String requestURL, String request) {
        try {
            URL url = new URL(requestURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);

            if (method.equals("POST")) {
                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();
                byte[] output = request.getBytes("UTF-8");
                os.write(output, 0, output.length);
            }

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();

                return content.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private JsonNode doWechatRequest(String appid, String method, String requestURL, String request) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            Integer retry = 0;
            String accessToken = wechat.getWechatAccessToken(appid, getWechatSecret(appid), false);

            while (retry < 3) {
                String result = doWechatRequestOnce(method, requestURL + accessToken, request);

                if (result != null) {
                    JsonNode resultNode = mapper.readTree(result);

                    if (resultNode.get("errcode") != null) {
                        if (resultNode.get("errcode").asInt(-1) == 40001) {
                            accessToken = wechat.getWechatAccessToken(appid, getWechatSecret(appid), true);
                        } else {
                            return resultNode;
                        }
                    } else {
                        return resultNode;
                    }
                }

                retry++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return JsonNodeFactory.instance.objectNode();
    }

    private JsonNode doWechatRequestMap(String appid, String method, String requestURL, Map<String, Object> requestMap) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            return doWechatRequest(appid, method, requestURL, mapper.writeValueAsString(requestMap));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return JsonNodeFactory.instance.objectNode();
    }

    public Boolean addMultiWechatUserTag(String appid, List<String> openidList, String tag) {
        Integer tagId = getWechatTag(appid, tag);
        if (tagId < 0) {
            tagId = createWechatTag(appid, tag);
        }

        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("openid_list", openidList);
        requestMap.put("tagid", tagId);

        JsonNode response = doWechatRequestMap(appid, "POST",
                "https://api.weixin.qq.com/cgi-bin/tags/members/batchtagging?access_token=",
                requestMap);

        if (response.get("errcode") != null) {
            if (response.get("errcode").asInt(-1) != 0) {
                System.out.println("addMultiWechatUserTag: " + tag + " " + response.get("errcode").asInt(-1));
            }

            return response.get("errcode").asInt(-1) == 0;
        } else {
            return false;
        }
    }

    public Boolean deleteMultiWechatUserTag(String appid, List<String> openidList, String tag) {
        Integer tagId = getWechatTag(appid, tag);
        if (tagId < 0) {
            return true;
        }

        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("openid_list", openidList);
        requestMap.put("tagid", tagId);

        JsonNode response = doWechatRequestMap(appid, "POST",
                "https://api.weixin.qq.com/cgi-bin/tags/members/batchuntagging?access_token=",
                requestMap);

        if (response.get("errcode") != null) {
            if (response.get("errcode").asInt(-1) != 0) {
                System.out.println("deleteMultiWechatUserTag: " + tag + " " + response.get("errcode").asInt(-1));
            }

            return response.get("errcode").asInt(-1) == 0;
        } else {
            return false;
        }
    }

    public Boolean addWechatUserTag(String appid, String openid, String tag) {
        Integer tagId = getWechatTag(appid, tag);
        if (tagId < 0) {
            tagId = createWechatTag(appid, tag);
        }

        List<String> tagList = getWechatUserTagList(appid, openid);
        if ((tagList != null) && tagList.contains(tag)) {
            return true;
        }

        List<String> openidList = new ArrayList<String>();
        openidList.add(openid);

        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("openid_list", openidList);
        requestMap.put("tagid", tagId);

        JsonNode response = doWechatRequestMap(appid, "POST",
                "https://api.weixin.qq.com/cgi-bin/tags/members/batchtagging?access_token=",
                requestMap);

        if (response.get("errcode") != null) {
            if (response.get("errcode").asInt(-1) != 0) {
                System.out.println("addWechatUserTag: " + openid + " " + tag + " " + response.get("errcode").asInt(-1));
            }

            return response.get("errcode").asInt(-1) == 0;
        } else {
            return false;
        }
    }

    public Boolean deleteWechatUserTag(String appid, String openid, String tag) {
        Integer tagId = getWechatTag(appid, tag);
        if (tagId < 0) {
            return true;
        }

        List<String> openidList = new ArrayList<String>();
        openidList.add(openid);

        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("openid_list", openidList);
        requestMap.put("tagid", tagId);

        JsonNode response = doWechatRequestMap(appid, "POST",
                "https://api.weixin.qq.com/cgi-bin/tags/members/batchuntagging?access_token=",
                requestMap);

        if (response.get("errcode") != null) {
            if (response.get("errcode").asInt(-1) != 0) {
                System.out.println("deleteWechatUserTag: " + openid + " " + tag + " " + response.get("errcode").asInt(-1));
            }

            return response.get("errcode").asInt(-1) == 0;
        } else {
            return false;
        }
    }

    public WechatUser getWechatUserInfo(String appid, String openid) {
        String url = "https://api.weixin.qq.com/cgi-bin/user/info?";
        if (openid != null) {
            url = url + "openid=" + openid + "&&lang=zh_CN&";
        }
        url = url + "access_token=";

        JsonNode response = doWechatRequest(appid, "GET", url, null);

        if (response.get("errcode") != null) {
            if (response.get("errcode").asInt(-1) != 0) {
                System.out.println("getWechatUserInfo: " + response.get("errcode").asInt(-1));
            }

            return null;
        } else {
            WechatUser wechatUser = new WechatUser();
            wechatUser.setOpenid(response.get("openid").asText());
            wechatUser.setAppid(appid);

            if (response.get("subscribe").asInt(-1) == 1) {
                wechatUser.setAvatar(response.get("headimgurl").asText());
                wechatUser.setGender(response.get("sex").asInt());
                wechatUser.setNickname(response.get("nickname").asText());
                if (response.has("unionid")) {
                    wechatUser.setUnionid(response.get("unionid").asText());
                }
                wechatUser.setSubscribed(true);
            } else {
                wechatUser.setSubscribed(false);
            }

            return wechatUser;
        }
    }

    public List<String> getWechatUserTagList(String appid, String openid) {
        Map<Integer, String> tagMap = getWechatTagList(appid);
        if (tagMap == null) {
            return null;
        }

        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("openid", openid);

        JsonNode response = doWechatRequestMap(appid, "POST",
                "https://api.weixin.qq.com/cgi-bin/tags/getidlist?access_token=",
                requestMap);

        if (response.get("tagid_list") != null) {
            ArrayNode tags = (ArrayNode)response.get("tagid_list");
            List<String> tagList = new ArrayList<String>();

            for (int i = 0; i < tags.size(); i++) {
                tagList.add(tagMap.get(tags.get(i).asInt()));
            }

            return tagList;
        } else {
            if (response.get("errcode") != null) {
                if (response.get("errcode").asInt(-1) != 0) {
                    System.out.println("getWechatUserTagList: " + openid + " " + response.get("errcode").asInt(-1));
                }
            }

            return null;
        }
    }

    public Map<Integer, String> getWechatTagList(String appid) {
        if (wechatTagMap != null) {
            return wechatTagMap;
        }

        JsonNode response = doWechatRequest(appid, "GET",
                "https://api.weixin.qq.com/cgi-bin/tags/get?access_token=",
                null);

        if (response.get("tags") != null) {
            ArrayNode tags = (ArrayNode)response.get("tags");
            wechatTagMap = new HashMap<Integer, String>();

            for (int i = 0; i < tags.size(); i++) {
                wechatTagMap.put(tags.get(i).get("id").asInt(), tags.get(i).get("name").asText());
            }

            return wechatTagMap;
        } else {
            if (response.get("errcode") != null) {
                if (response.get("errcode").asInt(-1) != 0) {
                    System.out.println("getWechatTagList: " + response.get("errcode").asInt(-1));
                }
            }

            return null;
        }
    }

    public Map<String, String> getWechatTemplateList(String appid) {
        JsonNode response = doWechatRequest(appid, "GET",
                "https://api.weixin.qq.com/cgi-bin/template/get_all_private_template?access_token=",
                null);

        if (response.get("template_list") != null) {
            ArrayNode templates = (ArrayNode)response.get("template_list");
            Map<String, String> templateMap = new HashMap<String, String>();

            for (int i = 0; i < templates.size(); i++) {
                templateMap.put(templates.get(i).get("template_id").asText(), templates.get(i).get("content").asText());
            }

            return templateMap;
        } else {
            if (response.get("errcode") != null) {
                if (response.get("errcode").asInt(-1) != 0) {
                    System.out.println("getWechatTemplateList: " + response.get("errcode").asInt(-1));
                }
            }

            return null;
        }
    }

    public Boolean sendWechatTemplateMessage(String appid, String openid, String templateId, String url, String pagepath, List<WechatTemplateMessageData> paramData) {
        Map<String, Map<String, Object>> tmplateData = new HashMap<String, Map<String, Object>>();
        for (WechatTemplateMessageData param : paramData) {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("value", param.getValue());
            if (param.getColor() != null) {
                paramMap.put("color", param.getColor());
            }
            tmplateData.put(param.getKey(), paramMap);
        }

        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("touser", openid);
        requestMap.put("template_id", templateId);
        if (url != null) {
            requestMap.put("url", url);
        }
        if (appid != null) {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("appid", appid);
            if (pagepath != null) {
                paramMap.put("pagepath", pagepath);
            }
            requestMap.put("miniprogram", paramMap);
        }
        requestMap.put("data", tmplateData);

        JsonNode response = doWechatRequestMap(appid, "POST",
                "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=",
                requestMap);

        if (response.get("errcode") != null) {
            if (response.get("errcode").asInt(-1) != 0) {
                System.out.println("sendWechatTemplateMessage: " + response.get("errcode").asInt(-1));
                return false;
            }

            return true;
        }

        return false;
    }

    public Map<String, String> getWechatMaterialList(String appid, String type) {
        Integer count = 1;
        Integer retry = 0;
        Integer offset = 0;
        Map<String, String> templateMap = new HashMap<String, String>();

        while ((count > 0) && (retry < 3)) {
            Map<String, Object> requestMap = new HashMap<String, Object>();

            requestMap.put("type", "news");
            requestMap.put("offset", offset);
            requestMap.put("count", 20);

            JsonNode response = doWechatRequestMap(appid, "POST",
                    "https://api.weixin.qq.com/cgi-bin/material/batchget_material?access_token=",
                    requestMap);

            if (response.get("item") != null) {
                count = response.get("item_count").asInt();
                offset += count;

                ArrayNode templates = (ArrayNode)response.get("item");

                for (int i = 0; i < templates.size(); i++) {
                    templateMap.put(templates.get(i).get("media_id").asText(), templates.get(i).get("content").toString());
                }
            } else {
                if (response.get("errcode") != null) {
                    retry++;
                    if (response.get("errcode").asInt(-1) != 0) {
                        System.out.println("getWechatMaterialList: " + response.get("errcode").asInt(-1));
                    }
                }

                continue;
            }
        }

        return templateMap;
    }

    public Boolean sendWechatMaterialByTag(String appid, String mediaId, Integer tagId) {
        Map<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put("is_to_all", false);
        filterMap.put("tag_id", tagId);

        Map<String, Object> mpnewsMap = new HashMap<String, Object>();
        mpnewsMap.put("media_id", mediaId);

        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("filter", filterMap);
        requestMap.put("mpnews", mpnewsMap);
        requestMap.put("msgtype", "mpnews");
        requestMap.put("send_ignore_reprint", 0);

        JsonNode response = doWechatRequestMap(appid, "POST",
                "https://api.weixin.qq.com/cgi-bin/message/mass/sendall?access_token=",
                requestMap);

        if (response.get("errcode") != null) {
            if (response.get("errcode").asInt(-1) != 0) {
                System.out.println("sendWechatMaterialByTag: " + mediaId + " " + tagId + " " + response.get("errcode").asInt(-1));
            }

            return response.get("errcode").asInt(-1) == 0;
        } else {
            return false;
        }
    }

    public Integer getWechatTag(String appid, String tag) {
        Map<Integer, String> tagMap = getWechatTagList(appid);
        if (tagMap == null) {
            return -1;
        }

        Integer tagId = -1;

        Iterator<Integer> iterator = tagMap.keySet().iterator();
        while (iterator.hasNext()) {
            Integer id = iterator.next();
            if (tagMap.get(id).equals(tag)) {
                tagId = id;
            }
        }

        return tagId;
    }

    public Integer createWechatTag(String appid, String tag) {
        Map<String, Object> tagMap = new HashMap<String, Object>();
        tagMap.put("name", tag);
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("tag", tagMap);

        JsonNode response = doWechatRequestMap(appid, "POST",
                "https://api.weixin.qq.com/cgi-bin/tags/create?access_token=",
                requestMap);

        if (response.get("tag") != null) {
            if (response.get("tag").get("id") != null) {
                wechatTagMap.put(response.get("tag").get("id").asInt(0), tag);
                return response.get("tag").get("id").asInt(0);
            } else {
                return -1;
            }
        } else {
            if (response.get("errcode") != null) {
                if (response.get("errcode").asInt(-1) != 0) {
                    System.out.println("createWechatTag: " + tag + " " + response.get("errcode").asInt(-1));
                }
            }

            return -1;
        }
    }

    public Boolean deleteWechatTag(String appid, String tag) {
        Integer tagId = getWechatTag(appid, tag);
        if (tagId < 0) {
            return true;
        }

        Map<String, Object> tagMap = new HashMap<String, Object>();
        tagMap.put("id", tagId);
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("tag", tagMap);

        JsonNode response = doWechatRequestMap(appid, "POST",
                "https://api.weixin.qq.com/cgi-bin/tags/delete?access_token=",
                requestMap);

        if (response.get("errcode") != null) {
            if (response.get("errcode").asInt(-1) != 0) {
                System.out.println("deleteWechatTag: " + tag + " " + response.get("errcode").asInt(-1));
            }

            if (response.get("errcode").asInt(-1) == 0) {
                wechatTagMap.remove(tagId);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public List<String> getWechatTagUserList(String appid, String tag) {
        List<String> result = new ArrayList<String>();
        String openid = null;
        Integer count = 1;
        Integer retry = 0;

        while ((count > 0) && (retry < 3)) {

            Map<String, Object> requestMap = new HashMap<String, Object>();
            requestMap.put("tagid", getWechatTag(appid, tag));
            if (openid != null) {
                requestMap.put("next_openid", openid);
            }
            JsonNode response = doWechatRequestMap(appid, "POST",
                    "https://api.weixin.qq.com/cgi-bin/user/tag/get?access_token=",
                    requestMap);

            if (response.get("errcode") != null) {
                retry++;
                if (response.get("errcode").asInt(-1) != 0) {
                    System.out.println("getWechatTagUserList: " + response.get("errcode").asInt(-1));
                }

                continue;
            } else {
                count = response.get("count").asInt();
                if (count > 0) {
                    JsonNode arrNode = response.get("data").get("openid");
                    if (arrNode.isArray()) {
                        for (JsonNode objNode : arrNode) {
                            result.add(objNode.asText());
                        }
                    }
                    openid = response.get("next_openid").asText();
                }
            }
        }

        return result;
    }

    public List<String> getWechatUserList(String appid) {
        List<String> result = new ArrayList<String>();
        String openid = null;
        Integer count = 1;
        Integer retry = 0;

        while ((count > 0) && (retry < 3)) {
            String url = "https://api.weixin.qq.com/cgi-bin/user/get?";
            if (openid != null) {
                url = url + "next_openid=" + openid + "&";
            }
            url = url + "access_token=";

            JsonNode response = doWechatRequest(appid, "GET", url, null);

            if (response.get("errcode") != null) {
                retry++;
                if (response.get("errcode").asInt(-1) != 0) {
                    System.out.println("getWechatUserList: " + response.get("errcode").asInt(-1));
                }
                continue;
            } else {
                count = response.get("count").asInt();
                if (count > 0) {
                    JsonNode arrNode = response.get("data").get("openid");
                    if (arrNode.isArray()) {
                        for (JsonNode objNode : arrNode) {
                            result.add(objNode.asText());
                        }
                    }
                    openid = response.get("next_openid").asText();
                }
            }
        }

        return result;
    }

    public String getMenu(String appid) {
        JsonNode response = doWechatRequest(appid, "GET",
                "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=",
                null);

        if (response.get("errcode") != null) {
            if (response.get("errcode").asInt(-1) != 0) {
                System.out.println("getMenu: " + response.get("errcode").asInt(-1));
            }
        }

        return response.toString();
    }

    public Boolean setMenu(String appid, String menu) {
        JsonNode response = doWechatRequest(appid, "POST",
                "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=",
                menu);

        if (response.get("errcode") != null) {
            if (response.get("errcode").asInt(-1) != 0) {
                System.out.println("setMenu: " + response.get("errcode").asInt(-1));
            }

            return response.get("errcode").asInt(-1) == 0;
        } else {
            return false;
        }
    }

    public Boolean addAdditionalMenu(String appid, String menu) {
        JsonNode response = doWechatRequest(appid, "POST",
                "https://api.weixin.qq.com/cgi-bin/menu/addconditional?access_token=",
                menu);

        if (response.get("errcode") != null) {
            if (response.get("errcode").asInt(-1) != 0) {
                System.out.println("setAdditionalMenu: " + response.get("errcode").asInt(-1));
            }

            return response.get("errcode").asInt(-1) == 0;
        } else {
            return false;
        }
    }

    public Boolean deleteAdditionalMenu(String appid, Integer id) {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("menuid", id);

        JsonNode response = doWechatRequestMap(appid, "POST",
                "https://api.weixin.qq.com/cgi-bin/menu/delconditional?access_token=",
                requestMap);

        if (response.get("errcode") != null) {
            if (response.get("errcode").asInt(-1) != 0) {
                System.out.println("deleteAdditionalMenu: " + response.get("errcode").asInt(-1));
            }

            return response.get("errcode").asInt(-1) == 0;
        } else {
            return false;
        }
    }

    public Boolean clearQuota(String appid) {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("appid", appid);

        JsonNode response = doWechatRequestMap(appid, "POST",
                "https://api.weixin.qq.com/cgi-bin/clear_quota?access_token=",
                requestMap);

        if (response.get("errcode") != null) {
            if (response.get("errcode").asInt(-1) != 0) {
                System.out.println("clearQuota: " + response.get("errcode").asInt(-1));
            }

            return response.get("errcode").asInt(-1) == 0;
        } else {
            return false;
        }
    }

}
