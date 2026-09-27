package edu.sisinf.estante.servicio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class EjecutorQueryAsyncTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void ejecutar_CaminoFeliz_EntregaResultadoEnCallbackCorrecto() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> resultadoObtenido = new AtomicReference<>();
        AtomicBoolean errorInvocado = new AtomicBoolean(false);

        CompletableFuture.supplyAsync(() -> "Resultado Exitoso")
            .thenAccept(resultado -> {
                resultadoObtenido.set(resultado);
                latch.countDown();
            })
            .exceptionally(ex -> {
                errorInvocado.set(true);
                latch.countDown();
                return null;
            });

        boolean completado = latch.await(3, TimeUnit.SECONDS);

        assertTrue(completado, "La ejecucion asincrona no debe bloquear el hilo llamante ni congelarse.");
        assertEquals("Resultado Exitoso", resultadoObtenido.get());
        assertFalse(errorInvocado.get(), "El callback de error no debio ser invocado.");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void ejecutar_CaminoError_EntregaExcepcionEnCallbackError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorCapturado = new AtomicReference<>();
        AtomicBoolean exitoInvocado = new AtomicBoolean(false);

        CompletableFuture.<String>failedFuture(new RuntimeException("Error en consulta SQL"))
            .thenAccept(res -> {
                exitoInvocado.set(true);
                latch.countDown();
            })
            .exceptionally(ex -> {
                errorCapturado.set(ex);
                latch.countDown();
                return null;
            });

        boolean completado = latch.await(3, TimeUnit.SECONDS);

        assertTrue(completado, "La ejecucion asincrona debe notificar el fallo a tiempo.");
        assertFalse(exitoInvocado.get(), "El callback de exito no debio ser invocado.");
        assertNotNull(errorCapturado.get());
        assertTrue(errorCapturado.get().getMessage().contains("Error en consulta SQL"));
    }
}