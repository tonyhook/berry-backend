package cc.tonyhook.berry.backend.controller.open;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.analysis.RegionChina;
import cc.tonyhook.berry.backend.entity.analysis.RegionWorld;
import cc.tonyhook.berry.backend.service.analysis.RegionChinaService;
import cc.tonyhook.berry.backend.service.analysis.RegionWorldService;

@RestController
public class OpenRegionController {

    @Autowired
    private RegionChinaService regionChinaService;
    @Autowired
    private RegionWorldService regionWorldService;

    @RequestMapping(value = "/api/open/region/china/province", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<RegionChina>> getProvinceList() {
        return ResponseEntity.ok().body(regionChinaService.getProvinceList());
    }

    @RequestMapping(value = "/api/open/region/china/city", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<RegionChina>> getCityList(
            @RequestParam(defaultValue = "") String province) {
        return ResponseEntity.ok().body(regionChinaService.getCityList(province));
    }

    @RequestMapping(value = "/api/open/region/china/county", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<RegionChina>> getCountyList(
            @RequestParam(defaultValue = "") String city) {
        return ResponseEntity.ok().body(regionChinaService.getCountyList(city));

    }

    @RequestMapping(value = "/api/open/region/china/code", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<RegionChina> getRegionChina(
            @RequestParam(defaultValue = "") String code) {
        return ResponseEntity.ok().body(regionChinaService.getRegionChina(code));
    }

    @RequestMapping(value = "/api/open/region/china/name", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<RegionChina>> getRegionList(
            @RequestParam(defaultValue = "") String name) {
        return ResponseEntity.ok().body(regionChinaService.getRegionList(name));
    }

    @RequestMapping(value = "/api/open/region/world/code", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<RegionWorld> getRegionWorld(
            @RequestParam(defaultValue = "") String code) {
        return ResponseEntity.ok().body(regionWorldService.getRegionWorld(code));
    }

}
