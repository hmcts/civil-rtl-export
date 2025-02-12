package uk.gov.hmcts.reform.civil.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ScheduledTaskRunner {

    private final ApplicationContext applicationContext;

    @Autowired
    public ScheduledTaskRunner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void run(String taskName) {
        String beanName = Character.toLowerCase(taskName.charAt(0)) + taskName.substring(1);
        Runnable task = getTask(beanName);

        if (task != null) {
            log.info("Running task [{}] (bean [{}])", taskName, beanName);
            task.run();
        } else {
            log.error("Task not found [{}] (bean [{}])", taskName, beanName);
        }
    }

    private Runnable getTask(String beanName) {
        try {
            return (Runnable) applicationContext.getBean(beanName);
        } catch (Exception e) {
            log.error("Error finding task", e);
            return null;
        }
    }
}
