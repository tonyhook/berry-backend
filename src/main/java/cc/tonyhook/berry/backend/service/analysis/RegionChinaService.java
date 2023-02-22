package cc.tonyhook.berry.backend.service.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.analysis.RegionChinaRepository;
import cc.tonyhook.berry.backend.entity.analysis.RegionChina;
import jakarta.annotation.PostConstruct;

@Service
public class RegionChinaService {

    @Autowired
    private RegionChinaRepository regionChinaRepository;

    private Map<String, RegionChina> regionChinaMap;

    @PostConstruct
    private void initRegionList() {
        regionChinaMap = new HashMap<String, RegionChina>();

        List<RegionChina> regionChinaList = regionChinaRepository.findAll();
        for (RegionChina regionChina : regionChinaList) {
            regionChinaMap.put(regionChina.getCode(), regionChina);
        }
    }

    public RegionChina getRegionChina(String code) {
        return regionChinaMap.get(code);
    }

    public List<RegionChina> getProvinceList() {
        List<RegionChina> provinceList = new ArrayList<RegionChina>();
        for (String code : regionChinaMap.keySet()) {
            if (code.endsWith("0000")) {
                provinceList.add(regionChinaMap.get(code));
            }
        }

        return provinceList;
    }

    public List<RegionChina> getCityList(String provinceCode) {
        List<RegionChina> cityList = new ArrayList<RegionChina>();
        for (String code : regionChinaMap.keySet()) {
            if (code.endsWith("00") && code.substring(0, 2).equals(provinceCode.substring(0, 2))) {
                cityList.add(regionChinaMap.get(code));
            }
        }

        if (cityList.size() > 1) {
            cityList.remove(regionChinaMap.get(provinceCode));
        }

        return cityList;
    }

    public List<RegionChina> getCountyList(String cityCode) {
        List<RegionChina> countyList = new ArrayList<RegionChina>();

        if (cityCode.substring(2, 4).equals("00")) {
            for (String code : regionChinaMap.keySet()) {
                if (code.substring(0, 2).equals(cityCode.substring(0, 2)) && !code.substring(2, 4).equals("00")) {
                    countyList.add(regionChinaMap.get(code));
                }
            }
        } else {
            for (String code : regionChinaMap.keySet()) {
                if (code.substring(0, 4).equals(cityCode.substring(0, 4))) {
                    countyList.add(regionChinaMap.get(code));
                }
            }
        }

        return countyList;
    }

    public List<RegionChina> getRegionList(String name) {
        List<RegionChina> regionList = new ArrayList<RegionChina>();

        for (RegionChina region : regionChinaMap.values()) {
            if (region.getName().equals(name)) {
                regionList.add(region);
            }
        }

        return regionList;
    }

}
