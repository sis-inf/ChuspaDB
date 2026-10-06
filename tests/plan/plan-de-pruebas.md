# Plan de Pruebas

## 1. Objetivo
Asegurar la calidad y estabilidad del sistema mediante la detección temprana de defectos, garantizando que todas las funcionalidades cumplan con los requisitos técnicos y de usuario antes de su integración en la rama principal.

## 2. Alcance
### En alcance
- Pruebas de las funcionalidades principales definidas en los casos de uso.
- Verificación de la integridad de los datos y flujo de información.
- Validación de la interfaz de usuario y usabilidad básica.
- Pruebas de regresión tras la integración de nuevos módulos.

### Fuera de alcance
- Pruebas de carga masiva y estrés extremo.
- Compatibilidad con versiones de sistemas operativos obsoletos (anteriores a Windows 10).
- Pruebas de penetración de seguridad avanzada (Ethical Hacking).

## 3. Tipos de prueba
- [x] Unitarias
- [x] Integración
- [x] Funcionales manuales
- [ ] Rendimiento
- [ ] Seguridad
- [x] Regresión

## 4. Entornos

| Entorno | SO | Versión |
|---|---|---|
| Local | Windows 10/11 | Python 3.11 / Node.js 18+ |
| CI | Ubuntu latest | GitHub Actions / Docker |

## 5. Responsables

| Rol | Responsable |
|---|---|
| Diseño de casos | RODRIGO BALTAZAR BORRAS |
| Ejecución manual | RODRIGO BALTAZAR BORRAS |
| Automatización | Equipo de Desarrollo / DevOps |
| Reporte | RODRIGO BALTAZAR BORRAS |

## 6. Criterios de salida
- [x] Cobertura mínima de 80% en pruebas unitarias.
- [x] Cero bugs críticos o de severidad "Bloqueante" abiertos.
- [x] Todos los casos de prueba manuales ejecutados y aprobados.

## 7. Riesgos

| Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|
| Retrasos en el entorno de CI | Media | Alto | Ejecución de pruebas en local antes del push. |
| Inconsistencia de datos en local | Baja | Medio | Uso de scripts de limpieza de base de datos. |
| Cambios de último minuto en requisitos | Alta | Muy Alto | Aplicar metodologías ágiles y comunicación constante. |