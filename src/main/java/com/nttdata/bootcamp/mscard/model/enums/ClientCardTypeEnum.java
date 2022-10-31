package com.nttdata.bootcamp.mscard.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum ClientCardTypeEnum {

    PERSONAL(0),
    BUSINESS(1);

    private int value;
    private static Map map = new HashMap();

    private ClientCardTypeEnum(int value) {
        this.value = value;
    }

    static {
        for (ClientCardTypeEnum creditCardType : ClientCardTypeEnum.values()) {
            map.put(creditCardType.value, creditCardType);
        }
    }

    public static ClientCardTypeEnum valueOf(int creditCardType) {
        return (ClientCardTypeEnum) map.get(creditCardType);
    }

    public int getValue() {
        return value;
    }
}
