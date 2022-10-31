package com.nttdata.bootcamp.mscard.model;

import com.fasterxml.jackson.annotation.JsonTypeId;
import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "card")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Card {

    @Transient
    public static final String SEQUENCE_NAME = "card_sequence";

    @Id
    private Long id;
    @NonNull
    private Long clientId;
    @NonNull
    private Integer cardType;
    @NonNull
    private Integer clientCardType;
    @NonNull
    @Indexed(unique = true)
    private String cardNumber;
    @Nullable
    private Double creditLine;
    @Nullable
    private Double availableCredit;
    @Nullable
    private Integer primaryAccountId;
    @Nullable
    private List<Integer> associatedAccountsId;
    @NonNull
    private LocalDateTime creationDate;

}
