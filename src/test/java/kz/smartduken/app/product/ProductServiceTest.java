package kz.smartduken.app.product;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ProductServiceTest {
 @Test void missingIdThrowsMeaningfulException() {
  ProductRepository repository = mock(ProductRepository.class);
  when(repository.findById(42L)).thenReturn(Optional.empty());
  ProductService service = new ProductService(repository);
  assertTrue(assertThrows(ProductNotFoundException.class, () -> service.findById(42L)).getMessage().contains("42"));
  assertThrows(ProductNotFoundException.class, () -> service.delete(42L));
  verify(repository, never()).delete(any());
 }
}
