package com.gnc.task.application.mvp.model.entity;

public enum TMantenimiento {
    CAMBIODECOMPONENTE("Cambio De Componente"), REPARACIONDECOMPONENTE("Reparación De Componente");

    private String mantenimientoName;

    private TMantenimiento(String mantenimientoName) {
        this.mantenimientoName = mantenimientoName;
    }

    public String getMantenimientoName() {
        return mantenimientoName;
    }

    @Override
    public String toString() {
        return mantenimientoName;
    }
}
