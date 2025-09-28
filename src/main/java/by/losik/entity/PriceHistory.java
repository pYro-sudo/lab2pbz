package by.losik.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(schema = "lab2var10", name = "price_history")
@Getter
@Setter
@Accessors(fluent = true)
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    protected Product product;

    @Column(name = "change_date", nullable = false)
    private Date changeDate;

    @Column(name = "price", precision = 15, scale = 2, nullable = false)
    private BigDecimal price;
}
