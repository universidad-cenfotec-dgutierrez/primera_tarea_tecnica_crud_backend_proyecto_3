package com.project.demo.rest.category;

import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
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
@RequestMapping("/categories")
public class CategoryRestController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(categoryPage.getTotalPages());
        meta.setTotalElements(categoryPage.getTotalElements());
        meta.setPageNumber(categoryPage.getNumber() + 1);
        meta.setPageSize(categoryPage.getSize());

        return new GlobalResponseHandler().handleResponse("Categories retrieved successfully",
                categoryPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id, HttpServletRequest request) {
        Optional<Category> category = categoryRepository.findById(id);

        if (category.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Category found",
                    category.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Category with ID " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category, HttpServletRequest request) {
        Category saved = categoryRepository.save(category);
        return new GlobalResponseHandler().handleResponse("Category created successfully",
                saved, HttpStatus.CREATED, request);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category input, HttpServletRequest request) {
        Optional<Category> category = categoryRepository.findById(id);

        if (category.isPresent()) {
            Category entity = category.get();
            entity.setName(input.getName());
            entity.setDescription(input.getDescription());
            categoryRepository.save(entity);
            return new GlobalResponseHandler().handleResponse("Category updated successfully",
                    entity, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Category with ID " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        Optional<Category> category = categoryRepository.findById(id);

        if (category.isPresent()) {
            categoryRepository.deleteById(id);
            return new GlobalResponseHandler().handleResponse("Category deleted successfully",
                    category.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Category with ID " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}
