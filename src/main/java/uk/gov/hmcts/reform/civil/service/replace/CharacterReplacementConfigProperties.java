package uk.gov.hmcts.reform.civil.service.replace;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "replace")
@Getter
@Setter
public class CharacterReplacementConfigProperties {

    private List<CharacterReplacement> characterReplacements;
}
