package es.umh.dadm.mistickets;

/**
 * Modelo que representa un ticket de gasto.
 * Se persiste en la tabla "Tickets" de la base de datos SQLite a través de
 * {@link TicketsSQLiteHelper}.
 */
public class Ticket {

    private int id;
    private String foto;
    private int categoriaId;
    private double precio;
    private long fecha;
    private String descCorta;
    private String descLarga;
    private String localiz;

    public Ticket() {}

    public Ticket(int id, String foto, int categoriaId, double precio,
                  long fecha, String descCorta, String descLarga, String localiz) {
        this.id = id;
        this.foto = foto;
        this.categoriaId = categoriaId;
        this.precio = precio;
        this.fecha = fecha;
        this.descCorta = descCorta;
        this.descLarga = descLarga;
        this.localiz = localiz;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public int getCategoriaId() { return categoriaId; }
    public void setCategoriaId(int categoriaId) { this.categoriaId = categoriaId; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }

    public String getDescCorta() { return descCorta; }
    public void setDescCorta(String descCorta) { this.descCorta = descCorta; }

    public String getDescLarga() { return descLarga; }
    public void setDescLarga(String descLarga) { this.descLarga = descLarga; }

    public String getLocaliz() { return localiz; }
    public void setLocaliz(String localiz) { this.localiz = localiz; }

    /** Usado por {@link android.widget.ArrayAdapter} para mostrar cada fila del ListView. */
    @Override
    public String toString() {
        return descCorta + " - " + precio + "€";
    }
}