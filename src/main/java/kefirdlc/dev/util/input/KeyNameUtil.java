package kefirdlc.dev.util.input;
// coded by sitoku \\
// since 27.04.2026 \\

import org.lwjgl.glfw.GLFW;

public class KeyNameUtil {

    public static String getKeyName(int key) {
        if (key == -1) return "None";

        if (key == 2000) return "Wheel Up";
        if (key == 2001) return "Wheel Down";

        if (key >= 1000) {
            return "Mouse " + (key - 1000);
        }

        return GLFW.glfwGetKeyName(key, 0) != null
                ? GLFW.glfwGetKeyName(key, 0).toUpperCase()
                : "Key " + key;
    }
}