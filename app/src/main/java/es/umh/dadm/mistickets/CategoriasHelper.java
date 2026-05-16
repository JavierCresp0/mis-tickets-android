package es.umh.dadm.mistickets;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Utilidad estática para persistir la lista de categorías en el almacenamiento
 * interno del dispositivo como JSON (fichero {@value #FICHERO}).
 *
 */
public class CategoriasHelper {

    private static final String TAG = "CategoriasHelper";
    private static final String FICHERO = "categorias.json";

    /**
     * Serializa la lista completa de categorías a JSON y la sobreescribe en el
     * fichero interno.
     *
     * @param ctx  contexto necesario para acceder al almacenamiento interno
     * @param lista lista de categorías a persistir
     */
    public static void guardar(Context ctx, ArrayList<Categoria> lista) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(lista);  // serialización igual que en clase

            OutputStreamWriter fout = new OutputStreamWriter(
                    ctx.openFileOutput(FICHERO, Context.MODE_PRIVATE));
            fout.write(json);
            fout.close();
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar: " + e.getMessage());
        }
    }

    /**
     * Deserializa la lista de categorías desde el fichero interno.
     *
     * @param ctx contexto necesario para acceder al almacenamiento interno
     * @return lista de categorías, o lista vacía si el fichero aún no existe
     */
    public static ArrayList<Categoria> cargar(Context ctx) {
        try {
            BufferedReader fin = new BufferedReader(
                    new InputStreamReader(ctx.openFileInput(FICHERO)));

            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = fin.readLine()) != null) {
                sb.append(linea);
            }
            fin.close();

            // Deserialización: de JSON a ArrayList<Categoria>
            Gson gson = new Gson();
            Type tipo = new TypeToken<ArrayList<Categoria>>(){}.getType();
            ArrayList<Categoria> lista = gson.fromJson(sb.toString(), tipo);

            return (lista != null) ? lista : new ArrayList<>();

        } catch (Exception e) {
            // Si el fichero no existe todavía, devolvemos lista vacía
            Log.e(TAG, "Error al cargar: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Genera un id único sumando 1 al mayor id existente en la lista.
     * Si la lista está vacía devuelve 1. No reutiliza ids de categorías eliminadas,
     * lo que evita colisiones con tickets huérfanos que aún referencien ese id.
     *
     * @param lista lista actual de categorías
     * @return nuevo id mayor que cualquier id existente
     */
    public static int generarId(ArrayList<Categoria> lista) {
        int maxId = 0;
        for (Categoria c : lista) {
            if (c.getId() > maxId) maxId = c.getId();
        }
        return maxId + 1;
    }
}