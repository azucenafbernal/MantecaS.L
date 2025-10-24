package com.mantecasl.accommodationapp.business.entity;

public class SolicitudReserva extends Reserva {
    private boolean confirmada;

    public SolicitudReserva() {
        super();
        this.confirmada = false;
    }

    public boolean isConfirmada() {
        return confirmada;
    }
}
