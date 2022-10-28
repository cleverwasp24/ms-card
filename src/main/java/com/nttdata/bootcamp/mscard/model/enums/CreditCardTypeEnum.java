package com.nttdata.bootcamp.mscard.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum CreditCardTypeEnum {

    PERSONAL(0),
    BUSINESS(1);

    private int value;
    private static Map map = new HashMap();

    private CreditCardTypeEnum(int value) {
        this.value = value;
    }

    static {
        for (CreditCardTypeEnum creditCardType : CreditCardTypeEnum.values()) {
            map.put(creditCardType.value, creditCardType);
        }
    }

    public static CreditCardTypeEnum valueOf(int creditCardType) {
        return (CreditCardTypeEnum) map.get(creditCardType);
    }

    public int getValue() {
        return value;
    }
}
