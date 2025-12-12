package org.atcraftmc.starlight.music;

import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.starlight.music.resolve.MusicResolveRequest;

import java.util.List;
import java.util.Objects;

public interface MusicCommandDispatcher {
    static MusicResolveRequest selectMusic(CommandExecution context, int commandOffset) {
        var args = context.getArgs();

        var pitch = 0;
        var speedMod = 1.0f;
        var legacy = !List.of(args).contains("-legacy");
        var interpolation = 0;

        for (String s : args) {
            if (s.startsWith("-p:")) {
                pitch = Integer.parseInt(s.replace("-p:", ""));
            }
            if (s.startsWith("-s:")) {
                speedMod = Float.parseFloat(s.replace("-s:", ""));
            }
            if (s.startsWith("-i:")) {
                interpolation = s.replace("-i:", "").equals("smart") ? 1 : 2;
            }
        }

        String music;

        if (Objects.equals(args[commandOffset + 1], "random")) {
            music = MusicService.instance().random();
        } else {
            music = args[commandOffset + 1];
        }

        if (!MusicService.instance().list().contains(music)) {
            throw new IllegalArgumentException(MusicService.NOT_FOUND);
        }

        return new MusicResolveRequest(context.getSender().getName(), music, pitch, legacy, speedMod, interpolation);
    }

    static void suggestMusic(CommandSuggestion ctx, int base) {
        ctx.suggest(base, MusicService.instance().list());
        ctx.suggest(base, "random");

        for (var i = base + 1; i < base + 7; i++) {
            ctx.suggest(i, "-p:0");
            ctx.suggest(i, "-p:12");
            ctx.suggest(i, "-p:-12");
            ctx.suggest(i, "-s:1");
            ctx.suggest(i, "-s:1.5");
            ctx.suggest(i, "-s:0.5");
            ctx.suggest(i, "-legacy");
            ctx.suggest(i, "-i:on");
            ctx.suggest(i, "-i:smart");
        }
    }

    static void suggest(CommandSuggestion suggestion, int base) {
        suggestion.suggest(base, "play", "pause", "resume", "cancel", "gui", "save-defaults", "trim");
        suggestion.matchArgument(base, "play", (ctx) -> suggestMusic(ctx, base + 1));
    }

}
