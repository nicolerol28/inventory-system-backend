package com.miapp.inventory_system.products.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Getter
public class Product {

    private Long id;
    private String name;
    private String description;
    private String sku;
    private Long unitId;
    private Long categoryId;
    private Long supplierId;
    private Optional<BigDecimal> purchasePrice;
    private Optional<BigDecimal> salePrice;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imageUrl;

    private Product() {}

    public static Product create(
            String name,
            String description,
            String sku,
            Long unitId,
            Long categoryId,
            Long supplierId,
            Optional<BigDecimal> purchasePrice,
            Optional<BigDecimal> salePrice) {

        validate(name, sku, unitId, categoryId, purchasePrice, salePrice);

        Product product = new Product();
        product.name          = name;
        product.description   = description;
        product.sku           = sku;
        product.unitId        = unitId;
        product.categoryId    = categoryId;
        product.supplierId    = supplierId;
        product.purchasePrice = purchasePrice;
        product.salePrice     = salePrice;
        product.active        = true;
        product.createdAt     = LocalDateTime.now();
        product.updatedAt  = LocalDateTime.now();

        return product;
    }

    public static Product reconstitute(
            Long id,
            String name,
            String description,
            String sku,
            Long unitId,
            Long categoryId,
            Long supplierId,
            Optional<BigDecimal> purchasePrice,
            Optional<BigDecimal> salePrice,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String imageUrl) {

        Product product = new Product();
        product.id          = id;
        product.name        = name;
        product.description = description;
        product.sku         = sku;
        product.unitId      = unitId;
        product.categoryId  = categoryId;
        product.supplierId  = supplierId;
        product.purchasePrice = purchasePrice;
        product.salePrice     = salePrice;
        product.active      = active;
        product.createdAt   = createdAt;
        product.updatedAt   = updatedAt;
        product.imageUrl    = imageUrl;

        return product;
    }

    public void update(
            String name,
            String description,
            String sku,
            Long unitId,
            Long categoryId,
            Long supplierId,
            Optional<BigDecimal> purchasePrice,
            Optional<BigDecimal> salePrice) {

        validate(name, sku, unitId, categoryId, purchasePrice, salePrice);

        this.name          = name;
        this.description   = description;
        this.sku           = sku;
        this.unitId        = unitId;
        this.categoryId    = categoryId;
        this.supplierId    = supplierId;
        this.purchasePrice = purchasePrice;
        this.salePrice     = salePrice;
        this.updatedAt     = LocalDateTime.now();
    }

    public void updateImage(String imageUrl) {
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalArgumentException(
                    "El producto ya está desactivado");
        }
        this.active = false;
    }

    private static void validate(
            String name,
            String sku,
            Long unitId,
            Long categoryId,
            Optional<BigDecimal> purchasePrice,
            Optional<BigDecimal> salePrice) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("El SKU es obligatorio");
        }
        if (unitId == null) {
            throw new IllegalArgumentException("La unidad es obligatoria");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }

        purchasePrice.ifPresent(p -> {
            if (p.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "El precio de compra debe ser mayor a cero");
            }
        });

        salePrice.ifPresent(p -> {
            if (p.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "El precio de venta debe ser mayor a cero");
            }
        });
    }

}
