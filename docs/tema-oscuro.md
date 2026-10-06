# Tema oscuro y tema claro

**Tema oscuro: no implementado.** Actualmente la aplicación solo cuenta con el tema claro. El tema oscuro descrito en este documento es una funcionalidad planificada, no disponible hoy. Ver [roadmap.md](roadmap.md).

## Descripción

La aplicación utiliza actualmente un tema claro fijo para la interfaz. Se planea en el futuro permitir alternar a un tema oscuro según las preferencias del usuario.

## Tema claro (actual)

El tema claro presenta una interfaz con fondos claros y texto oscuro, ofreciendo una apariencia limpia y adecuada para entornos con buena iluminación.

**Representación en texto:**

```text
┌─────────────────────────────┐
│ Barra superior (clara)      │
├─────────────────────────────┤
│ Fondo blanco                │
│ Texto oscuro                │
│ Botones claros              │
└─────────────────────────────┘
```

## Tema oscuro (planificado)

El tema oscuro utilizaría fondos oscuros y texto claro para reducir el brillo de la pantalla y mejorar la comodidad visual en ambientes con poca iluminación. Esta funcionalidad aún no ha sido implementada.

**Representación en texto (diseño planeado):**

```text
┌─────────────────────────────┐
│ Barra superior (oscura)     │
├─────────────────────────────┤
│ Fondo oscuro                │
│ Texto claro                 │
│ Botones oscuros             │
└─────────────────────────────┘
```

## Notas

Por el momento la aplicación no ofrece un selector de tema. Esta opción se agregará cuando el tema oscuro esté implementado.