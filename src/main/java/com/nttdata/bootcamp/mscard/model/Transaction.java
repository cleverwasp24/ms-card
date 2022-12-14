package com.nttdata.bootcamp.mscard.model;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "transaction")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Transaction {

    @Transient
    public static final String SEQUENCE_NAME = "transaction_sequence";

    @Id
    private Long id;
    @NonNull
    private Long cardId;
    @NonNull
    private Integer transactionType;
    @NonNull
    private String description;
    @NonNull
    private Double amount;
    @Nullable
    private Long accountId;
    @Nullable
    private Double newAvailableCredit;
    @NonNull
    private LocalDateTime transactionDate;

}
