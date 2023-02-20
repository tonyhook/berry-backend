package cc.tonyhook.berry.backend.service.analysis;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.analysis.IPAddressRepository;
import cc.tonyhook.berry.backend.entity.analysis.IPAddress;
import cc.tonyhook.berry.backend.entity.analysis.RegionChina;
import cc.tonyhook.berry.backend.entity.analysis.RegionWorld;
import jakarta.annotation.PostConstruct;

@Service
public class IPAddressService {

    public static final Integer REGION_LEVEL_COUNTRY  = 1;

    public static final Integer REGION_LEVEL_PROVINCE = 2;

    public static final Integer REGION_LEVEL_CITY     = 3;

    public static final Integer REGION_LEVEL_COUNTY   = 4;

    @Autowired
    private IPAddressRepository ipAddressRepository;

    @Autowired
    private RegionChinaService regionChinaService;
    @Autowired
    private RegionWorldService regionWorldService;

    private List<IPAddress> ipAddressList;

    @PostConstruct
    private void initRegionList() {
        ipAddressList = ipAddressRepository.findAll();
    }

    private String getRegionName(String regionCode, Integer level) {
        String countryCode = regionCode.substring(1, 4);
        RegionWorld regionWorld = regionWorldService.getRegionWorld(countryCode);

        if (regionWorld == null) {
            return "";
        }

        if ((level.equals(REGION_LEVEL_COUNTRY)) || !countryCode.equals("156")) {
            return regionWorld.getName();
        }

        String cityCode = regionCode.substring(4, 10);
        if (level.equals(REGION_LEVEL_PROVINCE)) {
            cityCode = cityCode.substring(0, 2) + "0000";
        }
        if (level.equals(REGION_LEVEL_CITY)) {
            cityCode = cityCode.substring(0, 4) + "00";
        }
        RegionChina regionChina = regionChinaService.getRegionChina(cityCode);

        if (regionChina == null) {
            return "";
        }

        return regionChina.getName();
    }

    public String resolveIPAddressRegion(String ipAddressStr, Integer level) {
        if (ipAddressList == null) {
            return "";
        }

        try {
            InetAddressValidator validator = InetAddressValidator.getInstance();
            List<String> regionCodeList = new ArrayList<String>();
            String cat = "";

            if (validator.isValidInet4Address(ipAddressStr)) {
                cat = "IPv4";
            }
            if (validator.isValidInet6Address(ipAddressStr)) {
                cat = "IPv6";
            }
            if (cat.equals("")) {
                return "";
            }

            InetAddress ia = InetAddress.getByName(ipAddressStr);
            byte[] ip = ia.getAddress();

            for (IPAddress ipAddress : ipAddressList) {
                if (ipAddress.getCat().equals(cat)) {
                    InetAddress iaStart = InetAddress.getByName(ipAddress.getIPAddressStart());
                    InetAddress iaEnd = InetAddress.getByName(ipAddress.getIPAddressEnd());
                    byte[] start = iaStart.getAddress();
                    byte[] end = iaEnd.getAddress();

                    Integer result = 0;
                    for (int i = 0; i < ip.length; i++) {
                        if (ip[i] > start[i]) {
                            result = 1;
                            break;
                        }
                        if (ip[i] < start[i]) {
                            result = -1;
                            break;
                        }
                    }
                    if (result < 0) {
                        continue;
                    }

                    result = 0;
                    for (int i = 0; i < ip.length; i++) {
                        if (ip[i] > end[i]) {
                            result = -1;
                            break;
                        }
                        if (ip[i] < end[i]) {
                            result = 1;
                            break;
                        }
                    }
                    if (result < 0) {
                        continue;
                    }

                    regionCodeList.add(ipAddress.getRegionCode());
                }
            }

            if (regionCodeList.size() > 0) {
                return getRegionName(regionCodeList.get(0), level);
            }

            return "";
        } catch (Exception e) {
            return "";
        }
    }

}
