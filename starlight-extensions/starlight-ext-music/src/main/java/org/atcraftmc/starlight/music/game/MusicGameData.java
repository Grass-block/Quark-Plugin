package org.atcraftmc.starlight.music.game;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public final class MusicGameData {
    public String id;
    public String music;
    public MusicGameMeta meta;
    public Map<Integer, String> tickEvents = new HashMap<>();
    public Map<String, MusicGameNote> notes = new HashMap<>(); // key = "x,y,z"


    public static MusicGameData deserialize(JsonObject obj) {
        var data = new MusicGameData();

        data.id = obj.get("id").getAsString();
        data.music = obj.get("music").getAsString();

        // meta
        var metaObj = obj.getAsJsonObject("meta");
        data.meta = new MusicGameMeta();
        data.meta.name = metaObj.get("name").getAsString();
        data.meta.author = metaObj.get("author").getAsString();
        data.meta.gameAuthor = metaObj.get("game_author").getAsString();

        // tick-events
        var teObj = obj.getAsJsonObject("tick-events");
        for (var key : teObj.keySet()) {
            int tick = Integer.parseInt(key);
            data.tickEvents.put(tick, teObj.get(key).getAsString());
        }

        // notes (key = "x,y,z")
        var notesObj = obj.getAsJsonObject("notes");
        for (var key : notesObj.keySet()) {
            var nObj = notesObj.getAsJsonObject(key);
            var note = new MusicGameNote();
            note.tick = nObj.get("tick").getAsInt();
            note.type = nObj.get("type").getAsString();
            data.notes.put(key, note);
        }

        return data;
    }

    public static JsonObject serialize(MusicGameData data) {
        JsonObject obj = new JsonObject();

        obj.addProperty("id", data.id);
        obj.addProperty("music", data.music);

        // meta
        JsonObject metaObj = new JsonObject();
        metaObj.addProperty("name", data.meta.name);
        metaObj.addProperty("author", data.meta.author);
        metaObj.addProperty("game", data.meta.gameAuthor);
        obj.add("meta", metaObj);

        // tick-events
        JsonObject teObj = new JsonObject();
        for (Map.Entry<Integer, String> e : data.tickEvents.entrySet()) {
            teObj.addProperty(String.valueOf(e.getKey()), e.getValue());
        }
        obj.add("tick-events", teObj);

        // notes
        JsonObject notesObj = new JsonObject();
        for (Map.Entry<String, MusicGameNote> e : data.notes.entrySet()) {
            JsonObject nObj = new JsonObject();
            nObj.addProperty("tick", e.getValue().tick);
            nObj.addProperty("type", e.getValue().type);
            notesObj.add(e.getKey(), nObj); // key 直接是 "x,y,z"
        }
        obj.add("notes", notesObj);

        return obj;
    }
}

