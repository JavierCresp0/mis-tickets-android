package es.umh.dadm.mistickets;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import es.umh.dadm.mistickets.databinding.ActivityListaCategoriasBinding;

import java.util.ArrayList;

/**
 * Muestra la lista de categorías persistidas en JSON.
 * Clic corto sobre una categoría abre {@link TicketsPorCategoriaActivity} con sus tickets.
 * Clic largo muestra un menú contextual para editar o eliminar la categoría;
 * al eliminar, se borran también todos sus tickets en SQLite.
 */
public class ListaCategoriasActivity extends AppCompatActivity {

    /**
     * Clave del extra que transporta la posición (índice en la lista) de la categoría
     * al formulario de edición. Se usa la posición en lugar del id porque la lista
     * es el origen de verdad durante la sesión.
     */
    public static final String PARAM_CATEGORIA_POS = "CATEGORIA_POS";

    private ArrayList<Categoria> listaCategorias;
    private ArrayAdapter<Categoria> adapter;
    private ActivityListaCategoriasBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListaCategoriasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Botón para abrir el formulario de nueva categoría
        binding.btnNuevaCategoria.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormularioCategoriaActivity.class);
            startActivity(intent);
        });

        // Clic corto sobre un elemento: ir a ver los tickets de esa categoría
        binding.listViewCategorias.setOnItemClickListener((parent, view, position, id) -> {
            Categoria c = listaCategorias.get(position);
            Intent intent = new Intent(this, TicketsPorCategoriaActivity.class);
            // Pasamos el id y el nombre de la categoría a la siguiente pantalla
            intent.putExtra(TicketsPorCategoriaActivity.PARAM_CATEGORIA_ID, c.getId());
            intent.putExtra(TicketsPorCategoriaActivity.PARAM_CATEGORIA_NOMBRE, c.getDescCorta());
            startActivity(intent);
        });

        // Clic largo sobre un elemento: menú para editar o eliminar
        binding.listViewCategorias.setOnItemLongClickListener((parent, view, position, id) -> {
            mostrarMenuContextual(position);
            return true; // devolver true indica que hemos consumido el evento
        });
    }

    // onResume se llama cada vez que esta pantalla vuelve a ser visible,
    // Así la lista siempre se refresca con los datos más recientes.
    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    private void cargarLista() {
        listaCategorias = CategoriasHelper.cargar(this);

        if (listaCategorias.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_hay_categorias), Toast.LENGTH_SHORT).show();
        }

        // ArrayAdapter usa el toString() de Categoria para mostrar cada fila,
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, listaCategorias);
        binding.listViewCategorias.setAdapter(adapter);
    }

    private void mostrarMenuContextual(int position) {
        // AlertDialog con dos opciones: editar o eliminar
        String[] opciones = {getString(R.string.editar), getString(R.string.eliminar)};
        new AlertDialog.Builder(this)
                .setTitle(listaCategorias.get(position).getDescCorta())
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        // Editar: abrimos el formulario pasando la posición
                        Intent intent = new Intent(this, FormularioCategoriaActivity.class);
                        intent.putExtra(PARAM_CATEGORIA_POS, position);
                        startActivity(intent);
                    } else {
                        // Eliminar: pedimos confirmación antes de borrar
                        confirmarEliminar(position);
                    }
                })
                .show();
    }

    private void confirmarEliminar(int position) {
        Categoria c = listaCategorias.get(position);
        // AlertDialog de confirmación, como indica el enunciado:
        // "ante una opción de borrado, preguntar al usuario"
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.eliminar))
                .setMessage(getString(R.string.confirmar_eliminar_categoria, c.getDescCorta()))
                .setPositiveButton(getString(R.string.si), (dialog, which) -> {
                    // Primero eliminamos todos los tickets de esa categoría en SQLite
                    TicketsSQLiteHelper dbHelper = new TicketsSQLiteHelper(this);
                    dbHelper.eliminarPorCategoria(c.getId());

                    // Luego eliminamos la categoría del fichero
                    listaCategorias.remove(position);
                    CategoriasHelper.guardar(this, listaCategorias);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, getString(R.string.categoria_eliminada), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}