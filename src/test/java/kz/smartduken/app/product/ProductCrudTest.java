package kz.smartduken.app.product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ProductCrudTest {
 @Autowired MockMvc mvc;
 @Autowired ProductRepository repository;
 @Autowired ObjectMapper mapper;
 private final String valid = """
 {"name":"Сүт","category":"Сүт өнімдері","price":550.25,"stockQuantity":12,"minimumStock":3}
 """;
 @BeforeEach void clean() { repository.deleteAll(); }
 @Test void completeCrud() throws Exception {
  String body = mvc.perform(post("/api/products").contentType("application/json").content(valid))
   .andExpect(status().isCreated()).andExpect(header().exists("Location"))
   .andExpect(jsonPath("$.name").value("Сүт")).andReturn().getResponse().getContentAsString();
  long id = mapper.readTree(body).get("id").asLong();
  mvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
  mvc.perform(get("/api/products/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.price").value(550.25));
  mvc.perform(put("/api/products/{id}", id).contentType("application/json").content(valid.replace("550.25", "600.00").replace("12", "8")))
   .andExpect(status().isOk()).andExpect(jsonPath("$.stockQuantity").value(8));
  mvc.perform(get("/api/products/{id}", id)).andExpect(jsonPath("$.price").value(600.0));
  mvc.perform(delete("/api/products/{id}", id)).andExpect(status().isNoContent());
  mvc.perform(get("/api/products/{id}", id)).andExpect(status().isNotFound()).andExpect(jsonPath("$.detail").exists());
  mvc.perform(get("/api/products")).andExpect(jsonPath("$", hasSize(0)));
 }
 @Test void rejectsInvalidFields() throws Exception {
  mvc.perform(post("/api/products").contentType("application/json").content("""
   {"name":" ","price":-1,"stockQuantity":-1,"minimumStock":-2}
   """ )).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.name").exists())
   .andExpect(jsonPath("$.errors.price").exists()).andExpect(jsonPath("$.errors.stockQuantity").exists())
   .andExpect(jsonPath("$.errors.minimumStock").exists());
  mvc.perform(post("/api/products").contentType("application/json").content("{}"))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.price").exists());
 }
 @Test void invalidUpdateDoesNotChangeProduct() throws Exception {
  var product = repository.save(new Product(new ProductRequest("Нан", null, java.math.BigDecimal.ONE, 0, 0)));
  mvc.perform(put("/api/products/{id}", product.getId()).contentType("application/json").content(valid.replace("550.25", "-1")))
   .andExpect(status().isBadRequest());
  mvc.perform(get("/api/products/{id}", product.getId())).andExpect(jsonPath("$.name").value("Нан"));
 }
 @Test void missingProductsAndMalformedRequests() throws Exception {
  mvc.perform(put("/api/products/999999").contentType("application/json").content(valid)).andExpect(status().isNotFound());
  mvc.perform(delete("/api/products/999999")).andExpect(status().isNotFound());
  mvc.perform(get("/api/products/abc")).andExpect(status().isBadRequest());
  mvc.perform(post("/api/products").contentType("application/json").content("{"))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").exists());
  mvc.perform(post("/api/products").contentType("application/json").content(valid.replace("12", "1.2")))
   .andExpect(status().isBadRequest());
 }
 @Test void zeroValuesAndFrontendWork() throws Exception {
  mvc.perform(post("/api/products").contentType("application/json").content(valid.replace("550.25", "0").replace("12", "0").replace(":3", ":0")))
   .andExpect(status().isCreated()).andExpect(jsonPath("$.price").value(0));
  mvc.perform(get("/index.html")).andExpect(status().isOk()).andExpect(content().string(containsString("lang=\"kk\"")));
 }
}
