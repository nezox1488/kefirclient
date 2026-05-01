package kefirdlc.dev.util.input;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.ui.clickgui.ClickGuiScreen;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class MouseHandler {
    
    private static boolean leftPressed = false;
    private static boolean rightPressed = false;
    private static double lastMouseX = 0;
    private static double lastMouseY = 0;
    
    public static void handleMouse() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getWindow() == null) return;
        
        ClickGuiScreen clickGui = KefirDLC.getInstance().getFunctionManager().getModule(ClickGuiScreen.class);
        if (clickGui == null || !clickGui.isOpen()) return;
        
        long window = mc.getWindow().getHandle();
        double mouseX = 0;
        double mouseY = 0;
        
        try {
            double[] xpos = new double[1];
            double[] ypos = new double[1];
            GLFW.glfwGetCursorPos(window, xpos, ypos);
            

            mouseX = xpos[0] * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth();
            mouseY = ypos[0] * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight();
        } catch (Exception e) {
            return;
        }
        
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        
        if (leftDown && !leftPressed) {
            clickGui.handleMouseClick(mouseX, mouseY, 0);
            leftPressed = true;
        } else if (!leftDown && leftPressed) {
            clickGui.handleMouseRelease(mouseX, mouseY, 0);
            leftPressed = false;
        }
        
        if (rightDown && !rightPressed) {
            clickGui.handleMouseClick(mouseX, mouseY, 1);
            rightPressed = true;
        } else if (!rightDown && rightPressed) {
            clickGui.handleMouseRelease(mouseX, mouseY, 1);
            rightPressed = false;
        }
        
        if (leftPressed && (mouseX != lastMouseX || mouseY != lastMouseY)) {
            clickGui.handleMouseDrag(mouseX, mouseY, 0, mouseX - lastMouseX, mouseY - lastMouseY);
        }
        
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }
}