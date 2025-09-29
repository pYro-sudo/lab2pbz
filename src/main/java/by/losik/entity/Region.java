package by.losik.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = "lab2var10", name = "regions")
@Getter
@Setter
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", columnDefinition = "serial", nullable = false, unique = true)
    private Long id;

    @Column(name = "name", columnDefinition = "varchar(100)", nullable = false, unique = true)
    private String name;

    @Column(name = "country", columnDefinition = "varchar(100)", nullable = false)
    private String country;
}
