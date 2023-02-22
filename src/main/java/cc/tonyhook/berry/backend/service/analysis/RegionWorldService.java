package cc.tonyhook.berry.backend.service.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.analysis.RegionWorldRepository;
import cc.tonyhook.berry.backend.entity.analysis.RegionWorld;
import jakarta.annotation.PostConstruct;

@Service
public class RegionWorldService {

    @Autowired
    private RegionWorldRepository regionWorldRepository;

    private Map<String, RegionWorld> regionWorldMap;

    @PostConstruct
    private void initRegionList() {
        regionWorldMap = new HashMap<String, RegionWorld>();

        List<RegionWorld> regionWorldList = regionWorldRepository.findAll();
        for (RegionWorld regionWorld : regionWorldList) {
            regionWorldMap.put(regionWorld.getCode(), regionWorld);
        }
    }

    public RegionWorld getRegionWorld(String code) {
        return regionWorldMap.get(code);
    }

}
