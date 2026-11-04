package kz.smartduken.app.product;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name = "products")
public class Product {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(nullable = false, length = 150) private String name;
 @Column(length = 100) private String category;
 @Column(nullable = false, precision = 19, scale = 2) private BigDecimal price;
 @Column(nullable = false) private Integer stockQuantity;
 @Column(nullable = false) private Integer minimumStock;
 protected Product() {}
 public Product(ProductRequest request) { update(request); }
 public void update(ProductRequest request) {
  name = request.name().strip(); category = request.category() == null ? null : request.category().strip();
  price = request.price(); stockQuantity = request.stockQuantity(); minimumStock = request.minimumStock();
 }
 public Long getId() { return id; }
 public String getName() { return name; }
 public String getCategory() { return category; }
 public BigDecimal getPrice() { return price; }
 public Integer getStockQuantity() { return stockQuantity; }
 public Integer getMinimumStock() { return minimumStock; }
}
