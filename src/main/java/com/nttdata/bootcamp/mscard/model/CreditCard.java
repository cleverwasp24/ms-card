package com.nttdata.bootcamp.mscard.model;

import com.mongodb.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "creditcard")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreditCard {

    @Id
    private Integer id;
    @NonNull
    private Integer clientId;
    @NonNull
    private Integer creditCardType;
    @NonNull
    @Indexed(unique = true)
    private String creditCardNumber;
    @NonNull
    private Double creditLine;
    @NonNull
    private Double availableCredit;
    @NonNull
    private LocalDateTime creationDate;

}
