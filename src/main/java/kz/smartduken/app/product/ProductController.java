package kz.smartduken.app.product;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
@RestController @RequestMapping("/api/products")
public class ProductController {
 private final ProductService service;
 public ProductController(ProductService service) { this.service = service; }
 @GetMapping public List<Product> list(
   @RequestParam(defaultValue = "") String search,
   @RequestParam(defaultValue = "") String category) {
  return service.findAll(search, category);
 }
 @GetMapping("/categories") public List<String> categories() { return service.findCategories(); }
 @GetMapping("/{id}") public Product get(@PathVariable Long id) { return service.findById(id); }
 @PostMapping public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
  Product product = service.create(request);
  return ResponseEntity.created(URI.create("/api/products/" + product.getId())).body(product);
 }
 @PutMapping("/{id}") public Product update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) { return service.update(id, request); }
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
