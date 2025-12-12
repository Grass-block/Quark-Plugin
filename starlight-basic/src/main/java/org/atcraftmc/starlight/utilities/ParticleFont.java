package org.atcraftmc.starlight.utilities;

import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.assertion.NumberLimitation;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.starlight.data.assets.AssetGroup;
import org.atcraftmc.starlight.framework.module.SLCommandModule;
import org.atcraftmc.starlight.utilities.font.ParticleFontComponent;
import org.atcraftmc.starlight.utilities.font.ParticleFontRenderer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationModule(id = "particle-font")
@QuarkCommand(name = "particle-font")
public final class ParticleFont extends SLCommandModule {
    public static final Map<String, String> PRESET_DOWNLOADS = Map.of(
            "霞鹜文楷-Lite",
            "https://release-assets.githubusercontent.com/github-production-release-asset/415519873/1523d0cf-f31f-4bcd-bec6-55a732ec4379?sp=r&sv=2018-11-09&sr=b&spr=https&se=2025-12-06T11%3A13%3A06Z&rscd=attachment%3B+filename%3DLXGWWenKaiLite-Medium.ttf&rsct=application%2Foctet-stream&skoid=96c2d410-5711-43a1-aedd-ab1947aa7ab0&sktid=398a6654-997b-47e9-b12b-9515b896b4de&skt=2025-12-06T10%3A12%3A22Z&ske=2025-12-06T11%3A13%3A06Z&sks=b&skv=2018-11-09&sig=aXc3FPdtYLLp3EyAAnZtDO%2FApeaDiRx35qMNXSUdraY%3D&jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmVsZWFzZS1hc3NldHMuZ2l0aHVidXNlcmNvbnRlbnQuY29tIiwia2V5Ijoia2V5MSIsImV4cCI6MTc2NTAxODkzNCwibmJmIjoxNzY1MDE3MTM0LCJwYXRoIjoicmVsZWFzZWFzc2V0cHJvZHVjdGlvbi5ibG9iLmNvcmUud2luZG93cy5uZXQifQ.O-msXMe8KYW_cgHG3dZsggJmly5MqmFq59L81vQ93OQ&response-content-disposition=attachment%3B%20filename%3DLXGWWenKaiLite-Medium.ttf&response-content-type=application%2Foctet-stream"
    );
    private final Map<String, Font> cache = new HashMap<>();
    @Inject("fonts")
    private AssetGroup fonts;

    @Override
    public void execute(CommandExecution context) {
        switch (context.requireEnum(0, "render", "list", "trim")) {
            case "trim" -> this.language().item("trim").send(context.getSender(), this.fonts.trim());
            case "render" -> {
                if (this.fonts.list().isEmpty()) {
                    this.language().item("no-fonts").send(context.getSender());
                }

                var font = context.requireArgumentAt(1);
                var renderer = context.requireArgumentAt(2);
                var size = context.requireArgumentFloat(3, NumberLimitation.moreThan(0));
                var density = context.requireArgumentFloat(4, NumberLimitation.bound(0.1, 111));
                var world = Bukkit.getWorld(context.requireArgumentAt(5));
                var v = context.requireCoordinate(6);
                var loc = new Location(world, v.getX(), v.getY(), v.getZ());

                loc.setYaw(context.requireArgumentFloat(9));
                loc.setPitch(context.requireArgumentFloat(10));
                var zRot = context.requireArgumentFloat(11);

                var content = context.requireRemainAsParagraph(12, true);

                var f = this.cache.computeIfAbsent(font, (s) -> {
                    try {
                        return Font.createFont(Font.TRUETYPE_FONT, this.fonts.getFile(font));
                    } catch (FontFormatException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                var request = new ParticleFontComponent(f, size, density, loc, zRot, content);

                ParticleFontRenderer.getInstance(renderer).render(request);

                this.language().item("done").send(context.getSender(), content, request.getCount());
            }
            case "list" -> {

            }
            case "download" -> {
                var link = context.requireArgumentAt(1);
                if (PRESET_DOWNLOADS.containsKey(link)) {
                    var l = PRESET_DOWNLOADS.get(link);
                }
                if (!link.startsWith("http")) {
                    this.language().item("download-bad-text").send(context.getSender());
                    return;
                }

                //todo:download
            }
        }
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        ParticleFontRenderer.getInstance("");

        suggestion.suggest(0, "trim", "render", "list");
        suggestion.matchArgument(0, "download", (c) -> c.suggest(1, PRESET_DOWNLOADS.keySet()));
        suggestion.matchArgument(0, "render", (c) -> {
            c.suggest(1, this.fonts.list());
            c.suggest(2, ParticleFontRenderer.RENDERERS.keySet());
            c.suggest(3, "<size>");
            c.suggest(4, "<density>");
            c.suggest(5, "<world>");
            c.suggest(5, Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toSet()));
            c.suggest(6, "[x]");
            c.suggest(7, "[y]");
            c.suggest(8, "[z]");
            c.suggest(9, "[xr]");
            c.suggest(10, "[yr]");
            c.suggest(11, "[zr]");
            c.suggest(12, "[content...]");
        });
    }


}
