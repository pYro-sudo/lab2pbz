package by.losik.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(schema = "lab2var10", name = "categories")
@Getter
@Setter
@Accessors(fluent = true)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", columnDefinition = "serial", nullable = false, unique = true)
    protected Long id;

    @Column(name = "name", columnDefinition = "varchar(100)", nullable = false, unique = true)
    protected String name;
}
