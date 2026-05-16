package es.umh.dadm.mistickets;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import es.umh.dadm.mistickets.databinding.ActivityMainBinding;

/**
 * Pantalla de inicio de la aplicación. Muestra dos botones para navegar a la
 * lista de tickets ({@link ListaTicketsActivity}) y a la lista de categorías
 * ({@link ListaCategoriasActivity}).
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnTickets.setOnClickListener(this);
        binding.btnCategorias.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnTickets) {
            startActivity(new Intent(this, ListaTicketsActivity.class));
        } else if (v.getId() == R.id.btnCategorias) {
            startActivity(new Intent(this, ListaCategoriasActivity.class));
        }
    }
}