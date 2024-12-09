package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RegistrationType {

    JUDGMENT_REGISTERED("R"),
    JUDGMENT_CANCELLED("C"),
    JUDGMENT_SATISFIED("S"),
    JUDGMENT_MODIFIED("M"),
    ADMIN_ORDER_REGISTERED("A"),
    ADMIN_ORDER_REVOKED("K"),
    ADMIN_ORDER_VARIED("V");

    private final String regType;

    RegistrationType(String regType) {
        this.regType = regType;
    }

    @JsonValue
    public String getRegType() {
        return regType;
    }
}
