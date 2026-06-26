package com.utp.meditrackapp.core.models.enums;

public enum TipoMovimientoEnum {
    ENTRADA("MOV-T-00-0000001"),
    SALIDA("MOV-T-00-0000002");

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
