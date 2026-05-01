package kefirdlc.dev.util.render.utils;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.util.wrapper.Wrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.awt.Color;

@UtilityClass
public class ChatUtils implements Wrapper {

    public void sendMessage(String message) {
        if (mc.player == null || mc.world == null ) return;
        MutableText text = Text.literal("");
        for (int i = 0; i < "KefirDLC".length(); i++) {
            text.append(Text.literal("KefirDLC".charAt(i) + "")
                    .setStyle(Style.EMPTY
                            .withBold(true)
                            .withColor(TextColor.fromRgb(ColorUtils.gradient(ColorUtils.getGlobalColor(), Color.WHITE, (float) i / "KefirDLC".length()).getRGB()))
                    )
            );
        }

        text.append(Text.literal(" -> ")
                .setStyle(Style.EMPTY
                        .withBold(false)
                        .withColor(TextColor.fromRgb(new Color(200, 200, 200).getRGB()))
                )
        );

        text.append(Text.literal(message)
                .setStyle(Style.EMPTY
                        .withBold(false)
                        .withColor(TextColor.fromRgb(new Color(200, 200, 200).getRGB()))
                )
        );

        mc.player.sendMessage(text, false);
    }
}