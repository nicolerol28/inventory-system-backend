package com.miapp.inventory_system.shared.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoResetJob {

    private final JdbcTemplate jdbc;
    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;

    @Scheduled(cron = "0 10 2 * * *")
    public void reset() {
        try {
            log.info("DemoResetJob: Iniciando limpieza total...");

            // 1. Borrar datos en orden de dependencia
            jdbc.execute("DELETE FROM inventory_movements");
            jdbc.execute("DELETE FROM stock");
            jdbc.execute("DELETE FROM products");
            jdbc.execute("DELETE FROM suppliers");
            jdbc.execute("DELETE FROM warehouses");
            jdbc.execute("DELETE FROM categories");
            jdbc.execute("DELETE FROM units");
            jdbc.execute("DELETE FROM users");

            // 2. Reiniciar TODAS las secuencias
            jdbc.execute("ALTER SEQUENCE inventory_movements_id_seq RESTART WITH 1");
            jdbc.execute("ALTER SEQUENCE stock_id_seq RESTART WITH 1");
            jdbc.execute("ALTER SEQUENCE products_id_seq RESTART WITH 1");
            jdbc.execute("ALTER SEQUENCE suppliers_id_seq RESTART WITH 1");
            jdbc.execute("ALTER SEQUENCE warehouses_id_seq RESTART WITH 1");
            jdbc.execute("ALTER SEQUENCE categories_id_seq RESTART WITH 1");
            jdbc.execute("ALTER SEQUENCE units_id_seq RESTART WITH 1");
            jdbc.execute("ALTER SEQUENCE users_id_seq RESTART WITH 1");

            // 3. Cargar la data en orden cronológico de Flyway
            log.info("DemoResetJob: Sembrando datos desde V2...");
            executeSql("classpath:db/migration/V2__seed_data.sql");

            log.info("DemoResetJob: Sembrando productos adicionales V3...");
            executeSql("classpath:db/migration/V3__seed_test_products.sql");

            log.info("DemoResetJob: Sembrando stock adicional V4...");
            executeSql("classpath:db/migration/V4__seed_stock_v3_products.sql");

            log.info("DemoResetJob: Sembrando datos adicionales V5...");
            executeSql("classpath:db/migration/V5__seed_additional_data.sql");

            log.info("DemoResetJob: Sembrando datos adicionales V6...");
            executeSql("classpath:db/migration/V6__seed_inactive_users_categories_units.sql");

            log.info("DemoResetJob: ¡Reinicio completado con éxito!");

        } catch (Exception e) {
            log.error("DemoResetJob: Error crítico durante el reset", e);
        }
    }

    private void executeSql(String location) {
        try (Connection connection = dataSource.getConnection()) {
            Resource resource = resourceLoader.getResource(location);
            ScriptUtils.executeSqlScript(connection, resource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute SQL script: " + location, e);
        }
    }
}