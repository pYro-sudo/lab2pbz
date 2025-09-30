package by.losik.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(schema = "lab2var10", name = "products")
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", columnDefinition = "serial", nullable = false, unique = true)
    private Long id;

    @Column(name = "code", columnDefinition = "varchar(50)", nullable = false, unique = true)
    private String code;

    @Column(name = "name", columnDefinition = "varchar(200)", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Category category;

    @Column(name = "manufacturer", columnDefinition = "varchar(200)", nullable = false)
    private String manufacturer;
}
