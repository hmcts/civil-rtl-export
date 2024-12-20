package uk.gov.hmcts.reform.civil.service.replace;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@Getter
@Setter
public class CharacterReplacementService {

    private Map<Integer, String> invalidCharacterReplacements;

    private static final String REGEX_INVALID_CHARS = "[^\\x20-\\x7F]";

    @Autowired
    public CharacterReplacementService(CharacterReplacementConfigProperties configProps) {
        invalidCharacterReplacements =
            configProps.getCharacterReplacements().stream()
                .collect(Collectors.toMap(
                    CharacterReplacement::getCharValue,
                    CharacterReplacement::getReplacement));
    }

    public String replaceCharacters(String fieldText, int maxLength) {
        if (fieldText == null) {
            // Text is null so return null
            log.debug("Text is null");
            return null;
        }

        Pattern invalidChars = Pattern.compile(REGEX_INVALID_CHARS);
        Matcher invalidCharsMatcher = invalidChars.matcher(fieldText);

        boolean hasInvalidChars = invalidCharsMatcher.find();

        if (hasInvalidChars) {
            log.debug("Text [{}] contains one or more invalid characters", fieldText);

            int startPos;
            int endPos;
            int invalidChar;

            String tempFieldText = fieldText;
            String replacement;
            StringBuilder newFieldText = new StringBuilder();

            while (hasInvalidChars) {
                startPos = invalidCharsMatcher.start();
                endPos = invalidCharsMatcher.end();

                // Append text that occurs before invalid character
                newFieldText.append(tempFieldText, 0, startPos);

                // Append replacement for invalid character if one has been defined
                invalidChar = tempFieldText.charAt(startPos);
                replacement = invalidCharacterReplacements.get(invalidChar);
                if (replacement != null) {
                    log.debug("Replaced invalid character [{}] with [{}]", invalidChar, replacement);
                    newFieldText.append(replacement);
                } else {
                    log.info("No replacement found for invalid character [{}], removing it from text", invalidChar);
                }

                // Remove text that has been appended ready for next search
                tempFieldText = tempFieldText.substring(endPos);

                // Search for more invalid characters in remaining text
                invalidCharsMatcher = invalidChars.matcher(tempFieldText);
                hasInvalidChars = invalidCharsMatcher.find();
            }

            // Append any remaining text
            newFieldText.append(tempFieldText);

            if (newFieldText.length() > maxLength) {
                // Replacing all the invalid characters has made the new text too long.  This can occur when a single
                // character is replaced with multiple characters.  Revert to returning the original text with all
                // invalid characters removed.
                log.info("Text with replacements [{}] exceeds maximum length, removing all invalid characters",
                         newFieldText);
                invalidCharsMatcher = invalidChars.matcher(fieldText);
                return invalidCharsMatcher.replaceAll("");
            } else {
                // Return text with invalid characters replaced
                return newFieldText.toString();
            }
        } else {
            // No invalid characters so return original text
            log.debug("Text [{}] contains no invalid characters", fieldText);
            return fieldText;
        }
    }
}
