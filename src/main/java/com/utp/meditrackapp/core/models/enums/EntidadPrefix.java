package com.utp.meditrackapp.core.models.enums;

public enum EntidadPrefix {
    SEDE("SED"),
    PERSONAL("PER"),
    USUARIO("USR"),
    ROL("ROL"),
    CATEGORIA("CAT"),
    PROVEEDOR("PRV"),
    PRODUCTO("PRD"),
    LOTE("LOT"),
    INVENTARIO("STK"),
    TIPO_MOVIMIENTO("TMV"),
    MOVIMIENTO("MOV"),
    DISPENSACION("DIS"),
    DETALLE_DISPENSACION("DTD"),
    LOG_ACTIVIDAD("LOG");

    private final String prefix;

    // Constructor del Enum
    EntidadPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
