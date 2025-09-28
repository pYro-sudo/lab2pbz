package by.losik.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(schema = "lab2var10", name = "settlements")
@Getter
@Setter
@Accessors(fluent = true)
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", columnDefinition = "serial", nullable = false, unique = true)
    protected Long id;

    @Column(name = "name", columnDefinition = "varchar(100)", nullable = false, unique = true)
    protected String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Region region;
}
