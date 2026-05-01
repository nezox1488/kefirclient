package kefirdlc.dev.util.render.postfx;
// coded by sitoku \\
// since 27.04.2026 \\

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import kefirdlc.dev.util.render.backends.gl.ResourceUtils;

final class PostShader {
    private static final Logger LOGGER = LogManager.getLogger("HysteriaPostShader");

    final int program;
    final int uTex0;
    final int uTexelSize;
    final int uDirection;
    final int uSigma;

    PostShader(String vertPath, String fragPath) {
        int vs = compile(GL20.GL_VERTEX_SHADER, ResourceUtils.readText(vertPath), vertPath);
        int fs = compile(GL20.GL_FRAGMENT_SHADER, ResourceUtils.readText(fragPath), fragPath);

        int createdProgram = GL20.glCreateProgram();
        if (createdProgram == 0) {
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            String message = "Failed to create OpenGL program for post-processing shaders.";
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }

        try {
            GL20.glAttachShader(createdProgram, vs);
            GL20.glAttachShader(createdProgram, fs);
            GL20.glLinkProgram(createdProgram);

            int linkStatus = GL20.glGetProgrami(createdProgram, GL20.GL_LINK_STATUS);
            if (linkStatus == GL11.GL_FALSE) {
                String infoLog = GL20.glGetProgramInfoLog(createdProgram);
                LOGGER.error("Failed to link post shader program (vert: {}, frag: {}): {}", vertPath, fragPath, infoLog);
                throw new IllegalStateException("Failed to link post shader program: " + infoLog);
            }
        } catch (RuntimeException e) {
            GL20.glDeleteProgram(createdProgram);
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            throw e;
        }

        GL20.glDeleteShader(vs);
        GL20.glDeleteShader(fs);

        program = createdProgram;
        uTex0 = GL20.glGetUniformLocation(program, "uTex0");
        uTexelSize = GL20.glGetUniformLocation(program, "uTexelSize");
        uDirection = GL20.glGetUniformLocation(program, "uDirection");
        uSigma = GL20.glGetUniformLocation(program, "uSigma");
    }

    private static int compile(int type, String src, String shaderPath) {
        int shader = GL20.glCreateShader(type);
        if (shader == 0) {
            String message = "Failed to create shader object for type " + shaderTypeName(type) + " (" + shaderPath + ").";
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }

        GL20.glShaderSource(shader, src);
        GL20.glCompileShader(shader);

        int status = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS);
        if (status == GL11.GL_FALSE) {
            String infoLog = GL20.glGetShaderInfoLog(shader);
            String message = "Failed to compile " + shaderTypeName(type) + " shader (" + shaderPath + "): " + infoLog;
            LOGGER.error(message);
            GL20.glDeleteShader(shader);
            throw new IllegalStateException(message);
        }

        return shader;
    }

    private static String shaderTypeName(int type) {
        return switch (type) {
            case GL20.GL_VERTEX_SHADER -> "vertex";
            case GL20.GL_FRAGMENT_SHADER -> "fragment";
            default -> "unknown";
        };
    }
}




