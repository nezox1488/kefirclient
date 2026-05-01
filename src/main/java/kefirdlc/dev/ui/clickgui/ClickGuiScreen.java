package kefirdlc.dev.ui.clickgui;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.event.impl.presss.EventPress;
import kefirdlc.dev.event.impl.render.RenderEvent;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.module.setting.api.Setting;
import kefirdlc.dev.module.setting.api.SettingElement;
import kefirdlc.dev.module.setting.impl.BooleanSetting;
import kefirdlc.dev.module.setting.impl.ModeSetting;
import kefirdlc.dev.module.setting.impl.NumberSetting;
import kefirdlc.dev.ui.clickgui.component.BindPopup;
import kefirdlc.dev.ui.clickgui.component.EmptyScreen;
import kefirdlc.dev.ui.clickgui.component.ModuleButton;
import kefirdlc.dev.ui.clickgui.component.Panel;
import kefirdlc.dev.ui.accounts.AccountManagerScreen;
import kefirdlc.dev.ui.clickgui.elements.SettingsPopup;
import kefirdlc.dev.ui.clickgui.elements.TextElement;
import kefirdlc.dev.util.input.KeyNameUtil;
import kefirdlc.dev.util.render.animation.Easings;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;
import kefirdlc.dev.util.render.text.FontRegistry;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "ClickGui", category = Category.RENDER)
public class ClickGuiScreen extends Function {

    private final List<Panel> panels = new ArrayList<>();
    private boolean isOpen = false;

    private final BindPopup bindPopup = new BindPopup();
    private final SettingsPopup settingsPopup = new SettingsPopup();

    private float animProgress = 0f;


    private boolean isSearching = false;
    private String searchQuery = "";
    private float searchAnim = 0f;
    private final List<SearchResult> searchResults = new ArrayList<>();
    private final List<Function> allModules = new ArrayList<>();
    private int selectedSearchIndex = 0;

    private float accountsButtonX;
    private float accountsButtonY;
    private float accountsButtonW = 116f;
    private float accountsButtonH = 24f;

    public boolean isOpen() {
        return isOpen;
    }

    public BindPopup getBindPopup() {
        return bindPopup;
    }

    public ClickGuiScreen() {
        setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }


    private void initializePanels() {
        if (!panels.isEmpty()) return;

        int index = 0;
        allModules.clear();

        for (Category category : Category.values()) {
            if (category == Category.THEME || category == Category.SCRIPT || category == Category.CONFIG) continue;
            List<Function> modules = KefirDLC.getInstance().getFunctionManager().getModules(category);
            allModules.addAll(modules);

            if (!modules.isEmpty()) {
                Panel panel = new Panel(category, 50, 50, modules);
                panels.add(panel);
                index++;
            }
        }
        centerPanels();
    }

    private void centerPanels() {
        if (mc.getWindow() == null) return;
        double screenWidth = mc.getWindow().getScaledWidth();
        double screenHeight = mc.getWindow().getScaledHeight();

        float panelSpacing = 130;
        float totalWidth = (panels.size() - 1) * panelSpacing;
        float startX = (float) ((screenWidth - totalWidth) / 2.0f) - 60;
        float startY = (float) (screenHeight / 2.0 - 160);

        int index = 0;
        for (Panel panel : panels) {
            panel.currentX = startX + index * panelSpacing;
            panel.currentY = startY;
            panel.targetX = panel.currentX;
            panel.targetY = panel.currentY;
            index++;
        }
    }

    @Override
    public void toggle() {
        isOpen = !isOpen;
        if (isOpen) {
            onEnable();
            mc.setScreen(new EmptyScreen());
            animProgress = 0f;
            isSearching = false;
            searchQuery = "";
            searchAnim = 0f;
            settingsPopup.close();
            bindPopup.close();
        } else {
            onDisable();
            mc.setScreen(null);
            bindPopup.close();
            settingsPopup.close();
        }
    }

    @Subscribe
    public void onRender(RenderEvent event) {
        if (!isOpen) return;

        initializePanels();
        Renderer2D renderer = event.renderer();
        FontObject font = FontRegistry.SF_REGULAR;

        double scaledWidth = mc.getWindow().getScaledWidth();
        double scaledHeight = mc.getWindow().getScaledHeight();
        double mouseX = mc.mouse.getX() * scaledWidth / (double) mc.getWindow().getWidth();
        double mouseY = mc.mouse.getY() * scaledHeight / (double) mc.getWindow().getHeight();

        if (animProgress < 1f) {
            animProgress += 0.01f;
            if (animProgress > 1f) animProgress = 1f;
        }
        float guiAlpha = Easings.EASE_OUT_CUBIC.ease(animProgress);

        renderer.rect(0, 0, (float) scaledWidth, (float) scaledHeight, 0, new Color(0, 0, 0, (int)(120 * guiAlpha)).getRGB());

        float panelAlpha = isSearching ? guiAlpha * 0.2f : guiAlpha;

        for (Panel panel : panels) {
            float yOffset = (1f - guiAlpha) * 50;
            panel.render(renderer, font, panelAlpha, yOffset, mouseX, mouseY);
        }


        if (!isSearching && !bindPopup.isActive() && !settingsPopup.isActive()) {
            renderDescription(renderer, font, mouseX, mouseY);
        }


        renderSearch(renderer, font, (float) scaledWidth, (float) scaledHeight, mouseX, mouseY);

        bindPopup.render(renderer, font, mouseX, mouseY);
        settingsPopup.render(renderer, font, mouseX, mouseY);
        renderAccountsButton(renderer, mouseX, mouseY, (float) scaledWidth, (float) scaledHeight, guiAlpha);
    }


    private void renderSearch(Renderer2D r, FontObject font, float sw, float sh, double mx, double my) {
        if (isSearching) {
            if (searchAnim < 1f) searchAnim += 0.12f;
        } else {
            if (searchAnim > 0f) searchAnim -= 0.12f;
        }
        searchAnim = Math.max(0, Math.min(1, searchAnim));

        if (searchAnim <= 0.01f) return;

        float anim = Easings.EASE_OUT_CUBIC.ease(searchAnim);

        r.rect(0, 0, sw, sh, 0, new Color(0, 0, 0, (int)(180 * anim)).getRGB());

        float w = 380;
        float h = 40;
        float x = (sw - w) / 2;
        float y = (sh / 2) - 150 * anim;

        float scale = 0.9f + 0.1f * anim;
        r.pushScale(scale, scale, sw/2, y + h/2);

        r.shadow(x, y, w, h, 20, 2f, 1f, new Color(0,0,0, (int)(150 * anim)).getRGB());
        r.rect(x, y, w, h, 8, new Color(25, 25, 30, (int)(255 * anim)).getRGB());

        int accentColor = new Color(100, 160, 255, (int)(255 * anim)).getRGB();
        r.rectOutline(x, y, w, h, 8, accentColor, 1.5f);

        r.text(FontRegistry.INTER_MEDIUM, x + 15, y + h/2 + 3, 10, "Search", accentColor, "l");

        String cursor = (System.currentTimeMillis() % 1000 > 500) ? "_" : "";
        String displayText = searchQuery.isEmpty() ? "Search..." : searchQuery;
        int textColor = searchQuery.isEmpty()
                ? new Color(120, 120, 120, (int)(255 * anim)).getRGB()
                : new Color(255, 255, 255, (int)(255 * anim)).getRGB();

        r.text(FontRegistry.SF_REGULAR, x + 70, y + h/2 + 2, 9, displayText + (searchQuery.isEmpty() ? "" : cursor), textColor, "l");

        if (!searchQuery.isEmpty() && !searchResults.isEmpty()) {
            float resY = y + h + 6;
            float resH = 32;
            float totalListH = searchResults.size() * (resH + 2) + 6;

            r.rect(x, resY, w, totalListH, 6, new Color(25, 25, 30, (int)(245 * anim)).getRGB());

            int i = 0;
            for (SearchResult result : searchResults) {
                float itemY = resY + 4 + i * (resH + 2);
                boolean isSelected = (i == selectedSearchIndex);
                boolean isHovered = mx >= x + 4 && mx <= x + w - 4 && my >= itemY && my <= itemY + resH;

                if (isSelected || isHovered) {
                    if (isHovered) selectedSearchIndex = i;
                    r.rect(x + 4, itemY, w - 8, resH, 4, new Color(45, 45, 50, (int)(255 * anim)).getRGB());
                    r.rectOutline(x + 4, itemY, w - 8, resH, 4, new Color(100, 160, 255, (int)(100 * anim)).getRGB(), 1f);
                }

                Function mod = result.module;
                Setting<?> setting = result.setting;

                String title = setting == null ? mod.getName() : setting.getName();
                r.text(FontRegistry.INTER_MEDIUM, x + 12, itemY + 10, 8, title, -1, "l");

                String path = setting == null ? mod.getCategory().getName() + " > " + mod.getName() : mod.getName() + " > " + setting.getName();
                r.text(FontRegistry.SF_REGULAR, x + 12, itemY + 22, 6, path, new Color(150,150,150).getRGB(), "l");

                String rightText;
                int rightColor;

                if (setting == null) {
                    String bind = KeyNameUtil.getKeyName(mod.getKey());
                    String bindText = (bind == null || bind.isEmpty()) ? "" : " [" + bind + "]";
                    rightText = (mod.isToggled() ? "ON" : "OFF") + bindText;
                    rightColor = mod.isToggled()
                            ? new Color(100, 255, 140, (int)(255*anim)).getRGB()
                            : new Color(120, 120, 120, (int)(255*anim)).getRGB();
                } else {
                    if (setting instanceof BooleanSetting bs) {
                        rightText = bs.getValue() ? "TRUE" : "FALSE";
                        rightColor = bs.getValue() ? new Color(100, 160, 255).getRGB() : new Color(255, 100, 100).getRGB();
                    } else if (setting instanceof ModeSetting ms) {
                        rightText = ms.getValue();
                        rightColor = new Color(200, 200, 255).getRGB();
                    } else if (setting instanceof NumberSetting ns) {
                        rightText = String.valueOf(ns.getValue());
                        rightColor = new Color(255, 200, 100).getRGB();
                    } else {
                        rightText = "..."; rightColor = -1;
                    }
                }

                r.text(FontRegistry.INTER_MEDIUM, x + w - 12, itemY + resH/2 + 2, 7, rightText, rightColor, "r");
                i++;
            }
        }
        r.popScale();
    }


    private void renderAccountsButton(Renderer2D r, double mx, double my, float sw, float sh, float alpha) {
        accountsButtonX = sw - accountsButtonW - 14f;
        accountsButtonY = 14f;

        boolean hovered = mx >= accountsButtonX && mx <= accountsButtonX + accountsButtonW
                && my >= accountsButtonY && my <= accountsButtonY + accountsButtonH;

        int fill = hovered
                ? new Color(36, 36, 42, (int) (255 * alpha)).getRGB()
                : new Color(8, 8, 10, (int) (255 * alpha)).getRGB();
        int outline = hovered
                ? new Color(110, 170, 255, (int) (200 * alpha)).getRGB()
                : new Color(70, 70, 80, (int) (150 * alpha)).getRGB();

        r.shadow(accountsButtonX, accountsButtonY, accountsButtonW, accountsButtonH, 10, 1.3f, 1f,
                new Color(0, 0, 0, (int) (170 * alpha)).getRGB());
        r.rect(accountsButtonX, accountsButtonY, accountsButtonW, accountsButtonH, 8, fill);
        r.rectOutline(accountsButtonX, accountsButtonY, accountsButtonW, accountsButtonH, 8, outline, 1.2f);
        r.text(FontRegistry.INTER_MEDIUM, accountsButtonX + accountsButtonW / 2f, accountsButtonY + 15.5f, 8,
                "Accounts", Color.WHITE.getRGB(), "c");
    }

    private void handleSearchClick(SearchResult result, int btn) {
        if (btn == 0) {
            if (result.setting == null) {
                result.module.toggle();
            } else {

                if (result.setting instanceof BooleanSetting bs) bs.toggle();
                else if (result.setting instanceof ModeSetting ms) {

                    List<String> modes = ms.getModes();
                    int cur = modes.indexOf(ms.getValue());
                    ms.setValue(modes.get((cur + 1) % modes.size()));
                }

            }
        } else if (btn == 1) {

            float popupX = (mc.getWindow().getScaledWidth() + 380) / 2f + 5;
            float popupY = (mc.getWindow().getScaledHeight() / 2f) - 150;

            settingsPopup.open(result.module, popupX, popupY);
        } else if (btn == 2) {
            bindPopup.open(result.module, (float)mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth(), (float)mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight());
        }
    }

    private void updateSearchResults() {
        searchResults.clear();
        selectedSearchIndex = 0;
        if (searchQuery.isEmpty()) return;

        String q = searchQuery.toLowerCase();

        for (Function mod : allModules) {
            if (mod.getName().toLowerCase().contains(q)) {
                searchResults.add(new SearchResult(mod, null));
            }
            for (Setting<?> setting : mod.getSettings()) {
                if (setting.getName().toLowerCase().contains(q)) {
                    searchResults.add(new SearchResult(mod, setting));
                }
            }
        }

        if (searchResults.size() > 8) {
            List<SearchResult> sub = new ArrayList<>(searchResults.subList(0, 8));
            searchResults.clear();
            searchResults.addAll(sub);
        }
    }

    @Subscribe
    public void onPress(EventPress e) {
        if (e.getAction() == 0) return;
        int key = e.getKey();


        for (Panel panel : panels) {
            for (ModuleButton button : panel.getButtons()) {
                if (button.isExpanded()) {
                    for (SettingElement element : button.getSettingElements()) {
                        if (element instanceof TextElement textElement) {
                            if (textElement.isListening()) {
                                textElement.handleKeyPress(key);
                                return;
                            }
                        }
                    }
                }
            }
        }


        if (settingsPopup.isActive()) {
            for (SettingElement element : settingsPopup.getElements()) {
                if (element instanceof TextElement textElement) {
                    if (textElement.isListening()) {
                        textElement.handleKeyPress(key);
                        return;
                    }
                }
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                settingsPopup.close();
                return;
            }
        }


        if (bindPopup.isActive()) {
            bindPopup.keyTyped(key);
            return;
        }


        boolean ctrl = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;

        if (!isSearching && ctrl && key == GLFW.GLFW_KEY_F) {
            isSearching = true;
            searchQuery = "";
            searchResults.clear();
            selectedSearchIndex = 0;
            return;
        }

        if (isSearching) {
            switch (key) {
                case GLFW.GLFW_KEY_ESCAPE -> isSearching = false;
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (!searchQuery.isEmpty()) {
                        searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                        updateSearchResults();
                    }
                }
                case GLFW.GLFW_KEY_DOWN -> {
                    if (selectedSearchIndex < searchResults.size() - 1) selectedSearchIndex++;
                }
                case GLFW.GLFW_KEY_UP -> {
                    if (selectedSearchIndex > 0) selectedSearchIndex--;
                }
                case GLFW.GLFW_KEY_ENTER -> {
                    if (!searchResults.isEmpty() && selectedSearchIndex < searchResults.size()) {
                        handleSearchClick(searchResults.get(selectedSearchIndex), 0);
                    }
                }
                default -> {
                    char c = keyToChar(key);
                    if (c != 0) {
                        searchQuery += c;
                        updateSearchResults();
                    }
                }
            }
            return;
        }


        if (key == GLFW.GLFW_KEY_ESCAPE) {
            toggle();
            return;
        }


        if (isOpen && GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            float moveAmount = 10f;
            switch (key) {
                case GLFW.GLFW_KEY_UP -> movePanels(0, -moveAmount);
                case GLFW.GLFW_KEY_DOWN -> movePanels(0, moveAmount);
                case GLFW.GLFW_KEY_LEFT -> movePanels(-moveAmount, 0);
                case GLFW.GLFW_KEY_RIGHT -> movePanels(moveAmount, 0);
            }
        }
    }

    private void movePanels(float dx, float dy) {
        for (Panel panel : panels) {
            panel.currentX += dx;
            panel.currentY += dy;
            panel.targetX = panel.currentX;
            panel.targetY = panel.currentY;
        }
    }

    private void renderDescription(Renderer2D r, FontObject f, double mx, double my) {

        for (Panel p : panels) {
            Function hovered = p.getHoveredModule(mx, my);
            if (hovered != null && hovered.getDesc() != null && !hovered.getDesc().isEmpty()) {
                float descX = (float) mx + 10;
                float descY = (float) my;
                r.text(f, descX + 4, descY + 4, 7, hovered.getDesc(), Color.WHITE.getRGB(), "l");
                break;
            }
        }


        String text = "Ctrl + F открывает меню поиска";


        float screenWidth = (float) mc.getWindow().getScaledWidth();
        float screenHeight = (float) mc.getWindow().getScaledHeight();


        float popupX = screenWidth ;
        float popupY = screenHeight / 2;


        r.gradientText(
                f,
                popupX,
                popupY,
                15,
                text,
                new Color(182, 181, 181).getRGB(), new Color(96, 93, 93).getRGB(),
                "c"
        );
    }

    public void handleMouseClick(double mx, double my, int btn) {
        if (!isOpen) return;

        boolean onAccounts = mx >= accountsButtonX && mx <= accountsButtonX + accountsButtonW
                && my >= accountsButtonY && my <= accountsButtonY + accountsButtonH;
        if (onAccounts && btn == 0) {
            isOpen = false;
            onDisable();
            mc.setScreen(new AccountManagerScreen(mc.currentScreen));
            return;
        }

        if (bindPopup.isActive()) {
            bindPopup.mouseClicked(mx, my, btn);
            return;
        }
        if (settingsPopup.isActive()) {
            settingsPopup.mouseClicked(mx, my, btn);

            if (settingsPopup.isActive()) return;
        }

        if (isSearching) {
            if (!searchQuery.isEmpty()) {
                float w = 380;
                float h = 40;
                float sw = mc.getWindow().getScaledWidth();
                float sh = mc.getWindow().getScaledHeight();
                float x = (sw - w) / 2;
                float y = (sh / 2) - 150;
                float resY = y + h + 6;
                float resH = 32;

                for (SearchResult result : searchResults) {
                    if (mx >= x + 4 && mx <= x + w - 4 && my >= resY && my <= resY + resH) {
                        handleSearchClick(result, btn);
                        return;
                    }
                    resY += resH + 2;
                }
            }


            float searchX = (mc.getWindow().getScaledWidth() - 380) / 2f;
            float searchY = (mc.getWindow().getScaledHeight() / 2f) - 150;
            boolean onSearch = mx >= searchX && mx <= searchX + 380 && my >= searchY && my <= searchY + 40;

            if (!onSearch && btn == 0) {
                isSearching = false;
                searchQuery = "";
                settingsPopup.close();
            }
            return;
        }

        for (Panel panel : panels) {
            panel.mouseClicked(mx, my, btn);
        }
    }

    public void handleMouseDrag(double mx, double my, int btn, double dx, double dy) {
        if (!isOpen) return;


        if (settingsPopup.isActive()) {
            settingsPopup.mouseDragged(mx, my, btn, dx, dy);
            return;
        }

        if (isSearching || bindPopup.isActive()) return;
        for (Panel panel : panels) panel.mouseDragged(mx, my, btn, dx, dy);
    }

    public void handleMouseRelease(double mx, double my, int btn) {
        if (!isOpen) return;

        if (settingsPopup.isActive()) {
            settingsPopup.mouseReleased(mx, my, btn);
        }

        if (isSearching) return;
        for (Panel panel : panels) panel.mouseReleased(mx, my, btn);
    }

    public void handleMouseScroll(double amount) {
        if (!isOpen || isSearching || bindPopup.isActive() || settingsPopup.isActive()) return;
        if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            movePanels(0, (float) (amount * 15));
        }
    }

    private char keyToChar(int key) {
        boolean shift = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) return shift ? (char)('A' + (key - GLFW.GLFW_KEY_A)) : (char)('a' + (key - GLFW.GLFW_KEY_A));
        if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) return (char)('0' + (key - GLFW.GLFW_KEY_0));
        if (key == GLFW.GLFW_KEY_SPACE) return ' ';
        if (key == GLFW.GLFW_KEY_MINUS) return shift ? '_' : '-';
        if (key == GLFW.GLFW_KEY_PERIOD) return '.';
        return 0;
    }

    private record SearchResult(Function module, Setting<?> setting) {}
}