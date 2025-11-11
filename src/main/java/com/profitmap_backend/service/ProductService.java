package com.profitmap_backend.service;

import com.profitmap_backend.model.Product;
import com.profitmap_backend.model.Kpd;
import com.profitmap_backend.repository.KpdRepository;
import com.profitmap_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final KpdRepository kpdRepository;

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> getByCompanyId(Long companyId) {
        return productRepository.findByCompanyId(companyId);
    }


    public Product create(Product product) {
        resolveKpd(product);
        return productRepository.save(product);
    }

    public Product update(Long id, Product product) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        product.setId(id);
        resolveKpd(product);
        return productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    private void resolveKpd(Product product) {
        if (product.getKpd() == null) {
            return;
        }

        Kpd incoming = product.getKpd();

        // Allow clearing the relation explicitly
        if (incoming.getId() == null && incoming.getCode() == null) {
            product.setKpd(null);
            return;
        }

        if (incoming.getId() != null) {
            Kpd kpd = kpdRepository.findById(incoming.getId())
                    .orElseThrow(() -> new RuntimeException("KPD not found with id: " + incoming.getId()));
            product.setKpd(kpd);
            return;
        }

        if (incoming.getCode() != null) {
            Kpd kpd = kpdRepository.findByCode(incoming.getCode())
                    .orElseThrow(() -> new RuntimeException("KPD not found with code: " + incoming.getCode()));
            product.setKpd(kpd);
            return;
        }

        product.setKpd(null);
    }
}