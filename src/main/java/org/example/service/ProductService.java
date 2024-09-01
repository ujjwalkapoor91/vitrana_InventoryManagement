package org.example.service;

import org.example.entity.Product;
import org.example.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findAllByCategory(category);
    }

    @Transactional
    public Product updateStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        int oldQuantity = product.getStockQuantity();
        product.setStockQuantity(quantity);

        adjustPrice(product, oldQuantity);

        product = productRepository.save(product);
        checkStockAlerts(product, oldQuantity);

        return product;
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    private void adjustPrice(Product product, int oldQuantity) {
        if (product.getStockQuantity() < product.getMinStockLevel()) {
            BigDecimal newPrice = product.getPrice().multiply(BigDecimal.valueOf(1.10));
            product.setPrice(newPrice);
        } else if (product.getStockQuantity() > 2 * product.getMinStockLevel()) {
            BigDecimal newPrice = product.getPrice().multiply(BigDecimal.valueOf(0.95));
            if (newPrice.compareTo(product.getPrice()) < 0) {
                product.setPrice(newPrice);
            }
        }
    }

    private void checkStockAlerts(Product product, int oldQuantity) {
        if (product.getStockQuantity() < product.getMinStockLevel()) {
            System.out.println("Alert: Stock for " + product.getName() + " is below the minimum level.");
        }

        if (product.getStockQuantity() > oldQuantity) {
            System.out.println("Alert: Stock for " + product.getName() + " has been updated.");
        }
    }
}
