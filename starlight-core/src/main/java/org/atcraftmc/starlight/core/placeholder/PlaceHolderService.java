package org.atcraftmc.starlight.core.placeholder;

import org.bukkit.entity.Player;
import org.atcraftmc.starlight.data.GlobalVars;
import org.atcraftmc.starlight.framework.service.SLService;
import org.atcraftmc.starlight.framework.service.Service;
import org.atcraftmc.starlight.framework.service.ServiceInject;
import org.atcraftmc.qlib.texts.placeholder.GloballyPlaceHolder;
import org.atcraftmc.qlib.texts.placeholder.ObjectivePlaceHolder;
import org.atcraftmc.qlib.texts.placeholder.PlaceHolder;
import org.atcraftmc.qlib.texts.placeholder.StringExtraction;

import java.util.Map;
import java.util.regex.Pattern;

@SLService(id = "place-holder")
public interface PlaceHolderService extends Service {
    StringExtraction PATTERN = new StringExtraction(Pattern.compile("\\{#(.*?)}"), 2, 1);
    GlobalVars EXTERNAL_VARS = new GlobalVars();

    ObjectivePlaceHolder<Player> PLAYER = PlaceHolders.player();
    GloballyPlaceHolder GLOBAL_VAR = new GloballyPlaceHolder();
    GloballyPlaceHolder SERVER = PlaceHolders.server();
    GloballyPlaceHolder TEXT_STYLE = PlaceHolders.chat();

    PAPIWrapper PAPI_WRAPPER = PAPIWrapper.getInstance();

    @ServiceInject
    static void start() {
        reloadExternal();
    }

    static void reloadExternal() {
        GLOBAL_VAR.clear();

        Map<String, String> map = EXTERNAL_VARS.loadMap();

        for (String key : map.keySet()) {
            GLOBAL_VAR.register(key, map.get(key));
        }
    }

    static String format(String input) {
        return PAPI_WRAPPER.handle(PlaceHolder.format(PATTERN, input, GLOBAL_VAR, SERVER, TEXT_STYLE));
    }

    static String formatPlayer(Player player, String input) {
        return PAPI_WRAPPER.handlerPlayer(player, PlaceHolder.formatObjective(PATTERN, player, input, PLAYER));
    }

    static String format(String s, GloballyPlaceHolder... placeHolders) {
        return PAPI_WRAPPER.handle(PlaceHolder.format(PATTERN, s, placeHolders));
    }
}
