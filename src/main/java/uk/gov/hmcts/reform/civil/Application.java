package uk.gov.hmcts.reform.civil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.reform.civil"})
//@SpringBootApplication
//@EnableJpaRepositories(basePackages = {"uk.gov.hmcts.reform.civil"})
//@EnableTransactionManagement
//@EnableScheduling
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
