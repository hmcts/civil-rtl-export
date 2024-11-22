package uk.gov.hmcts.reform.civil.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.model.Defendant;
import uk.gov.hmcts.reform.civil.model.DefendantAddress;
import uk.gov.hmcts.reform.civil.model.JudgmentEvent;
import uk.gov.hmcts.reform.civil.service.replace.CharacterReplacementService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class JudgmentEventTransformerService {

    private static final String JUDGMENT_ID_SUFFIX_1 = "-1";
    private static final String JUDGMENT_ID_SUFFIX_2 = "-2";

    private static final int MAX_LENGTH_DEFENDANT_NAME = 70;
    private static final int MAX_LENGTH_DEFENDANT_ADDRESS_LINE = 35;
    private static final int MAX_LENGTH_DEFENDANT_POSTCODE = 8;

    private final CharacterReplacementService characterReplacementService;

    @Autowired
    public JudgmentEventTransformerService(CharacterReplacementService characterReplacementService) {
        this.characterReplacementService = characterReplacementService;
    }

    public List<Judgment> transformJudgmentEvent(JudgmentEvent judgmentEvent, String courtLocationCode) {
        List<Judgment> judgments = new ArrayList<>();

        Judgment judgment1 =
            createJudgment(judgmentEvent, courtLocationCode, JUDGMENT_ID_SUFFIX_1, judgmentEvent.getDefendant1());
        judgments.add(judgment1);

        Defendant defendant2 = judgmentEvent.getDefendant2();
        if (defendant2 != null) {
            Judgment judgment2 = createJudgment(judgmentEvent, courtLocationCode, JUDGMENT_ID_SUFFIX_2, defendant2);
            judgments.add(judgment2);
        }

        return judgments;
    }

    private Judgment createJudgment(JudgmentEvent judgmentEvent,
                                    String courtLocationCode,
                                    String judgmentIdSuffix,
                                    Defendant defendant) {
        String judgmentId = judgmentEvent.getJudgmentId() + judgmentIdSuffix;
        log.debug("Create judgment with judgmentId [{}] from judgmentEvent: "
                      + "serviceId [{}], judgmentId [{}], timestamp [{}], caseNumber [{}]",
                  judgmentId,
                  judgmentEvent.getServiceId(),
                  judgmentEvent.getJudgmentId(),
                  judgmentEvent.getJudgmentEventTimeStamp(),
                  judgmentEvent.getCaseNumber());

        Judgment judgment = new Judgment();

        judgment.setServiceId(judgmentEvent.getServiceId());
        judgment.setJudgmentId(judgmentId);
        judgment.setJudgmentEventTimestamp(judgmentEvent.getJudgmentEventTimeStamp());
        judgment.setCourtCode(courtLocationCode);
        judgment.setCcdCaseRef(judgmentEvent.getCcdCaseRef());
        judgment.setCaseNumber(judgmentEvent.getCaseNumber());
        judgment.setJudgmentAdminOrderTotal(judgmentEvent.getJudgmentAdminOrderTotal());
        judgment.setJudgmentAdminOrderDate(judgmentEvent.getJudgmentAdminOrderDate());
        judgment.setRegistrationType(judgmentEvent.getRegistrationType().getRegType());
        judgment.setCancellationDate(judgmentEvent.getCancellationDate());

        judgment.setDefendantName(parseDefendantName(defendant.getDefendantName()));

        DefendantAddress address = defendant.getDefendantAddress();
        judgment.setDefendantAddressLine1(parseDefendantAddressLine(address.getDefendantAddressLine1()));
        judgment.setDefendantAddressLine2(parseDefendantAddressLine(address.getDefendantAddressLine2()));
        judgment.setDefendantAddressLine3(parseDefendantAddressLine(address.getDefendantAddressLine3()));
        judgment.setDefendantAddressLine4(parseDefendantAddressLine(address.getDefendantAddressLine4()));
        judgment.setDefendantAddressLine5(parseDefendantAddressLine(address.getDefendantAddressLine5()));
        judgment.setDefendantAddressPostcode(parseDefendantPostcode(address.getDefendantPostcode()));

        judgment.setDefendantDob(defendant.getDefendantDateOfBirth());

        return judgment;
    }

    private String parseDefendantName(String defendantName) {
        log.debug("Parse defendantName [{}]", defendantName);
        return parseTextField(defendantName, MAX_LENGTH_DEFENDANT_NAME);
    }

    private String parseDefendantAddressLine(String defendantAddressField) {
        log.debug("Parse defendantAddressLine [{}]", defendantAddressField);
        return parseTextField(defendantAddressField, MAX_LENGTH_DEFENDANT_ADDRESS_LINE);
    }

    private String parseDefendantPostcode(String defendantPostcode) {
        log.debug("Parse defendantPostcode [{}]", defendantPostcode);
        return parseTextField(defendantPostcode, MAX_LENGTH_DEFENDANT_POSTCODE);
    }

    private String parseTextField(String textField, int maxFieldLength) {
        return characterReplacementService.replaceCharacters(textField, maxFieldLength);
    }
}
