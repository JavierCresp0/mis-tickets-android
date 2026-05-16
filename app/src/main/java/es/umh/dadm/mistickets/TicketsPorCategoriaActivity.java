package es.umh.dadm.mistickets;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import es.umh.dadm.mistickets.databinding.ActivityTicketsPorCategoriaBinding;
import java.util.ArrayList;

/**
 * Muestra los tickets de una categoría concreta, filtrados desde SQLite.
 * Recibe el id y el nombre de la categoría por Intent; el nombre se usa como
 * título de la pantalla y el id para la consulta a la base de datos.
 */
public class TicketsPorCategoriaActivity extends AppCompatActivity {

    /** Id de la categoría por la que se filtra la consulta a SQLite. */
    public static final String PARAM_CATEGORIA_ID = "CATEGORIA_ID";
    /** Nombre de la categoría; se muestra como título en la barra superior. */
    public static final String PARAM_CATEGORIA_NOMBRE = "CATEGORIA_NOMBRE";

    private TicketsSQLiteHelper dbHelper;
    private ArrayList<Ticket> listaTickets;
    private ArrayAdapter<Ticket> adapter;
    private ActivityTicketsPorCategoriaBinding binding;
    private int categoriaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketsPorCategoriaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        categoriaId = getIntent().getIntExtra(PARAM_CATEGORIA_ID, -1);
        String categoriaNombre = getIntent().getStringExtra(PARAM_CATEGORIA_NOMBRE);

        // setTitle cambia el texto que aparece en la barra superior de la app,
        setTitle(categoriaNombre);

        dbHelper = new TicketsSQLiteHelper(this);

        // Clic corto sobre un ticket: abre el formulario de edición
        // pasando el id del ticket seleccionado.
        binding.listViewTicketsCategoria.setOnItemClickListener((parent, view, position, id) -> {
            Ticket t = listaTickets.get(position);
            Intent intent = new Intent(this, FormularioTicketActivity.class);
            intent.putExtra(ListaTicketsActivity.PARAM_TICKET_ID, t.getId());
            startActivity(intent);
        });

        // Clic largo sobre un ticket: muestra diálogo de confirmación para eliminar.
        binding.listViewTicketsCategoria.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmarEliminar(position);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    // Consulta la BD filtrando por categoría y conecta el resultado al ListView.
    private void cargarLista() {
        listaTickets = dbHelper.obtenerPorCategoria(categoriaId);

        if (listaTickets.isEmpty()) {
            Toast.makeText(this,
                    getString(R.string.no_hay_tickets_categoria), Toast.LENGTH_SHORT).show();
        }

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, listaTickets);
        binding.listViewTicketsCategoria.setAdapter(adapter);
    }

    // Muestra un AlertDialog pidiendo confirmación antes de borrar
    private void confirmarEliminar(int position) {
        Ticket t = listaTickets.get(position);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.eliminar))
                .setMessage(getString(R.string.confirmar_eliminar, t.getDescCorta()))
                .setPositiveButton(getString(R.string.si), (dialog, which) -> {
                    dbHelper.eliminar(t.getId());
                    listaTickets.remove(position);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, getString(R.string.ticket_eliminado), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}