package com.utp.meditrackapp.core.util;

import com.utp.meditrackapp.core.models.enums.EntidadPrefix;

import java.util.UUID;

public class IdGenerator {
    public static String generateId(EntidadPrefix entidad) {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        String shortId = uuid.substring(0, 8);

        return entidad.getPrefix() + "-" + shortId;
    }
}
