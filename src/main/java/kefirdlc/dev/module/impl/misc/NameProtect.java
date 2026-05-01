package kefirdlc.dev.module.impl.misc;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.event.impl.render.TextFactoryEvent;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.module.setting.impl.BooleanSetting;
import kefirdlc.dev.module.setting.impl.StringSetting;
import kefirdlc.dev.util.others.Friends;

@ModuleInfo(name = "NameProtect", category = Category.MISC, desc = "Защищает ваш ник и ники друзей от показа в чате")
public class NameProtect extends Function {



    StringSetting nameSetting = new StringSetting("Ник","efef");
    BooleanSetting friendsSetting = new BooleanSetting("Friends",false);

    public NameProtect(){
        addSettings(nameSetting, friendsSetting);
    }

    @Subscribe
    public void onTextFactory(TextFactoryEvent e) {
        e.replaceText(mc.getSession().getUsername(), nameSetting.getValue());
        if (friendsSetting.getValue()) Friends.getFriends().forEach(friend -> e.replaceText(friend.getName(), nameSetting.getValue()));
    }
}
