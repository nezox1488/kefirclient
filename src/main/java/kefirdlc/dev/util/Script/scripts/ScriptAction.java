package kefirdlc.dev.util.Script.scripts;
// coded by sitoku \\
// since 27.04.2026 \\

/**
 * Functional interface representing an action to be performed by a script.
 * This interface is intended to be used as a callback mechanism for script actions.
 *
 * @see Script
 */
@FunctionalInterface
public interface ScriptAction {

    /**
     * Performs the action associated with this interface.
     */
    void perform();
}
