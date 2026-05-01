package kefirdlc.dev.util.render.backends.gl;
// coded by sitoku \\
// since 27.04.2026 \\

import org.lwjgl.opengl.GL20;

import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;

public final class ShaderProgram {
    private final int programId;

    public ShaderProgram(String vertexSource, String fragmentSource) {
        int vs = compile(GL20.GL_VERTEX_SHADER, vertexSource);
        int fs = compile(GL20.GL_FRAGMENT_SHADER, fragmentSource);

        programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vs);
        GL20.glAttachShader(programId, fs);
        GL20.glLinkProgram(programId);

        try (var stack = stackPush()) {
            IntBuffer status = stack.mallocInt(1);
            GL20.glGetProgramiv(programId, GL20.GL_LINK_STATUS, status);
            if (status.get(0) == 0) {
                String log = GL20.glGetProgramInfoLog(programId);
                GL20.glDeleteShader(vs);
                GL20.glDeleteShader(fs);
                GL20.glDeleteProgram(programId);
                throw new IllegalStateException("Program link failed: " + log);
            }
        }

        GL20.glDetachShader(programId, vs);
        GL20.glDetachShader(programId, fs);
        GL20.glDeleteShader(vs);
        GL20.glDeleteShader(fs);
    }

    public static ShaderProgram fromResources(String vertexPath, String fragmentPath) {
        String vs = ResourceUtils.readText(vertexPath);
        String fs = ResourceUtils.readText(fragmentPath);
        return new ShaderProgram(vs, fs);
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        try (var stack = stackPush()) {
            IntBuffer status = stack.mallocInt(1);
            GL20.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, status);
            if (status.get(0) == 0) {
                String log = GL20.glGetShaderInfoLog(shader);
                GL20.glDeleteShader(shader);
                throw new IllegalStateException("Shader compile failed: " + log);
            }
        }
        return shader;
    }

    public void use() {
        GL20.glUseProgram(programId);
    }

    public void delete() {
        GL20.glDeleteProgram(programId);
    }

    public int id() {
        return programId;
    }

    public int getUniformLocation(String name) {
        return GL20.glGetUniformLocation(programId, name);
    }
}


