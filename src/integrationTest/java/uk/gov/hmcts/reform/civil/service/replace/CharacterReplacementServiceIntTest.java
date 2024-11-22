package uk.gov.hmcts.reform.civil.service.replace;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    initializers = ConfigDataApplicationContextInitializer.class,
    classes = CharacterReplacementService.class
)
@EnableConfigurationProperties(CharacterReplacementConfigProperties.class)
@ActiveProfiles("itest")
class CharacterReplacementServiceIntTest {
    private static final int FIELD_LENGTH_ADDRESS_LINE = 35;

    private final CharacterReplacementService characterReplacementService;

    @Autowired
    public CharacterReplacementServiceIntTest(CharacterReplacementService characterReplacementService) {
        this.characterReplacementService = characterReplacementService;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("textAndExpectedText")
    void testReplaceCharacters(String fieldText, String expectedText) {
        String newTextValue = characterReplacementService.replaceCharacters(fieldText, FIELD_LENGTH_ADDRESS_LINE);
        assertEquals(expectedText, newTextValue, "Text produced does not match expected text");
    }

    @Test
    void testReplaceCharactersNull() {
        String newTextValue = characterReplacementService.replaceCharacters(null, FIELD_LENGTH_ADDRESS_LINE);
        assertNull(newTextValue, "Text should be null");
    }

    private static Stream<Arguments> textAndExpectedText() {
        return Stream.of(
            arguments(named("No text (empty string)", ""), ""),
            arguments(named("No invalid characters", "TEST ADDRESS LINE ONE"), "TEST ADDRESS LINE ONE"),
            arguments(named("Single invalid character", "TEST ADDRESS LÏNE TWO"), "TEST ADDRESS LINE TWO"),
            arguments(named("Multiple invalid characters", "TEST ADDRESS LÏNE TWÔ"), "TEST ADDRESS LINE TWO"),
            arguments(named("Text too long after invalid characters replaced", "½TEST ADDRESS LÏNE THREE MAX LENGTH"),
                      "TEST ADDRESS LNE THREE MAX LENGTH"),
            arguments(named("Single unknown invalid character", "TEST ADDRESS LINE FØUR"), "TEST ADDRESS LINE FUR")
        );
    }
}
