package cc.tonyhook.berry.backend.service.analysis;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.analysis.PhoneRepository;
import cc.tonyhook.berry.backend.entity.analysis.Phone;
import cc.tonyhook.berry.backend.entity.analysis.RegionChina;
import jakarta.annotation.PostConstruct;

@Service
public class PhoneService {

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private RegionChinaService regionChinaService;

    private List<Phone> phoneList;

    @PostConstruct
    private void initPhoneList() {
        phoneList = phoneRepository.findAll();
    }

    private String getRegionName(String regionCode) {
        String provinceCode = regionCode.substring(0, 2) + "0000";
        String cityCode = regionCode.substring(0, 4) + "00";
        String countyCode = regionCode;

        RegionChina province = regionChinaService.getRegionChina(provinceCode);
        RegionChina city = regionChinaService.getRegionChina(cityCode);
        RegionChina county = regionChinaService.getRegionChina(countyCode);

        if (province == null) {
            return "";
        }
        if (city == null) {
            cityCode = provinceCode;
            city = regionChinaService.getRegionChina(cityCode);
        }
        if (county == null) {
            countyCode = cityCode;
            county = regionChinaService.getRegionChina(countyCode);
        }

        return province.getName() + "," + city.getName() + "," + county.getName();
    }

    public String getPhoneInfo(String prefix) {
        for (Phone phone : phoneList) {
            if (phone.getPrefix().equals(prefix)) {
                return getRegionName(phone.getRegion()) + "," + phone.getIsp();
            }
        }
        return "";
    }

}
