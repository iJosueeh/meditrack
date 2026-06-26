package com.utp.meditrackapp.core.models.enums;

public enum MotivoMovimientoEnum {
    COMPRA("MOV-M-00-0000001"),
    TRANSFERENCIA("MOV-M-00-0000002"),
    ATENCION("MOV-M-00-0000003"),
    MERMA("MOV-M-00-0000004");

    private final String id;

    MotivoMovimientoEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static MotivoMovimientoEnum fromId(String id) {
        for (MotivoMovimientoEnum e : values()) {
            if (e.id.equals(id)) return e;
        }
        return null;
    }
}
