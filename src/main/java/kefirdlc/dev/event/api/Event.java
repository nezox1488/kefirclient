package kefirdlc.dev.event.api;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Event {
    private boolean canceled;
    @Setter
    private boolean pre;

    public void cancel() {canceled = true;}

    public boolean isCanceled() {
        return canceled;
    }

    public void resume() {canceled = false;}

    public void call() {
        if (!KefirDLC.instance.isPanic()) {
            KefirDLC.getInstance().getEventBus().post(this);
        }
    }
}
