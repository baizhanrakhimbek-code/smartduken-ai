package kz.smartduken.app.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("select distinct p.category from Product p where p.category is not null and p.category <> '' order by p.category")
    List<String> findCategories();

    @Query("""
        select new kz.smartduken.app.product.ProductSummary(
            count(p),
            coalesce(sum(case when p.stockQuantity <= p.minimumStock then 1L else 0L end), 0L),
            coalesce(sum(case when p.stockQuantity = 0 then 1L else 0L end), 0L),
            coalesce(sum(p.price * p.stockQuantity), 0))
        from Product p
        """)
    ProductSummary summarize();
}
