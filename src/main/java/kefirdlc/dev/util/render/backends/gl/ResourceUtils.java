package kefirdlc.dev.util.render.backends.gl;
// coded by sitoku \\
// since 27.04.2026 \\

import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class ResourceUtils {
    private ResourceUtils() {}

    public static String readText(String path) {
        ClassLoader cl = ResourceUtils.class.getClassLoader();
        try (InputStream in = cl.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Resource not found: " + path);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }
    }

    public static ByteBuffer readBinary(String path) {
        ClassLoader cl = ResourceUtils.class.getClassLoader();
        try (InputStream in = cl.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Resource not found: " + path);
            byte[] bytes = in.readAllBytes();
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes).flip();
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }
    }
}


