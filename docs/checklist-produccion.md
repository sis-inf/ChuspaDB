# Checklist antes de usar ChuspaDB con bases de datos de producción

## Objetivo

Antes de conectarse a una base de datos de producción, es recomendable verificar que la configuración del entorno minimice el riesgo de modificaciones accidentales y facilite la auditoría de las operaciones realizadas.

## Lista de verificación

- [ ] Configuré un color de conexión diferente para distinguir visualmente el entorno de producción.
- [ ] Confirmé que estoy conectado a la base de datos correcta.
- [ ] Activé el modo de solo lectura si no necesito realizar modificaciones.
- [ ] Verifiqué que la confirmación de operaciones DML está habilitada.
- [ ] Revisé cuidadosamente las consultas antes de ejecutarlas.
- [ ] Configuré la auditoría de operaciones si es requerida por las políticas de cumplimiento.
- [ ] Confirmé que tengo los permisos mínimos necesarios para realizar mi trabajo.
- [ ] Realicé una copia de seguridad cuando la operación lo requiere.

## Recomendaciones

- Evita ejecutar operaciones de escritura innecesarias sobre producción.
- Verifica siempre el nombre del servidor y la base de datos antes de ejecutar consultas.
- Utiliza cuentas con permisos limitados cuando sea posible.
- Mantén habilitadas las opciones de confirmación para operaciones destructivas.

## Conclusión

Seguir esta lista de verificación ayuda a reducir errores humanos y mejora la seguridad al trabajar con bases de datos de producción.
