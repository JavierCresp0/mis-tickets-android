package es.umh.dadm.mistickets;

/**
 * Modelo que representa una categoría de tickets.
 * Se persiste en formato JSON en el almacenamiento interno del dispositivo a través de
 * {@link CategoriasHelper}. El campo {@code foto} almacena la ruta absoluta o URI
 * de la imagen asociada.
 */
public class Categoria {

    private int id;
    private String descCorta;
    private String descLarga;
    private String detalles;
    private String foto;

    public Categoria() {}

    public Categoria(int id, String descCorta, String descLarga, String detalles, String foto) {
        this.id = id;
        this.descCorta = descCorta;
        this.descLarga = descLarga;
        this.detalles = detalles;
        this.foto = foto;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescCorta() { return descCorta; }
    public void setDescCorta(String descCorta) { this.descCorta = descCorta; }

    public String getDescLarga() { return descLarga; }
    public void setDescLarga(String descLarga) { this.descLarga = descLarga; }

    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    /** Usado por {@link android.widget.ArrayAdapter} para mostrar cada fila del ListView. */
    @Override
    public String toString() {
        return descCorta;
    }
}