package es.umh.dadm.mistickets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Helper de SQLite que gestiona la creación, actualización y acceso a la tabla
 * de tickets. Extiende {@link SQLiteOpenHelper}.
 *
 *Cada instancia abre (o crea) la base de datos {@value #NOMBRE_BD}.
 */
public class TicketsSQLiteHelper extends SQLiteOpenHelper {

    // Nombre del fichero de base de datos
    private static final String NOMBRE_BD = "BDTickets.db";

    // Versión de la BD
    private static final int VERSION_BD = 2;

    // Nombre de la tabla y sus columnas, igual que en el enunciado
    private static final String TABLA        = "Tickets";
    private static final String COL_ID       = "_id";
    private static final String COL_FOTO     = "foto";
    private static final String COL_CATEGORIA= "categoria";
    private static final String COL_PRECIO   = "precio";
    private static final String COL_FECHA    = "fecha";
    private static final String COL_DES_CORTA= "desCorta";
    private static final String COL_DES_LARGA= "descLarga";
    private static final String COL_LOCALIZ  = "localiz";

    // Sentencia SQL de creación de la tabla, copiada del enunciado
    private static final String SQL_CREATE =
            "CREATE TABLE " + TABLA + " (" +
                    COL_ID        + " integer primary key autoincrement, " +
                    COL_FOTO      + " text, " +
                    COL_CATEGORIA + " int not null, " +
                    COL_PRECIO    + " real not null, " +
                    COL_FECHA     + " integer not null, " +
                    COL_DES_CORTA + " text not null, " +
                    COL_DES_LARGA + " text not null, " +
                    COL_LOCALIZ   + " text)";

    // Array con los nombres de todas las columnas, se usa en las consultas
    private String[] campos = {
            COL_ID, COL_FOTO,COL_CATEGORIA, COL_PRECIO,
            COL_FECHA, COL_DES_CORTA, COL_DES_LARGA, COL_LOCALIZ
    };

    private Cursor cursor;
    private SQLiteDatabase db;

    // Constructor
    public TicketsSQLiteHelper(Context contexto) {
        super(contexto, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    // Borra y crea uno nueva cuando se cambia de version
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA);
        db.execSQL(SQL_CREATE);
    }

    /**
     * Elimina en cascada todos los tickets de una categoría.
     *
     * @param categoriaId id de la categoría cuyos tickets se eliminarán
     */
    public void eliminarPorCategoria(int categoriaId) {
        db = getWritableDatabase();
        db.delete(TABLA, COL_CATEGORIA + "=?",
                new String[]{String.valueOf(categoriaId)});
    }

    // ---------------------------------------------------------------
    // CONSULTAS
    // ---------------------------------------------------------------

    /**
     * Devuelve todos los tickets ordenados del más reciente al más antiguo.
     * Equivale a: {@code SELECT * FROM Tickets ORDER BY fecha DESC}
     *
     * @return lista de tickets; vacía si la tabla no tiene filas
     */
    public ArrayList<Ticket> obtenerTodos() {
        db = getWritableDatabase();
        cursor = db.query(TABLA, campos, null, null, null, null, COL_FECHA + " DESC");

        ArrayList<Ticket> lista = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                lista.add(cursorATicket());
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    /**
     * Devuelve los tickets de una categoría concreta, del más reciente al más antiguo.
     * Equivale a: {@code SELECT * FROM Tickets WHERE categoria = ? ORDER BY fecha DESC}
     *
     * @param categoriaId id de la categoría por la que filtrar
     * @return lista de tickets de esa categoría; vacía si no hay ninguno
     */
    public ArrayList<Ticket> obtenerPorCategoria(int categoriaId) {
        db = getWritableDatabase();
        cursor = db.query(
                TABLA,
                campos,
                COL_CATEGORIA + "=?",              // cláusula WHERE
                new String[]{String.valueOf(categoriaId)}, // valor del WHERE
                null, null,
                COL_FECHA + " DESC");

        ArrayList<Ticket> lista = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                lista.add(cursorATicket());
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    // ---------------------------------------------------------------
    // INSERTAR
    // ---------------------------------------------------------------

    /**
     * Inserta un ticket nuevo en la tabla. El campo {@code _id} se ignora porque
     * SQLite asigna el id automáticamente.
     *
     * @param t ticket a insertar (su campo id no se usa)
     */
    public void insertar(Ticket t) {
        db = getWritableDatabase();
        db.insert(TABLA, null, ticketAContentValues(t));
    }

    // ---------------------------------------------------------------
    // ACTUALIZAR
    // ---------------------------------------------------------------

    /**
     * Actualiza todos los campos de un ticket existente buscándolo por su {@code _id}.
     * Equivale a: {@code UPDATE Tickets SET ... WHERE _id = ?}
     *
     * @param t ticket con los nuevos valores
     */
    public void actualizar(Ticket t) {
        db = getWritableDatabase();
        db.update(
                TABLA,
                ticketAContentValues(t),
                COL_ID + "=?",
                new String[]{String.valueOf(t.getId())});
    }

    // ---------------------------------------------------------------
    // ELIMINAR
    // ---------------------------------------------------------------

    /**
     * Elimina un ticket de la tabla por su id.
     * Equivale a: {@code DELETE FROM Tickets WHERE _id = ?}
     *
     * @param id identificador del ticket a eliminar
     */
    public void eliminar(int id) {
        db = getWritableDatabase();
        db.delete(TABLA, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // ---------------------------------------------------------------
    // MÉTODOS PRIVADOS AUXILIARES
    // ---------------------------------------------------------------

    // Lee la fila actual del cursor y construye un objeto Ticket.
    // Los índices (0, 1, 2...) corresponden al orden del array "campos".
    private Ticket cursorATicket() {
        Ticket t = new Ticket();
        t.setId(cursor.getInt(0));
        t.setFoto(cursor.getString(1));
        t.setCategoriaId(cursor.getInt(2));
        t.setPrecio(cursor.getDouble(3));
        t.setFecha(cursor.getLong(4));
        t.setDescCorta(cursor.getString(5));
        t.setDescLarga(cursor.getString(6));
        t.setLocaliz(cursor.getString(7));
        return t;
    }

    // Convierte un objeto Ticket en ContentValues para poder
    // pasarlo a insert() o update().
    private ContentValues ticketAContentValues(Ticket t) {
        ContentValues cv = new ContentValues();
        cv.put(COL_FOTO, t.getFoto());
        cv.put(COL_CATEGORIA, t.getCategoriaId());
        cv.put(COL_PRECIO,    t.getPrecio());
        cv.put(COL_FECHA,     t.getFecha());
        cv.put(COL_DES_CORTA, t.getDescCorta());
        cv.put(COL_DES_LARGA, t.getDescLarga());
        cv.put(COL_LOCALIZ,   t.getLocaliz());
        return cv;
    }
}
