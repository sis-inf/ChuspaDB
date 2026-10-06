/**
 * Módulo principal de la aplicación Estante.
 *
 * <p>Declara las dependencias del JDK Platform Module System (JPMS) necesarias
 * para JavaFX 21, acceso JDBC, serialización JSON con Jackson y logging con SLF4J.
 * Las directivas {@code opens} permiten el acceso por reflexión que requieren
 * JavaFX (via FXML) y Jackson para controllers y modelos respectivamente.</p>
 */
module edu.sisinf.estante {

    // ── Dependencias de plataforma ────────────────────────────────────────────
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires org.postgresql.jdbc;
    requires org.xerial.sqlitejdbc;
    uses java.sql.Driver;

    // ── Dependencias de terceros ──────────────────────────────────────────────
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires org.apache.poi.ooxml;

    // ── Apertura por reflexión (FXML + Jackson) ───────────────────────────────

    /** Permite a javafx.fxml cargar la clase principal de la aplicación. */
    opens edu.sisinf.estante to javafx.fxml;

    /** Permite a javafx.fxml instanciar y conectar los controladores FXML. */
    opens edu.sisinf.estante.controller to javafx.fxml;

    /** Permite a Jackson serializar / deserializar los modelos de dominio. */
    opens edu.sisinf.estante.modelo to com.fasterxml.jackson.databind;

    // ── Exportaciones públicas del módulo ─────────────────────────────────────
    exports edu.sisinf.estante;
    exports edu.sisinf.estante.controller;
    exports edu.sisinf.estante.modelo;
    exports edu.sisinf.estante.servicio;
    exports edu.sisinf.estante.dao;
    exports edu.sisinf.estante.util;
    exports edu.sisinf.estante.core;
    exports edu.sisinf.estante.config;
    exports edu.sisinf.estante.dto;
}
