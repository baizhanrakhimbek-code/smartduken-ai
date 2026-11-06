package kz.smartduken.app.product;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import jakarta.persistence.criteria.Predicate;
@Service @Transactional(readOnly = true)
public class ProductService {
 private final ProductRepository repository;
 public ProductService(ProductRepository repository) { this.repository = repository; }
 public List<Product> findAll(String search, String category, boolean lowStock) {
  String nameFilter = search == null ? "" : search.strip().toLowerCase(Locale.ROOT);
  String categoryFilter = category == null ? "" : category.strip().toLowerCase(Locale.ROOT);
  return repository.findAll((root, query, builder) -> {
   var conditions = new ArrayList<Predicate>();
   if (!nameFilter.isEmpty()) {
    // locate treats % and _ as ordinary characters, not SQL wildcards.
    conditions.add(builder.greaterThan(builder.locate(builder.lower(root.get("name")), nameFilter), 0));
   }
   if (!categoryFilter.isEmpty()) {
    conditions.add(builder.equal(builder.lower(root.get("category")), categoryFilter));
   }
   if (lowStock) {
    conditions.add(builder.lessThanOrEqualTo(root.get("stockQuantity"), root.get("minimumStock")));
   }
   return builder.and(conditions.toArray(Predicate[]::new));
  }, Sort.by("id"));
 }
 public ProductSummary summarize() { return repository.summarize(); }
 public List<String> findCategories() { return repository.findCategories(); }
 public Product findById(Long id) { return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id)); }
 @Transactional public Product create(ProductRequest request) { return repository.save(new Product(request)); }
 @Transactional public Product update(Long id, ProductRequest request) { Product product = findById(id); product.update(request); return repository.save(product); }
 @Transactional public void delete(Long id) { repository.delete(findById(id)); }
}
