package com.utp.meditrackapp.core.models.enums;

public enum MotivoMovimientoEnum {
    COMPRA("MOV-M-01"),
    TRANSFERENCIA("MOV-M-02"),
    ATENCION("MOV-M-03"),
    MERMA("MOV-M-04");

    private final String id;

    MotivoMovimientoEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
