package uk.gov.hmcts.reform.civil.service.replace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CharacterReplacement {

    int charValue;

    String description;

    String replacement;
}
