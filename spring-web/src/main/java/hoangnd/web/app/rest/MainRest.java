package hoangnd.web.app.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Main API")
@RestController
public class MainRest {

    @GetMapping("/hello")
    @Operation(summary = "Hello Admin")
    ResponseEntity<String> hello () {
        return ResponseEntity.ok("Hello Admin");
    }
}
