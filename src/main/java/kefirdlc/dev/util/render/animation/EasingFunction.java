package kefirdlc.dev.util.render.animation;
// coded by sitoku \\
// since 27.04.2026 \\

/**
 * Functional contract describing easing curves used to remap linear progress
 * values. Implementations must be pure and return values in the {@code [0, 1]}
 * range for inputs from {@code 0} to {@code 1}.
 */
@FunctionalInterface
public interface EasingFunction {

    float ease(float t);

    static EasingFunction identity() {
        return t -> t;
    }

    default EasingFunction compose(EasingFunction after) {
        if (after == null) {
            return this;
        }
        return t -> after.ease(ease(t));
    }
}
