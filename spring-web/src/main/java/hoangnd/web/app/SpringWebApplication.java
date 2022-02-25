package hoangnd.web.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.log4j.Log4j2;

@SpringBootApplication
@Log4j2
public class SpringWebApplication {

    public static void main (final String[] args) {
        SpringApplication.run(SpringWebApplication.class, args);
    }

}
