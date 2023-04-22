package cc.tonyhook.berry.backend.service.wechat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TreeMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.tonyhook.berry.backend.service.shared.HashHelperService;
import cc.tonyhook.berry.backend.service.shared.ParameterStringBuilder;

@Service
@ConditionalOnProperty(prefix = "app.wechat", name = "provider", havingValue = "official")
public class WechatProviderOfficial implements WechatProvider {

    private String access_token = "";
    private long access_token_expire = 0L;

    private String jsapi_ticket = "";
    private long jsapi_ticket_expire = 0L;

    @Override
    public String getWechatAccessToken(String appid, String secret, Boolean forceUpdate) {
        long now = System.currentTimeMillis();

        if ((now > access_token_expire - 5 * 60 * 1000) || forceUpdate) {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("appid", appid);
            params.put("secret", secret);

            try {
                ObjectMapper mapper = new ObjectMapper();

                URL url = new URL("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&"
                    + ParameterStringBuilder.getParamsString(params));

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    con.disconnect();

                    JsonNode response = mapper.readTree(content.toString());
                    if (response.get("access_token") != null) {
                        access_token = response.get("access_token").asText();
                        access_token_expire = System.currentTimeMillis() + response.get("expires_in").asInt(0) * 1000;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return access_token;
    }

    @Override
    public String getWechatConfig(String appid, String secret, String pageUrl) {
        long now = System.currentTimeMillis();

        if (now > jsapi_ticket_expire - 5 * 60 * 1000) {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("access_token", getWechatAccessToken(appid, secret, false));
            params.put("type", "jsapi");

            try {
                ObjectMapper mapper = new ObjectMapper();

                URL url = new URL("https://api.weixin.qq.com/cgi-bin/ticket/getticket?"
                    + ParameterStringBuilder.getParamsString(params));

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    con.disconnect();

                    JsonNode response = mapper.readTree(content.toString());
                    if (response.get("ticket") != null) {
                        jsapi_ticket = response.get("ticket").asText();
                        jsapi_ticket_expire = System.currentTimeMillis() + response.get("expires_in").asInt(0) * 1000;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String nonceStr = "";
        for (int i = 0; i < 16; i++) {
            int index = (int) (Math.random() * 62);
            if (index < 10) {
                nonceStr += Character.toString((char) index + 48);
            } else if (index < 36) {
                nonceStr += Character.toString((char) index - 10 + 65);
            } else {
                nonceStr += Character.toString((char) index - 36 + 97);
            }
        }

        TreeMap<String, String> params = new TreeMap<>();
        params.put("jsapi_ticket", jsapi_ticket);
        params.put("noncestr", nonceStr);
        params.put("timestamp", String.valueOf(now / 1000));
        params.put("url", pageUrl);

        String code = ParameterStringBuilder.getCodeString(params);

        String hashtext = HashHelperService.hash(code.getBytes(), "SHA1");

        params.put("appId", appid);
        params.put("nonceStr", nonceStr);
        params.put("signature", hashtext);
        params.remove("jsapi_ticket");
        params.remove("noncestr");
        params.remove("url");

        return ParameterStringBuilder.getCodeString(params);
    }

}
