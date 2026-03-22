package com.lautadev.lautadev.Util;

import java.util.Set;

public class Constants {

    private Constants() {
    }

    public static final Set<String> UNPROTECTED_PATHS = Set.of(
            "/api/users/register",
            "/api/users/login",
            "/health"
    );
}

