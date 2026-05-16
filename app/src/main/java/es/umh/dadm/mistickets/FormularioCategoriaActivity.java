package es.umh.dadm.mistickets;

import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import es.umh.dadm.mistickets.databinding.ActivityFormularioCategoriaBinding;

import java.util.ArrayList;

/**
 * Formulario de alta y edición de categorías. Opera en dos modos según el extra recibido:
 *
 *   Alta: no se pasa {@link ListaCategoriasActivity#PARAM_CATEGORIA_POS}; {@code posEditar} vale -1.
 *   Edición: se recibe la posición de la categoría en la lista JSON.
 *
 * Al guardar, siempre se sobreescribe el fichero JSON completo con la lista actualizada.
 */
public class FormularioCategoriaActivity extends AppCompatActivity {
    /** Código de solicitud para la actividad de captura de cámara. */
    private static final int CAPTURAR_IMAGEN = 1;
    /** Código de solicitud para el selector de imágenes de la galería. */
    private static final int SELECCIONAR_IMAGEN_GALERIA = 2;
    private Context contexto;
    /** Ruta absoluta o URI en String de la foto seleccionada; vacía si no hay foto. */
    private String rutaFotoActual = "";
    private ActivityFormularioCategoriaBinding binding;
    private ArrayList<Categoria> listaCategorias;

    // Si es -1 estamos dando de alta, si tiene otro valor estamos editando
    private int posEditar = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFormularioCategoriaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        contexto = this;

        // Botón cámara
        binding.btnCamaraCategoria.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;
            try {
                photoFile = crearFicheroImagen();
            } catch (IOException e) {
                Log.e("FormularioCategoria", "Error al crear fichero: " + e.getMessage());
                Toast.makeText(this, getString(R.string.error_abrir_camara),
                        Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(contexto,
                        "es.umh.dadm.mistickets.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURAR_IMAGEN);
            }
        });

        // Botón galería
        binding.btnGaleriaCategoria.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.seleccionar_imagen)),
                    SELECCIONAR_IMAGEN_GALERIA);
        });

        // Cargamos la lista completa desde fichero
        listaCategorias = CategoriasHelper.cargar(this);

        // Comprobamos si nos han pasado una posición por Intent
        posEditar = getIntent().getIntExtra(
                ListaCategoriasActivity.PARAM_CATEGORIA_POS, -1);

        if (posEditar != -1) {
            // Modo edición: rellenamos los campos con los datos existentes
            cargarDatosParaEditar();
            setTitle(R.string.titulo_editar_categoria);
        } else {
            // Modo alta: los campos están vacíos, solo cambiamos el título
            setTitle(R.string.titulo_nueva_categoria);
        }

        // Botón guardar
        binding.btnGuardarCategoria.setOnClickListener(v -> guardar());
    }

    // Rellena los campos del formulario con los datos de la categoría a editar
    private void cargarDatosParaEditar() {
        Categoria c = listaCategorias.get(posEditar);
        binding.etDescCortaCategoria.setText(c.getDescCorta());
        binding.etDescLargaCategoria.setText(c.getDescLarga());
        binding.etDetallesCategoria.setText(c.getDetalles());

        if (c.getFoto() != null && !c.getFoto().isEmpty()) {
            rutaFotoActual = c.getFoto();
            Bitmap bitmap = BitmapFactory.decodeFile(rutaFotoActual);
            if (bitmap != null) binding.imgFotoCategoria.setImageBitmap(bitmap);
        }
    }

    private void guardar() {
        // Recogemos los valores escritos en los campos
        String descCorta = binding.etDescCortaCategoria.getText().toString().trim();
        String descLarga = binding.etDescLargaCategoria.getText().toString().trim();
        String detalles  = binding.etDetallesCategoria.getText().toString().trim();

        // Validación: la descripción corta es obligatoria
        if (descCorta.isEmpty()) {
            Toast.makeText(this,
                    getString(R.string.desc_corta_obligatoria), Toast.LENGTH_SHORT).show();
            return; // salimos sin guardar
        }

        if (posEditar == -1) {
            // ALTA: creamos una categoría nueva con un id generado
            int nuevoId = CategoriasHelper.generarId(listaCategorias);
            Categoria nueva = new Categoria(nuevoId, descCorta, descLarga, detalles, rutaFotoActual);
            listaCategorias.add(nueva);
            Toast.makeText(this, getString(R.string.categoria_guardada), Toast.LENGTH_SHORT).show();
        } else {
            // EDICIÓN: modificamos el objeto que ya existe en la lista
            Categoria c = listaCategorias.get(posEditar);
            c.setDescCorta(descCorta);
            c.setDescLarga(descLarga);
            c.setDetalles(detalles);
            c.setFoto(rutaFotoActual);
            Toast.makeText(this, getString(R.string.categoria_actualizada), Toast.LENGTH_SHORT).show();
        }

        // Guardamos la lista completa en el fichero
        CategoriasHelper.guardar(this, listaCategorias);

        // Cerramos el formulario y volvemos al listado
        finish();
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
        String nombreFichero = "CAT_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(nombreFichero, ".jpg", storageDir);
        rutaFotoActual = imagen.getAbsolutePath();
        return imagen;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURAR_IMAGEN) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = BitmapFactory.decodeFile(rutaFotoActual);
                binding.imgFotoCategoria.setImageBitmap(bitmap);
            }
        } else if (requestCode == SELECCIONAR_IMAGEN_GALERIA) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                rutaFotoActual = uri.toString();
                try {
                    binding.imgFotoCategoria.setImageBitmap(MediaStore.Images.Media
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

}