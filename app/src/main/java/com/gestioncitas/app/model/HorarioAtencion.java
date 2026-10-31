package com.gestioncitas.app.model;

public class HorarioAtencion {
    private int id;
    private int medicoId;
    private int diaSemana; // 1 = Lunes ... 7 = Domingo
    private String horaInicio; // "08:00"
    private String horaFin;    // "12:00"

    public HorarioAtencion() {}

    public HorarioAtencion(int id, int medicoId, int diaSemana, String horaInicio, String horaFin) {
        this.id = id;
        this.medicoId = medicoId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMedicoId() { return medicoId; }
    public void setMedicoId(int medicoId) { this.medicoId = medicoId; }
    public int getDiaSemana() { return diaSemana; }
    public void setDiaSemana(int diaSemana) { this.diaSemana = diaSemana; }
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }
}
