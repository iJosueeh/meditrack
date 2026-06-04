package com.utp.meditrackapp.core.models.enums;

public enum TipoMovimientoEnum {
    ENTRADA("MOV-T-01"),
    SALIDA("MOV-T-02");

    private final String id;

    TipoMovimientoEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static TipoMovimientoEnum fromId(String id) {
        for (TipoMovimientoEnum e : values()) {
            if (e.id.equals(id)) return e;
        }
        return null;
    }
}
