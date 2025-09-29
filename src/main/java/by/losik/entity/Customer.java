package by.losik.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = "lab2var10", name = "customers")
@Getter
@Setter
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", columnDefinition = "varchar(200)", nullable = false)
    private String name;

    @Column(name = "address", columnDefinition = "varchar(300)")
    private String address;

    @Column(name = "is_legal_entity", nullable = false)
    private Boolean isLegalEntity;

    @Column(name = "document_number", columnDefinition = "varchar(100)")
    private String documentNumber;

    @Column(name = "document_series", columnDefinition = "varchar(50)")
    private String documentSeries;

    @Column(name = "bank_name", columnDefinition = "varchar(200)")
    private String bankName;

    @Column(name = "bank_account", columnDefinition = "varchar(100)")
    private String bankAccount;
}