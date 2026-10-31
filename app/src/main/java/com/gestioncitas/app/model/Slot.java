package com.gestioncitas.app.model;

/**
 * Representa un horario generado (slot) de un médico para una fecha concreta.
 * Se usa para mostrar la disponibilidad: 60 minutos por cita.
 */
public class Slot {
    private String fecha;     // "yyyy-MM-dd"
    private String hora;      // "HH:mm"
    private boolean ocupado;

    public Slot() {}

    public Slot(String fecha, String hora, boolean ocupado) {
        this.fecha = fecha;
        this.hora = hora;
        this.ocupado = ocupado;
    }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public boolean isOcupado() { return ocupado; }
    public void setOcupado(boolean ocupado) { this.ocupado = ocupado; }
}
