package com.project.demo.rest.product;

import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.product.Product;
import com.project.demo.logic.entity.product.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductRestController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;


    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> productPage = productRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productPage.getTotalPages());
        meta.setTotalElements(productPage.getTotalElements());
        meta.setPageNumber(productPage.getNumber() + 1);
        meta.setPageSize(productPage.getSize());

        return new GlobalResponseHandler().handleResponse("Products retrieved successfully",
                productPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProductById(@PathVariable Long id, HttpServletRequest request) {
        Optional<Product> product = productRepository.findById(id);

        if (product.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Product found",
                    product.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Product with ID " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product, HttpServletRequest request) {
        Long categoryId = product.getCategory().getId();
        Optional<Category> category = categoryRepository.findById(categoryId);

        if (category.isPresent()) {
            product.setCategory(category.get());
            Product saved = productRepository.save(product);
            return new GlobalResponseHandler().handleResponse("Product created successfully",
                    saved, HttpStatus.CREATED, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Category with ID " + categoryId + " not found",
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product input, HttpServletRequest request) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setName(input.getName());
            product.setDescription(input.getDescription());
            product.setPrice(input.getPrice());
            product.setStock(input.getStock());

            if (input.getCategory() != null) {
                Optional<Category> category = categoryRepository.findById(input.getCategory().getId());
                if (category.isEmpty()) {
                    return new GlobalResponseHandler().handleResponse("Category not found",
                            HttpStatus.BAD_REQUEST, request);
                }
                product.setCategory(category.get());
            }

            productRepository.save(product);
            return new GlobalResponseHandler().handleResponse("Product updated successfully",
                    product, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Product with ID " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, HttpServletRequest request) {
        Optional<Product> product = productRepository.findById(id);

        if (product.isPresent()) {
            productRepository.deleteById(id);
            return new GlobalResponseHandler().handleResponse("Product deleted successfully",
                    product.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Product with ID " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}