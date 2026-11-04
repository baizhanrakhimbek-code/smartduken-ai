package kz.smartduken.app.product;
public class ProductNotFoundException extends RuntimeException {
 public ProductNotFoundException(Long id) { super("Өнім табылмады: " + id); }
}
