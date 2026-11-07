# Guía para Contribuidores de ChuspaDB

## Introducción

Esta guía explica paso a paso cómo realizar tu primer Pull Request en el proyecto ChuspaDB utilizando Java, Maven y JavaFX.

---

## 1. Crear un Fork

Desde GitHub, presiona el botón *Fork* en el repositorio principal.

---

## 2. Clonar el repositorio

bash
git clone https://github.com/TU-USUARIO/ChuspaDB.git
cd ChuspaDB


---

## 3. Instalar Java 17 o superior

Verificar instalación:

bash
java -version
javac -version


---

## 4. Instalar Maven 3.8 o superior

Verificar instalación:

bash
mvn -version


---

## 5. Compilar el proyecto

bash
mvn compile


---

## 6. Ejecutar la aplicación JavaFX

bash
mvn javafx:run


---

## 7. Instalar pre-commit

bash
pip install pre-commit
pre-commit install


---

## 8. Crear una rama de trabajo

bash
git checkout dev
git pull origin dev
git checkout -b nombre-de-la-rama


Ejemplo:

bash
git checkout -b docs/guia-primer-pr


---

## 9. Implementar los cambios

Realiza las modificaciones necesarias y guarda los archivos.

---

## 10. Ejecutar pruebas

bash
mvn test


---

## 11. Verificar estilo de código

bash
mvn checkstyle:check


---

## 12. Revisar cambios

bash
git status


---

## 13. Agregar archivos

bash
git add .


---

## 14. Crear commit

Ejemplo:

bash
git commit -m "docs: agregar guia de contribuidor"


---

## 15. Enviar cambios al repositorio

bash
git push origin nombre-de-la-rama


Ejemplo:

bash
git push origin docs/guia-primer-pr


---

## 16. Crear Pull Request

1. Abrir GitHub.
2. Seleccionar la rama enviada.
3. Presionar *Compare & pull request*.
4. Verificar que el destino sea dev.
5. Completar título y descripción.
6. Crear el Pull Request.

---

## Recomendaciones

* Utilizar Conventional Commits.
* Mantener los cambios pequeños y específicos.
* Ejecutar pruebas antes de enviar cambios.
* Verificar que Checkstyle no reporte errores.
* Abrir el Pull Request contra la rama dev.
*
