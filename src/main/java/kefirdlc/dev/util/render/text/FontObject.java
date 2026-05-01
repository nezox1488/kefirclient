package kefirdlc.dev.util.render.text;
// coded by sitoku \\
// since 27.04.2026 \\

import java.util.Objects;

public final class FontObject {
    public final String id;

    public FontObject(String id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FontObject that = (FontObject) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() { return "FontObject(" + id + ")"; }
}


