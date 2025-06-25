package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;

import java.util.List;

@RestController
@RequestMapping("/categories")
@CrossOrigin

public class CategoriesController
{
    private CategoryDao categoryDao;
    private ProductDao productDao;


    @Autowired
    public CategoriesController(CategoryDao categoryDao, ProductDao productDao){
        this.categoryDao = categoryDao;
        this.productDao = productDao;
    }

    @PreAuthorize("permitAll()")
    @GetMapping()
    public List<Category> getAll()
    {
        System.out.println("📦 Fetching all categories...");
        return categoryDao.getAllCategories();
    }

    @GetMapping("/{id}")
    public Category getById(@PathVariable int id)
    {
        System.out.println("DEBUG: CategoriesController.getById called for ID: " + id);
        Category category = categoryDao.getById(id);

        if (category == null) {
            System.out.println("DEBUG: Category ID " + id + " not found by DAO. Throwing 404."); // ✅ NEW DEBUG LINE
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + id);
        }
        System.out.println("DEBUG: Category ID " + id + " found. Returning 200 OK."); // ✅ NEW DEBUG LINE
        return category;
    }


    @GetMapping("/{categoryId}/products")
    public List<Product> getProductsById(@PathVariable int categoryId)
    {
        return productDao.listByCategoryId(categoryId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Category> addCategory(@RequestBody Category category)
    {
        Category createdCategory = categoryDao.create(category);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateCategory(@PathVariable int id, @RequestBody Category category)
    {
        categoryDao.update(id,category);
    }


    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCategory(@PathVariable int id)
    {
        try {
            Category existingCategory = categoryDao.getById(id);
            if (existingCategory == null) {
                System.out.println("DEBUG: Delete called for non-existent Category ID: " + id + ". Throwing 404.");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + id);
            }

            System.out.println("DEBUG: Deleting category with ID: " + id);
            categoryDao.delete(id);
            System.out.println("DEBUG: Category ID " + id + " deleted successfully.");

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResponseStatusException ex) {
            System.err.println("ERROR: Propagating ResponseStatusException during delete for ID " + id + ": " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            System.err.println("ERROR: Unexpected error deleting category with ID " + id + ": " + ex.getMessage());
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting category: " + ex.getMessage(), ex);
        }
    }
}
