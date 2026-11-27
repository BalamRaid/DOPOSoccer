package doposoccer.infrastructure;

import doposoccer.domain.DOPOSoccerException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

/**
 * Persistencia en archivos: guarda y abre objetos serializables, como una
 * partida en curso o el progreso de los jugadores.
 *
 * <p>Pertenece a infraestructura porque conoce el sistema de archivos. Los
 * problemas de lectura y escritura se traducen a
 * {@link DOPOSoccerException} con un mensaje claro para el usuario, y el
 * detalle tecnico queda en el log de errores para los programadores.</p>
 */
public final class GameStore {

    /** Extension de los archivos de partida. */
    public static final String GAME_EXTENSION = "dopo";

    /** Archivo donde se conserva el progreso de los jugadores. */
    public static final String PROGRESS_FILE = "doposoccer-progress.dat";

    /**
     * Constructor privado: la clase solo expone metodos estaticos.
     */
    private GameStore() {
    }

    /**
     * Guarda un objeto en el archivo indicado, reemplazando su contenido.
     *
     * @param data objeto que se guarda
     * @param file archivo de destino
     * @throws DOPOSoccerException si faltan datos o no se puede escribir
     */
    public static void save(Serializable data, File file) throws DOPOSoccerException {
        if (data == null || file == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NULL_ELEMENT);
        }
        try (FileOutputStream raw = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(raw))) {
            out.writeObject(data);
        } catch (IOException e) {
            ErrorLog.record(e);
            throw new DOPOSoccerException(DOPOSoccerException.SAVE_FAILED
                    + " (" + file.getName() + ": " + e.getMessage() + ")");
        }
    }

    /**
     * Abre un objeto guardado y comprueba que sea del tipo esperado.
     *
     * @param file archivo de origen
     * @param type tipo que se espera leer
     * @param <T> tipo del objeto
     * @return objeto leido
     * @throws DOPOSoccerException si el archivo no existe, no se puede leer o
     *         no contiene un objeto del tipo esperado
     */
    public static <T> T open(File file, Class<T> type) throws DOPOSoccerException {
        if (file == null || type == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NULL_ELEMENT);
        }
        if (!file.isFile()) {
            throw new DOPOSoccerException(DOPOSoccerException.OPEN_FAILED
                    + " El archivo no existe: " + file.getName());
        }
        // El archivo se abre como recurso propio: si el constructor de
        // ObjectInputStream falla (archivo danado) igual queda cerrado.
        try (FileInputStream raw = new FileInputStream(file);
             ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(raw))) {
            Object data = in.readObject();
            if (!type.isInstance(data)) {
                throw new DOPOSoccerException(DOPOSoccerException.INVALID_SAVE_FILE);
            }
            return type.cast(data);
        } catch (StreamCorruptedException | InvalidClassException | InvalidObjectException
                 | ClassNotFoundException | ClassCastException e) {
            ErrorLog.record(e);
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_SAVE_FILE);
        } catch (IOException e) {
            ErrorLog.record(e);
            throw new DOPOSoccerException(DOPOSoccerException.OPEN_FAILED
                    + " (" + file.getName() + ": " + e.getMessage() + ")");
        }
    }
}
