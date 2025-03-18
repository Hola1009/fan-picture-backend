package com.fancier.picture.backend.auth.helper;

import com.fancier.picture.backend.auth.model.SpaceAuthContext;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
public class SpaceAuthHolder {
    private static final ThreadLocal<SpaceAuthContext> SPACE_AUTH_CONTEXT = new ThreadLocal<>();

    public static void set(SpaceAuthContext spaceAuthContext) {
        SPACE_AUTH_CONTEXT.set(spaceAuthContext);
    }

    public static SpaceAuthContext get() {
        return SPACE_AUTH_CONTEXT.get();
    }

    public static void clear() {
        SPACE_AUTH_CONTEXT.remove();
    }

}
