package cc.tonyhook.berry.backend.controller.managed.cms;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.cms.Carousel;
import cc.tonyhook.berry.backend.service.cms.CarouselService;
import cc.tonyhook.berry.backend.service.upload.FileUploadService;

@RestController
public class CarouselController {

    @Autowired
    private CarouselService carouselService;
    @Autowired
    private FileUploadService fileUploadService;

    @RequestMapping(value = "/api/managed/carousel", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Carousel>> getCarouselList(
            @RequestParam(defaultValue = "") String list) {
        List<Carousel> carouselList = carouselService.getCarouselList(list);

        return ResponseEntity.ok().body(carouselList);
    }

    @RequestMapping(value = "/api/managed/carousel/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Carousel> getCarousel(
            @PathVariable Integer id) {
        Carousel carousel = carouselService.getCarousel(id);

        if (carousel != null) {
            return ResponseEntity.ok().body(carousel);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/carousel", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Carousel> addCarousel(
            @RequestBody Carousel newCarousel) throws URISyntaxException {
        Carousel updatedCarousel = carouselService.addCarousel(newCarousel);

        return ResponseEntity
                .created(new URI("/api/managed/carousel/" + updatedCarousel.getId()))
                .body(updatedCarousel);
    }

    @RequestMapping(value = "/api/managed/carousel/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateCarousel(
            @PathVariable Integer id,
            @RequestBody Carousel newCarousel) {
        if (!id.equals(newCarousel.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Carousel targetCarousel = carouselService.getCarousel(id);
        if (targetCarousel == null) {
            return ResponseEntity.notFound().build();
        }

        if (targetCarousel.getImage() != null && !targetCarousel.getImage().equals(newCarousel.getImage())) {
            fileUploadService.delete("carousel", String.valueOf(id), targetCarousel.getImage());
            fileUploadService.delete("carousel", String.valueOf(id), targetCarousel.getImage() + ".thumbnail");
        }

        carouselService.updateCarousel(id, newCarousel);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/carousel/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeCarousel(
            @PathVariable Integer id) {
        Carousel deletedCarousel = carouselService.getCarousel(id);
        if (deletedCarousel == null) {
            return ResponseEntity.notFound().build();
        }

        if (deletedCarousel.getImage() != null) {
            fileUploadService.delete("carousel", String.valueOf(id), deletedCarousel.getImage());
            fileUploadService.delete("carousel", String.valueOf(id), deletedCarousel.getImage() + ".thumbnail");
        }

        carouselService.removeCarousel(id);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/carousel/cleanup", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<String>> cleanupCarousel(
            @RequestParam(defaultValue = "false") Boolean delete) {
        List<Carousel> carouselList = carouselService.getCarouselList();
        Map<Integer, Carousel> carouselMap = new HashMap<Integer, Carousel>();
        List<String> action = new ArrayList<String>();

        for (Carousel carousel : carouselList) {
            carouselMap.put(carousel.getId(), carousel);
        }

        Set<String> carouselIDSet = fileUploadService.listId("carousel");

        for (String carouselID : carouselIDSet) {
            if (NumberUtils.isCreatable(carouselID)) {
                if (!carouselMap.containsKey(NumberUtils.toInt(carouselID))) {
                    if (delete) {
                        fileUploadService.delete("carousel", carouselID);
                    }
                    action.add("DELETE carousel: " + carouselID);
                } else {
                    Boolean imageFound = false;
                    Boolean thumbnailFound = false;

                    Set<String> carouselFileSet = fileUploadService.listFile("carousel", carouselID);
                    for (String carouselFile : carouselFileSet) {
                        if (carouselFile.equals(carouselMap.get(NumberUtils.toInt(carouselID)).getImage())) {
                            imageFound = true;
                        } else if (carouselFile.equals(carouselMap.get(NumberUtils.toInt(carouselID)).getImage() + ".thumbnail")) {
                            thumbnailFound = true;
                        } else {
                            if (delete) {
                                fileUploadService.delete("carousel", carouselID, carouselFile);
                            }
                            action.add("DELETE carousel: " + carouselID + "/" + carouselFile);
                        }
                    }

                    if (imageFound && !thumbnailFound) {
                        byte[] image = fileUploadService.download("carousel", carouselID, carouselMap.get(NumberUtils.toInt(carouselID)).getImage());
                        byte[] thumbnail = fileUploadService.getThumbnail(image);
                        fileUploadService.upload("carousel", carouselID, carouselMap.get(NumberUtils.toInt(carouselID)).getImage() + ".thumbnail", thumbnail);
                        action.add("CREATE carousel: " + carouselID + "/" + carouselMap.get(NumberUtils.toInt(carouselID)).getImage() + ".thumbnail");
                    }
                }
            }
        }

        return ResponseEntity.ok().body(action);
    }

}
