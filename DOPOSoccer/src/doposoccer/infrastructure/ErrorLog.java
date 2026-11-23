package doposoccer.infrastructure;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Log de errores para los programadores. Cada error se agrega al archivo
 * doposoccer-errors.log con la fecha y la traza completa.
 *
 * <p>Esta clase pertenece a infraestructura: conoce el sistema de archivos y
 * esta pensada para errores inesperados, no para errores de validacion que ya
 * se muestran al usuario mediante {@link doposoccer.domain.DOPOSoccerException}.</p>
 */
public final class ErrorLog {

    /** Nombre del archivo donde se acumulan los errores. */
    public static final String FILE_NAME = "doposoccer-errors.log";

    /**
     * Constructor privado: la clase solo expone metodos estaticos.
     */
    private ErrorLog() {
    }

    /**
     * Registra una excepcion en el archivo de log; nunca lanza excepciones.
     * Si el archivo no se puede escribir, el problema se informa por la
     * salida de error estandar.
     *
     * @param error excepcion que se desea registrar
     */
    public static void record(Throwable error) {
        if (error == null) {
            return;
        }
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            out.println("[" + LocalDateTime.now() + "] " + error);
            error.printStackTrace(out);
            out.println();
        } catch (IOException e) {
            System.err.println("No se pudo escribir el log: " + e.getMessage());
        }
    }
}
