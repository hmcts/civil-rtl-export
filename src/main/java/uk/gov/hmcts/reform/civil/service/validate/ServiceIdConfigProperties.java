package uk.gov.hmcts.reform.civil.service.validate;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "validate")
@Getter
@Setter
public class ServiceIdConfigProperties {

    List<String> serviceIds;
}
