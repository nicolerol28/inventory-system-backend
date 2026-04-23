package com.miapp.inventory_system.products.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductController — sanitizeFilename")
class ProductControllerFilenameTest {

    // -----------------------------------------------------------------------
    // GROUP 1 — Normal filenames pass through unchanged
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("normal filename with extension is returned unchanged")
    void should_return_unchanged_normal_filename() {
        assertThat(ProductController.sanitizeFilename("product.jpg")).isEqualTo("product.jpg");
    }

    @Test
    @DisplayName("filename with dash and underscore is returned unchanged")
    void should_allow_dash_and_underscore_in_filename() {
        assertThat(ProductController.sanitizeFilename("my-product_01.png")).isEqualTo("my-product_01.png");
    }

    // -----------------------------------------------------------------------
    // GROUP 2 — Path traversal sequences are stripped
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("path traversal with ../../ keeps only the final filename segment")
    void should_strip_path_traversal_and_keep_filename_only() {
        assertThat(ProductController.sanitizeFilename("../../etc/passwd")).isEqualTo("passwd");
    }

    @Test
    @DisplayName("Windows-style path keeps only the final filename segment")
    void should_strip_windows_path_and_keep_filename_only() {
        assertThat(ProductController.sanitizeFilename("C:\\Users\\admin\\secret.txt")).isEqualTo("secret.txt");
    }

    // -----------------------------------------------------------------------
    // GROUP 3 — Special characters are replaced with underscores
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("spaces and angle brackets are replaced with underscores")
    void should_replace_special_characters_with_underscores() {
        assertThat(ProductController.sanitizeFilename("my file@product!.jpg"))
                .isEqualTo("my_file_product_.jpg");
    }

    @Test
    @DisplayName("filename with unicode and accented characters is sanitized")
    void should_replace_unicode_and_symbols_with_underscores() {
        assertThat(ProductController.sanitizeFilename("café.jpg"))
                .isEqualTo("caf_.jpg");
    }

    // -----------------------------------------------------------------------
    // GROUP 4 — Degenerate inputs produce a safe UUID-based fallback
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("null input returns a non-empty UUID-based safe name")
    void should_return_uuid_when_original_is_null() {
        String result = ProductController.sanitizeFilename(null);
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(36); // UUID canonical form
    }

    @Test
    @DisplayName("blank input returns a non-empty UUID-based safe name")
    void should_return_uuid_when_original_is_blank() {
        String result = ProductController.sanitizeFilename("   ");
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(36);
    }

    @Test
    @DisplayName("double-dot-only input returns a UUID-based safe name")
    void should_return_uuid_when_filename_is_only_double_dot() {
        String result = ProductController.sanitizeFilename("..");
        assertThat(result).hasSize(36);
    }
}
