package kefirdlc.dev.util.render.core;
// coded by sitoku \\
// since 27.04.2026 \\

import java.util.ArrayDeque;
import java.util.Arrays;

public final class TransformStack {
    private final ArrayDeque<float[]> stack = new ArrayDeque<>();

    public TransformStack() {
        pushIdentity();
    }

    public void clear() {
        stack.clear();
        pushIdentity();
    }

    public void pushIdentity() {
        stack.push(new float[]{
                1,0,0,
                0,1,0,
                0,0,1
        });
    }

    public void pushRotation(float degrees) {
        float rad = (float) Math.toRadians(degrees);
        float c = (float) Math.cos(rad);
        float s = (float) Math.sin(rad);
        float[] r = new float[]{
                c, -s, 0,
                s,  c, 0,
                0,  0, 1
        };
        float[] top = stack.peek();
        stack.push(mul(top, r));
    }

    public void pushTranslation(float tx, float ty) {
        float[] t = new float[]{
                1, 0, tx,
                0, 1, ty,
                0, 0, 1
        };
        float[] top = stack.peek();
        stack.push(mul(top, t));
    }

    public void pushTranslationInv(float tx, float ty) {
        pushTranslation(-tx, -ty);
    }

    public void pushScale(float sx, float sy, float originX, float originY) {
        float translateX = originX - originX * sx;
        float translateY = originY - originY * sy;
        float[] s = new float[]{
                sx, 0,  translateX,
                0,  sy, translateY,
                0,  0,  1
        };
        float[] top = stack.peek();
        stack.push(mul(top, s));
    }

    public void pushScale(float scale, float originX, float originY) {
        pushScale(scale, scale, originX, originY);
    }

    public void replaceTop(float[] matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("matrix must not be null");
        }
        if (matrix.length != 9) {
            throw new IllegalArgumentException("matrix must have length 9");
        }
        for (float value : matrix) {
            if (!Float.isFinite(value)) {
                throw new IllegalArgumentException("matrix entries must be finite");
            }
        }
        if (stack.isEmpty()) {
            throw new IllegalStateException("cannot replace top matrix on an empty stack");
        }
        float[] copy = Arrays.copyOf(matrix, matrix.length);
        stack.pop();
        stack.push(copy);
    }

    public void pop() {
        if (stack.size() > 1) {
            stack.pop();
        }
    }

    public void popN(int count) {
        for (int i = 0; i < count; i++) {
            if (stack.size() > 1) {
                stack.pop();
            }
        }
    }

    public float[] current() {
        return stack.peek();
    }

    private static float[] mul(float[] a, float[] b) {

        float[] r = new float[9];
        r[0] = a[0]*b[0] + a[1]*b[3] + a[2]*b[6];
        r[1] = a[0]*b[1] + a[1]*b[4] + a[2]*b[7];
        r[2] = a[0]*b[2] + a[1]*b[5] + a[2]*b[8];

        r[3] = a[3]*b[0] + a[4]*b[3] + a[5]*b[6];
        r[4] = a[3]*b[1] + a[4]*b[4] + a[5]*b[7];
        r[5] = a[3]*b[2] + a[4]*b[5] + a[5]*b[8];

        r[6] = a[6]*b[0] + a[7]*b[3] + a[8]*b[6];
        r[7] = a[6]*b[1] + a[7]*b[4] + a[8]*b[7];
        r[8] = a[6]*b[2] + a[7]*b[5] + a[8]*b[8];
        return r;
    }
}
