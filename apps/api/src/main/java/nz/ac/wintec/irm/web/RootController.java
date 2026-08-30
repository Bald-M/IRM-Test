package nz.ac.wintec.irm.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    String root() {
        return "hi, egg";
    }
}
