#!/bin/bash

echo "Iniciando verificación de conformidad con estructura Maven..."
ERROR=0

ARCHIVOS_JAVA=$(find . -type f -name "*.java" ! -path "./src/main/java/*" ! -path "./src/test/java/*" ! -path "./scripts/*" ! -path "./target/*")

if [ ! -z "$ARCHIVOS_JAVA" ]; then
    echo "[ERROR] Archivos .java detectados en rutas no permitidas:"
    echo "$ARCHIVOS_JAVA"
    ERROR=1
fi

ARCHIVOS_FXML=$(find . -type f -name "*.fxml" ! -path "./src/main/resources/*" ! -path "./target/*")

if [ ! -z "$ARCHIVOS_FXML" ]; then
    echo "[ERROR] Archivos .fxml detectados en rutas no permitidas:"
    echo "$ARCHIVOS_FXML"
    ERROR=1
fi

PAQUETES_TEST=$(find src/test/java -type d | sed 's#src/test/java/##' | grep -v "^$")

for PAQUETE in $PAQUETES_TEST; do
    if [ ! -d "src/main/java/$PAQUETE" ]; then
        echo "[ERROR] Paquete de test sin correspondencia en main: $PAQUETE"
        ERROR=1
    fi
done


if [ $ERROR -eq 1 ]; then
    echo "[FALLO] La estructura del proyecto no cumple con el estándar requerido."
    exit 1
else
    echo "✅ Validación de estructura Maven exitosa."
    exit 0
fi
