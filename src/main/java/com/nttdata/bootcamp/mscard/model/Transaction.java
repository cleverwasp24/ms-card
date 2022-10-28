package com.nttdata.bootcamp.mscard.model;

import com.mongodb.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "transaction")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Transaction {

    @Id
    private Integer id;
    @NonNull
    private Integer creditCardId;
    @NonNull
    private Integer transactionType;
    @NonNull
    private String description;
    @NonNull
    private Double amount;
    @NonNull
    private Double newAvailableCredit;
    @NonNull
    private LocalDateTime transactionDate;

}
