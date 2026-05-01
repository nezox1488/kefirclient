package kefirdlc.dev.util.render.postfx;
// coded by sitoku \\
// since 27.04.2026 \\

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

/**
 * Простой color-only рендер-таргет RGBA8 с текстурой и FBO.
 */
public final class RenderTarget {
    public int fbo = 0;
    public int colorTex = 0;
    public int width = 0;
    public int height = 0;

    public void ensure(int w, int h) {
        if (w <= 0 || h <= 0) return;
        if (w == width && h == height && fbo != 0 && colorTex != 0) return;
        destroy();

        width = w;
        height = h;

        int prevActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        int prevTextureBinding;
        if (prevActiveTexture != GL13.GL_TEXTURE0) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            prevTextureBinding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        } else {
            prevTextureBinding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        }
        int prevFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int prevDrawBuffer = GL11.glGetInteger(GL11.GL_DRAW_BUFFER);
        int prevReadBuffer = GL11.glGetInteger(GL11.GL_READ_BUFFER);

        boolean restoreActiveTexture = prevActiveTexture != GL13.GL_TEXTURE0;
        try {
            colorTex = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTex);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0);

            fbo = GL30.glGenFramebuffers();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorTex, 0);

            GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
            GL11.glReadBuffer(GL11.GL_NONE);
            int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
            if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
                throw new IllegalStateException("FBO incomplete: status=" + status);
            }
        } catch (RuntimeException | Error ex) {
            destroy();
            throw ex;
        } finally {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFramebuffer);
            GL11.glDrawBuffer(prevDrawBuffer);
            GL11.glReadBuffer(prevReadBuffer);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTextureBinding);
            if (restoreActiveTexture) {
                GL13.glActiveTexture(prevActiveTexture);
            }
        }
    }

    public void bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL11.glViewport(0, 0, width, height);
    }

    public static void bindDefault(int w, int h) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, w, h);
        GL11.glDrawBuffer(GL11.GL_BACK);
        GL11.glReadBuffer(GL11.GL_BACK);
    }

    public void generateMips() {
        if (colorTex == 0) return;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTex);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public void destroy() {
        if (fbo != 0) {
            GL30.glDeleteFramebuffers(fbo);
            fbo = 0;
        }
        if (colorTex != 0) {
            GL11.glDeleteTextures(colorTex);
            colorTex = 0;
        }
        width = height = 0;
    }
}


