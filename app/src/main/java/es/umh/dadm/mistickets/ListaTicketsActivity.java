package es.umh.dadm.mistickets;


import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import es.umh.dadm.mistickets.databinding.ActivityListaTicketsBinding;

import java.util.ArrayList;

/**
 * Muestra todos los tickets almacenados en SQLite, ordenados del más reciente al más antiguo.
 * Permite crear un ticket nuevo, editarlo (clic corto) o eliminarlo (clic largo).
 * La lista se recarga en {@code onResume} para reflejar los cambios hechos en el formulario.
 */
public class ListaTicketsActivity extends AppCompatActivity {

    /** Clave del extra que transporta el id del ticket al formulario de edición. */
    public static final String PARAM_TICKET_ID = "TICKET_ID";

    private TicketsSQLiteHelper dbHelper;
    private ArrayList<Ticket> listaTickets;
    private ArrayAdapter<Ticket> adapter;
    private ActivityListaTicketsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListaTicketsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Creamos el helper de SQLite pasándole el contexto
        dbHelper = new TicketsSQLiteHelper(this);

        // Botón para abrir el formulario de nuevo ticket
        binding.btnNuevoTicket.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormularioTicketActivity.class);
            startActivity(intent);
        });

        // Clic corto: editar el ticket pulsado
        // Pasamos el id del ticket para que el formulario lo cargue
        binding.listViewTickets.setOnItemClickListener((parent, view, position, id) -> {
            Ticket t = listaTickets.get(position);
            Intent intent = new Intent(this, FormularioTicketActivity.class);
            intent.putExtra(PARAM_TICKET_ID, t.getId());
            startActivity(intent);
        });

        // Clic largo: eliminar con confirmación
        binding.listViewTickets.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmarEliminar(position);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    private void cargarLista() {
        // obtenerTodos() consulta la BD y devuelve la lista ordenada por fecha
        listaTickets = dbHelper.obtenerTodos();

        if (listaTickets.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_hay_tickets), Toast.LENGTH_SHORT).show();
        }

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, listaTickets);
        binding.listViewTickets.setAdapter(adapter);
    }

    private void confirmarEliminar(int position) {
        Ticket t = listaTickets.get(position);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.eliminar))
                .setMessage(getString(R.string.confirmar_eliminar, t.getDescCorta()))
                .setPositiveButton(getString(R.string.si), (dialog, which) -> {
                    // Eliminamos de la BD usando el id del ticket
                    dbHelper.eliminar(t.getId());
                    // Eliminamos también de la lista en memoria y refrescamos
                    listaTickets.remove(position);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, getString(R.string.ticket_eliminado), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}