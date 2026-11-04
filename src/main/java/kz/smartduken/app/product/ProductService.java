package kz.smartduken.app.product;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @Transactional(readOnly = true)
public class ProductService {
 private final ProductRepository repository;
 public ProductService(ProductRepository repository) { this.repository = repository; }
 public List<Product> findAll() { return repository.findAll(Sort.by("id")); }
 public Product findById(Long id) { return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id)); }
 @Transactional public Product create(ProductRequest request) { return repository.save(new Product(request)); }
 @Transactional public Product update(Long id, ProductRequest request) { Product product = findById(id); product.update(request); return repository.save(product); }
 @Transactional public void delete(Long id) { repository.delete(findById(id)); }
}
