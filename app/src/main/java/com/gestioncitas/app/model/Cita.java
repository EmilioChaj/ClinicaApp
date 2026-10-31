package com.gestioncitas.app.model;

public class Cita {
    public static final String ESTADO_PROGRAMADA = "programada";
    public static final String ESTADO_CANCELADA = "cancelada";

    private int id;
    private int pacienteId;
    private int medicoId;
    private String fecha;   // "yyyy-MM-dd"
    private String hora;    // "HH:mm"
    private String motivo;
    private String estado;

    // Campos de apoyo para mostrar en la lista (no se guardan en la tabla citas)
    private String medicoNombre;
    private String especialidad;

    public Cita() {}

    public Cita(int id, int pacienteId, int medicoId, String fecha, String hora, String motivo, String estado) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPacienteId() { return pacienteId; }
    public void setPacienteId(int pacienteId) { this.pacienteId = pacienteId; }
    public int getMedicoId() { return medicoId; }
    public void setMedicoId(int medicoId) { this.medicoId = medicoId; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMedicoNombre() { return medicoNombre; }
    public void setMedicoNombre(String medicoNombre) { this.medicoNombre = medicoNombre; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public boolean esProgramada() { return ESTADO_PROGRAMADA.equals(estado); }
    public boolean esCancelada() { return ESTADO_CANCELADA.equals(estado); }
}
