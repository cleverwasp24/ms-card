package com.nttdata.bootcamp.mscard.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum CardTypeEnum {

    CREDIT(0),
    DEBIT(1);

    private int value;
    private static Map map = new HashMap();

    private CardTypeEnum(int value) {
        this.value = value;
    }

    static {
        for (CardTypeEnum creditCardType : CardTypeEnum.values()) {
            map.put(creditCardType.value, creditCardType);
        }
    }

    public static CardTypeEnum valueOf(int creditCardType) {
        return (CardTypeEnum) map.get(creditCardType);
    }

    public int getValue() {
        return value;
    }
}
