package org.atcraftmc.starlight.utilities.font;

import java.util.HashMap;
import java.util.Map;

public interface ParticleFontRenderer {
    Map<String, ParticleFontRenderer> RENDERERS = new HashMap<>();

    static void init(){
        RENDERERS.put("curve-filling-v2", new CurveFillingV2());
        RENDERERS.put("outline-v2", new OutlineV2());
    }

    static ParticleFontRenderer getInstance(String id){
        if(RENDERERS.isEmpty()){
            init();
        }

        return RENDERERS.get(id);
    }

    void render(ParticleFontComponent component);
}
