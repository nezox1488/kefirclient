package kefirdlc.dev.util.render.postfx;
// coded by sitoku \\
// since 27.04.2026 \\

import org.lwjgl.opengl.*;

import java.util.Arrays;

/**
 * Captures the bindings of one texture unit and restores them when closed.
 */
final class TextureUnitGuard implements AutoCloseable {
    private final int unit;
    private final int previousActiveUnit;
    private final int[] targets;
    private final int[] bindings;
    private boolean closed;

    private TextureUnitGuard(int unit, int previousActiveUnit, int[] targets, int[] bindings) {
        this.unit = unit;
        this.previousActiveUnit = previousActiveUnit;
        this.targets = targets;
        this.bindings = bindings;
    }

    static TextureUnitGuard capture(int unit, int... requestedTargets) {
        int previousActive = Math.max(0, GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE) - GL13.GL_TEXTURE0);
        int[] targets = deduplicateTargets(requestedTargets);
        int[] bindings = new int[targets.length];
        if (targets.length > 0) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
            for (int i = 0; i < targets.length; i++) {
                bindings[i] = GL11.glGetInteger(bindingEnumForTarget(targets[i]));
            }
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + previousActive);
        }
        return new TextureUnitGuard(unit, previousActive, targets, bindings);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (targets.length > 0) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
            for (int i = 0; i < targets.length; i++) {
                GL11.glBindTexture(targets[i], bindings[i]);
            }
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + previousActiveUnit);
    }

    private static int[] deduplicateTargets(int[] requestedTargets) {
        if (requestedTargets == null || requestedTargets.length == 0) {
            return new int[0];
        }
        int[] copy = Arrays.copyOf(requestedTargets, requestedTargets.length);
        int count = 0;
        for (int value : copy) {
            if (value <= 0) {
                continue;
            }
            boolean exists = false;
            for (int i = 0; i < count; i++) {
                if (copy[i] == value) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                copy[count++] = value;
            }
        }
        return Arrays.copyOf(copy, count);
    }

    private static int bindingEnumForTarget(int target) {
        switch (target) {
            case GL11.GL_TEXTURE_1D:
                return GL11.GL_TEXTURE_BINDING_1D;
            case GL11.GL_TEXTURE_2D:
                return GL11.GL_TEXTURE_BINDING_2D;
            case GL12.GL_TEXTURE_3D:
                return GL12.GL_TEXTURE_BINDING_3D;
            case GL13.GL_TEXTURE_CUBE_MAP:
                return GL13.GL_TEXTURE_BINDING_CUBE_MAP;
            case GL30.GL_TEXTURE_1D_ARRAY:
                return GL30.GL_TEXTURE_BINDING_1D_ARRAY;
            case GL30.GL_TEXTURE_2D_ARRAY:
                return GL30.GL_TEXTURE_BINDING_2D_ARRAY;
            case GL31.GL_TEXTURE_RECTANGLE:
                return GL31.GL_TEXTURE_BINDING_RECTANGLE;
            case GL31.GL_TEXTURE_BUFFER:
                return GL31.GL_TEXTURE_BINDING_BUFFER;
            case GL40.GL_TEXTURE_CUBE_MAP_ARRAY:
                return GL40.GL_TEXTURE_BINDING_CUBE_MAP_ARRAY;
            default:
                return GL11.GL_TEXTURE_BINDING_2D;
        }
    }
}
