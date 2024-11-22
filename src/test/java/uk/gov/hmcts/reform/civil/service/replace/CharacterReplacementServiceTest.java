package uk.gov.hmcts.reform.civil.service.replace;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CharacterReplacementServiceTest {

    private CharacterReplacementService characterReplacementService;

    @BeforeEach
    void setUp() {
        List<CharacterReplacement> replacements = new ArrayList<>();
        replacements.add(new CharacterReplacement(192, "Latin capital letter A with grave", "A"));
        replacements.add(new CharacterReplacement(198, "Latin capital letter AE", "AE"));
        replacements.add(new CharacterReplacement(200, "Latin capital letter E with grave", "E"));

        CharacterReplacementConfigProperties configProps = new CharacterReplacementConfigProperties();
        configProps.setCharacterReplacements(replacements);

        characterReplacementService = new CharacterReplacementService(configProps);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("textAndExpectedText")
    void testReplaceCharacters(String fieldText, String expectedText, int maxLength) {
        String newTextValue = characterReplacementService.replaceCharacters(fieldText, maxLength);
        assertEquals(expectedText, newTextValue, "Text produced does not match expected text");
    }

    @Test
    void testReplaceCharactersNull() {
        String newTextValue = characterReplacementService.replaceCharacters(null, 3);
        assertNull(newTextValue, "Text should be null");
    }

    private static Stream<Arguments> textAndExpectedText() {
        return Stream.of(
            arguments(named("No text (empty string)", ""), "", 3),
            arguments(named("No invalid characters", "ABCDE"), "ABCDE", 5),
            arguments(named("Single invalid char at start of text", "ÀBCDE"), "ABCDE", 5),
            arguments(named("Single invalid char in middle of text", "ABÀCD"), "ABACD", 5),
            arguments(named("Single invalid char at end of text", "ABCDÀ"), "ABCDA", 5),
            arguments(named("Multiple invalid chars at start of text", "ÀÈBC"), "AEBC", 4),
            arguments(named("Multiple invalid chars in middle of text", "AÀÈB"), "AAEB", 4),
            arguments(named("Multiple invalid chars at end of text", "ABÀÈ"), "ABAE", 4),
            arguments(named("Multiple non-consecutive invalid chars", "AÀBCDÈE"), "AABCDEE", 7),
            arguments(named("Single unknown invalid char at start of text", "¿ABCD"), "ABCD", 5),
            arguments(named("Single unknown invalid char in middle of text", "AB¿CD"), "ABCD", 5),
            arguments(named("Single unknown invalid char at end of text", "ABCD¿"), "ABCD", 5),
            arguments(named("Text too long after invalid character replaced", "ÆBCD"), "BCD", 4)
        );
    }
}
