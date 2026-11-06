/**
 * Módulo principal de la aplicación ChuspaDB.
 *
 * <p>Declara las dependencias del JDK Platform Module System (JPMS) necesarias
 * para JavaFX 21, acceso JDBC, serialización JSON con Jackson y logging con SLF4J.
 * Las directivas {@code opens} permiten el acceso por reflexión que requieren
 * JavaFX (via FXML) y Jackson para controllers y modelos respectivamente.</p>
 */
module edu.sisinf.chuspadb {

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
    opens edu.sisinf.chuspadb to javafx.fxml;

    /** Permite a javafx.fxml instanciar y conectar los controladores FXML. */
    opens edu.sisinf.chuspadb.controller to javafx.fxml;

    /** Permite a Jackson serializar / deserializar los modelos de dominio. */
    opens edu.sisinf.chuspadb.modelo to com.fasterxml.jackson.databind;

    // ── Exportaciones públicas del módulo ─────────────────────────────────────
    exports edu.sisinf.chuspadb;
    exports edu.sisinf.chuspadb.controller;
    exports edu.sisinf.chuspadb.modelo;
    exports edu.sisinf.chuspadb.servicio;
    exports edu.sisinf.chuspadb.dao;
    exports edu.sisinf.chuspadb.util;
    exports edu.sisinf.chuspadb.core;
    exports edu.sisinf.chuspadb.config;
    exports edu.sisinf.chuspadb.dto;
}
