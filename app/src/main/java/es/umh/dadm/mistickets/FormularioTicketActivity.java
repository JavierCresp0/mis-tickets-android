package es.umh.dadm.mistickets;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import es.umh.dadm.mistickets.databinding.ActivityFormularioTicketBinding;

/**
 * Formulario de alta y edición de tickets. Opera en dos modos según el extra recibido:
 *   Alta: no se pasa {@link ListaTicketsActivity#PARAM_TICKET_ID}; {@code idEditar} vale -1.
 *   Edición: se recibe el id del ticket; los campos se rellenan con sus datos actuales.
 * La foto se puede capturar con la cámara o seleccionar desde la galería .
 */
public class FormularioTicketActivity extends AppCompatActivity {

    /** Código de solicitud para la actividad de captura de cámara. */
    private static final int CAPTURAR_IMAGEN = 1;
    /** Código de solicitud para el selector de imágenes de la galería. */
    private static final int SELECCIONAR_IMAGEN_GALERIA = 2;
    private ActivityFormularioTicketBinding binding;
    /** Ruta absoluta o URI en String de la foto seleccionada; vacía si no hay foto. */
    private String rutaFotoActual = "";

    private TicketsSQLiteHelper dbHelper;
    private ArrayList<Categoria> listaCategorias;

    // -1 significa alta nueva. Si tiene otro valor, es el id del ticket a editar.
    private int idEditar = -1;
    private Ticket ticketEditar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFormularioTicketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new TicketsSQLiteHelper(this);

        listaCategorias = CategoriasHelper.cargar(this);

        if (listaCategorias.isEmpty()) {
            Toast.makeText(this,
                    getString(R.string.crea_primero_categoria), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ArrayAdapter<Categoria> adapterSpinner = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, listaCategorias);
        adapterSpinner.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        binding.spCategoriaTicket.setAdapter(adapterSpinner);

        // Botón cámara: abre la cámara y guarda la foto en un fichero.
        binding.btnCamaraTicket.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;
            try {
                photoFile = crearFicheroImagen();
            } catch (IOException e) {
                Log.e("FormularioTicket", "Error al crear fichero: " + e.getMessage());
                Toast.makeText(this, getString(R.string.error_abrir_camara),
                        Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "es.umh.dadm.mistickets.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURAR_IMAGEN);
            }
        });

        // Botón galería: abre el selector de imágenes del dispositivo.
        binding.btnGaleriaTicket.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.seleccionar_imagen)),
                    SELECCIONAR_IMAGEN_GALERIA);
        });

        idEditar = getIntent().getIntExtra(
                ListaTicketsActivity.PARAM_TICKET_ID, -1);

        if (idEditar != -1) {
            cargarTicketParaEditar();
            setTitle(R.string.titulo_editar_ticket);
        } else {
            setTitle(R.string.titulo_nuevo_ticket);
        }

        binding.btnGuardarTicket.setOnClickListener(v -> guardar());
    }

    /**
     * Crea un fichero vacío en el directorio externo privado de imágenes de la app.
     * La ruta del fichero se guarda en {@link #rutaFotoActual} antes de lanzar
     * la cámara, porque el Intent no devuelve la ruta: solo confirma el resultado.
     *
     * @return fichero listo para que la cámara escriba en él
     * @throws IOException si no se puede crear el fichero
     */
    private File crearFicheroImagen() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreFichero = "TICKET_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(nombreFichero, ".jpg", storageDir);
        rutaFotoActual = imagen.getAbsolutePath();
        return imagen;
    }

    // Se llama automáticamente cuando la cámara o la galería devuelven el resultado.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURAR_IMAGEN) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = BitmapFactory.decodeFile(rutaFotoActual);
                binding.imgFotoTicket.setImageBitmap(bitmap);
            }
        } else if (requestCode == SELECCIONAR_IMAGEN_GALERIA) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                // Guardamos la Uri como String para poder recuperarla después
                rutaFotoActual = uri.toString();
                try {
                    binding.imgFotoTicket.setImageBitmap(MediaStore.Images.Media
                            .getBitmap(this.getContentResolver(), uri));
                } catch (IOException e) {
                    Toast.makeText(this, getString(R.string.error_cargar_imagen),
                            Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.cancelado), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Busca el ticket a editar en la BD por {@link #idEditar} y rellena los campos
     * del formulario con sus datos. Si el ticket ya no existe en la BD, cierra
     * el formulario sin hacer nada.
     */
    private void cargarTicketParaEditar() {
        ArrayList<Ticket> todos = dbHelper.obtenerTodos();
        for (Ticket t : todos) {
            if (t.getId() == idEditar) {
                ticketEditar = t;
                break;
            }
        }

        binding.etDescCortaTicket.setText(ticketEditar.getDescCorta());
        binding.etDescLargaTicket.setText(ticketEditar.getDescLarga());
        binding.etPrecioTicket.setText(String.valueOf(ticketEditar.getPrecio()));
        binding.etLocalizTicket.setText(ticketEditar.getLocaliz());

        // Si el ticket tiene foto se muestra
        if (ticketEditar.getFoto() != null && !ticketEditar.getFoto().isEmpty()) {
            rutaFotoActual = ticketEditar.getFoto();
            Bitmap bitmap = BitmapFactory.decodeFile(rutaFotoActual);
            if (bitmap != null) binding.imgFotoTicket.setImageBitmap(bitmap);
        }

        for (int i = 0; i < listaCategorias.size(); i++) {
            if (listaCategorias.get(i).getId() == ticketEditar.getCategoriaId()) {
                binding.spCategoriaTicket.setSelection(i);
                break;
            }
        }

        // Mostramos la fecha de creación del ticket en formato legible.
        // SimpleDateFormat convierte el long de timestamp en una cadena con fecha y hora
        String fechaFormateada = new SimpleDateFormat(getString(R.string.formato_fecha)).format(new Date(ticketEditar.getFecha()));
        binding.tvFechaTicket.setText(getString(R.string.fecha_creacion, fechaFormateada));
        binding.tvFechaTicket.setVisibility(android.view.View.VISIBLE);
    }

    /**
     * Valida los campos y persiste el ticket en SQLite (alta o actualización).
     * La fecha siempre se actualiza al momento de guardar, tanto en alta como
     * en edición, para que el orden cronológico de la lista refleje el último cambio.
     */
    private void guardar() {
        String descCorta = binding.etDescCortaTicket.getText().toString().trim();
        String descLarga = binding.etDescLargaTicket.getText().toString().trim();
        String precioStr = binding.etPrecioTicket.getText().toString().trim();
        String localiz   = binding.etLocalizTicket.getText().toString().trim();

        if (descCorta.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this,
                    getString(R.string.descripcion_precio_obligatorios), Toast.LENGTH_SHORT).show();
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.precio_no_valido), Toast.LENGTH_SHORT).show();
            return;
        }

        Categoria catSeleccionada = (Categoria) binding.spCategoriaTicket.getSelectedItem();

        if (idEditar == -1) {
            Ticket nuevo = new Ticket(
                    0,
                    rutaFotoActual,
                    catSeleccionada.getId(),
                    precio,
                    System.currentTimeMillis(),
                    descCorta,
                    descLarga,
                    localiz);
            dbHelper.insertar(nuevo);
            Toast.makeText(this, getString(R.string.ticket_guardado), Toast.LENGTH_SHORT).show();
        } else {
            ticketEditar.setDescCorta(descCorta);
            ticketEditar.setDescLarga(descLarga);
            ticketEditar.setPrecio(precio);
            ticketEditar.setLocaliz(localiz);
            ticketEditar.setFoto(rutaFotoActual);
            ticketEditar.setCategoriaId(catSeleccionada.getId());
            ticketEditar.setFecha(System.currentTimeMillis());
            dbHelper.actualizar(ticketEditar);
            Toast.makeText(this, getString(R.string.ticket_actualizado), Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}