package uk.gov.hmcts.reform.civil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import uk.gov.hmcts.reform.civil.service.task.ScheduledTaskRunner;

@SpringBootApplication
@Slf4j
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application implements CommandLineRunner {

    private static final String ENV_TASK_NAME = "TASK_NAME";

    private final ScheduledTaskRunner scheduledTaskRunner;

    @Autowired
    public Application(ScheduledTaskRunner scheduledTaskRunner) {
        this.scheduledTaskRunner = scheduledTaskRunner;
    }

    public static void main(final String[] args) {
        ConfigurableApplicationContext instance = SpringApplication.run(Application.class, args);
        if (System.getenv(ENV_TASK_NAME) != null) {
            instance.close();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        String taskName = System.getenv(ENV_TASK_NAME);
        if (taskName != null) {
            log.info("*** Running scheduled task: [{}] ***", taskName);
            scheduledTaskRunner.run(taskName);
        }
    }
}
