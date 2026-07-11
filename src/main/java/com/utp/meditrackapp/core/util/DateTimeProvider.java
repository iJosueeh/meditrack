package com.utp.meditrackapp.core.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class DateTimeProvider {

    private static final ZoneId LIMA_ZONE = ZoneId.of("America/Lima");

    private DateTimeProvider() {}

    public static LocalDateTime now() {
        return LocalDateTime.now(LIMA_ZONE);
    }
}
