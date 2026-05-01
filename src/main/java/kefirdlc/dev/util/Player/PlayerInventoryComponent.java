package kefirdlc.dev.util.Player;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.event.impl.input.InputEvent;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.impl.combat.furry.AngleUtil;
import kefirdlc.dev.module.impl.combat.furry.RotationConfig;
import kefirdlc.dev.module.impl.combat.furry.RotationController;
import kefirdlc.dev.util.Script.TaskPriority;
import kefirdlc.dev.util.Script.scripts.Script;
import kefirdlc.dev.util.others.ServerUtil;
import kefirdlc.dev.util.wrapper.Wrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.List;

@UtilityClass
public class PlayerInventoryComponent implements Wrapper {
    public final List<KeyBinding> moveKeys = List.of(mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey,mc.options.sprintKey);
    public static final Script script = new Script(), postScript = new Script();
    public boolean canMove = true;

    public void tick() {
        script.update();
    }

    public void postMotion() {
        postScript.update();
    }

    public void input(InputEvent e) {
        if (!canMove) e.inputNone();
    }

    public void addTask(Runnable task) {
        if (script.isFinished() && MobilityHandler.hasPlayerMovement()) {
            switch (ServerUtil.server) {
                case "FunTime" -> {
                    script.cleanup().addTickStep(0, () -> {
                        PlayerInventoryComponent.disableMoveKeys();
                        PlayerInventoryComponent.rotateToCamera();
                    }).addTickStep(1, () -> {
                        task.run();
                        enableMoveKeys();
                    });
                    return;
                }
                case "LonyGrief" -> {
                    script.cleanup().addTickStep(0, () -> {
                        PlayerInventoryComponent.disableMoveKeys();
                        PlayerInventoryComponent.rotateToCamera();
                    }).addTickStep(1, () -> {
                        task.run();
                        enableMoveKeys();
                    });
                    return;
                }
                case "ReallyWorld" -> {
                    if (mc.player.isOnGround()) {
                        script.cleanup().addTickStep(0, PlayerInventoryComponent::disableMoveKeys).addTickStep(2, PlayerInventoryComponent::rotateToCamera).addTickStep(3, task::run)
                                .addTickStep(4, PlayerInventoryComponent::enableMoveKeys);
                        return;
                    }
                }
                case "SpookyTime", "CopyTime" -> {
                    script.cleanup().addTickStep(0, ()-> {
                                PlayerInventoryComponent.disableMoveKeys();
                                PlayerInventoryComponent.rotateToCamera();
                            }).addTickStep(1, task::run)
                            .addTickStep(2, PlayerInventoryComponent::enableMoveKeys);
                    return;
                }
            }
        }
        script.addTickStep(0, PlayerInventoryComponent::rotateToCamera);
        postScript.cleanup().addTickStep(0, () -> {
            task.run();
            PlayerInventoryUtil.closeScreen(true);
        });
    }

    private void rotateToCamera() {
        Function module = null;
        RotationController.INSTANCE.rotateTo(AngleUtil.cameraAngle(), RotationConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_3, null);
    }
    private boolean wasSprinting = false;

    public void disableMoveKeys() {
        if (mc.player != null) {
            wasSprinting = mc.player.isSprinting();
        }
        canMove = false;
        unPressMoveKeys();
    }

    public void enableMoveKeys() {
        canMove = true;
        updateMoveKeys();
        if (mc.player != null && wasSprinting) {
            mc.player.setSprinting(true);
        }
    }

    public void unPressMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.setPressed(false));
    }

    public void updateMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.setPressed(InputUtil.isKeyPressed(mc.getWindow(), keyBinding.getDefaultKey().getCode())));
    }

    public boolean shouldSkipExecution() {
        return mc.currentScreen != null && !PlayerIntersectionUtil.isChat(mc.currentScreen) && !(mc.currentScreen instanceof SignEditScreen) && !(mc.currentScreen instanceof AnvilScreen)
                && !(mc.currentScreen instanceof AbstractCommandBlockScreen) && !(mc.currentScreen instanceof StructureBlockScreen) ;
    }
}
