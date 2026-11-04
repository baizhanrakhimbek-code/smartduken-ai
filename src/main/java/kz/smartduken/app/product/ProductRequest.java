package kz.smartduken.app.product;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ProductRequest(
 @NotBlank(message = "Өнім атауы міндетті") @Size(max = 150) String name,
 @Size(max = 100) String category,
 @NotNull @DecimalMin(value = "0.00", message = "Баға теріс болмауы керек") @Digits(integer = 17, fraction = 2) BigDecimal price,
 @NotNull @PositiveOrZero(message = "Қор теріс болмауы керек") Integer stockQuantity,
 @NotNull @PositiveOrZero(message = "Ең аз қор теріс болмауы керек") Integer minimumStock
) {}
