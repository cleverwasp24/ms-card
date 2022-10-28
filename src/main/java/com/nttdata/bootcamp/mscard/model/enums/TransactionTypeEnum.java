package com.nttdata.bootcamp.mscard.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum TransactionTypeEnum {

    PURCHASE(0),
    PAY_DEBT(1);

    private int value;
    private static Map map = new HashMap();

    private TransactionTypeEnum(int value) {
        this.value = value;
    }

    static {
        for (TransactionTypeEnum transactionType : TransactionTypeEnum.values()) {
            map.put(transactionType.value, transactionType);
        }
    }

    public static TransactionTypeEnum valueOf(int transactionType) {
        return (TransactionTypeEnum) map.get(transactionType);
    }

    public int getValue() {
        return value;
    }

}
