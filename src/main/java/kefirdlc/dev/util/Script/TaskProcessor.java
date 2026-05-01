package kefirdlc.dev.util.Script;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.api.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.util.Objects;
import java.util.PriorityQueue;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskProcessor<T> {
    public int tickCounter = 0;
    public PriorityQueue<Task<T>> activeTasks = new PriorityQueue<>((r1, r2) -> Integer.compare(r2.priority, r1.priority));

    public void tick(int deltaTime) {
        tickCounter += deltaTime;
    }

    public void addTask(Task<T> task) {
        activeTasks.removeIf(r -> Objects.equals(r.provider, task.provider));
        task.expiresIn += tickCounter;
        this.activeTasks.add(task);
    }

    public T fetchActiveTaskValue() {

        while (!activeTasks.isEmpty()) {
            Task<T> task = activeTasks.peek();

            if (task == null) {
                activeTasks.poll();
                continue;
            }

            boolean isExpired = task.expiresIn <= tickCounter;


            boolean isProviderDisabled = task.provider != null && !task.provider.isToggled();

            if (isExpired || isProviderDisabled) {
                activeTasks.poll();
            } else {
                break;
            }
        }

        if (activeTasks.isEmpty()) {
            return null;
        } else {
            return activeTasks.peek().value;
        }
    }

    @ToString
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor
    public static class Task<T> {
        @NonFinal
        int expiresIn;
        int priority;
        Function provider;
        T value;
    }
}