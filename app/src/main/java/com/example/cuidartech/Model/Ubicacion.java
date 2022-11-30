package com.example.cuidartech.Model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Ubicacion {

    private int idUbicacion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Timestamp fecha;
    private int idPersona;

    public int getIdUbicacion() {
        return idUbicacion;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public int getIdPersona() {
        return idPersona;
    }
}
