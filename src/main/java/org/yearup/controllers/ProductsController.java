package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity; // Import ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;
import org.springframework.web.server.ResponseStatusException; // Import for 404 handling

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products") // This controller handles requests starting with /products
@CrossOrigin
public class ProductsController {

    private ProductDao productDao;

    @Autowired
    public ProductsController(ProductDao productDao) {
        this.productDao = productDao;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Product> searchProducts(
            @RequestParam(name = "cat", required = false) Integer categoryId,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "color", required = false) String color
    ) {
        System.out.println("DEBUG: ProductController.searchProducts called with: cat=" + categoryId +
                ", minPrice=" + minPrice + ", maxPrice=" + maxPrice + ", color=" + color);
        return productDao.search(categoryId, minPrice, maxPrice, color);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public Product getProductById(@PathVariable int id) {
        Product product = productDao.getById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id);
        }
        return product;
    }

    @PostMapping // For creating new products
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED) // Return 201 for creation
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        System.out.println("DEBUG: ProductsController.createProduct method CALLED.");
        Product createdProduct = productDao.create(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    // ✅ THE FIX IS HERE: Use @PutMapping for updating existing resources
    @PutMapping("/{id}") // Maps to PUT /products/{id}
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Only admins can update
    @ResponseStatus(HttpStatus.NO_CONTENT) // Typically 204 No Content for successful PUT (if no Body returned)
    public void updateProduct(@PathVariable int id, @RequestBody Product product) {
        System.out.println("DEBUG: ProductsController.updateProduct called for ID: " + id);
        // Add a check to ensure the product exists before attempting to update
        Product existingProduct = productDao.getById(id);
        if (existingProduct == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id);
        }

        // You might want to ensure the ID in the path matches the ID in the request body
        // if (product.getProductId() != id) {
        //    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID in path does not match ID in body");
        // }

        productDao.update(id, product);
        System.out.println("DEBUG: Product ID " + id + " updated successfully.");
        // Return nothing, or a ResponseEntity<Void> with HttpStatus.NO_CONTENT
    }


    // Optional: Add a DELETE endpoint for products if you plan to delete individual products
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Typically 204 No Content for successful DELETE
    public void deleteProduct(@PathVariable int id) {
        System.out.println("DEBUG: ProductsController.deleteProduct called for ID: " + id);
        // Add a check to ensure the product exists before deleting
        Product existingProduct = productDao.getById(id);
        if (existingProduct == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id);
        }
        productDao.delete(id);
        System.out.println("DEBUG: Product ID " + id + " deleted successfully.");
    }
}
