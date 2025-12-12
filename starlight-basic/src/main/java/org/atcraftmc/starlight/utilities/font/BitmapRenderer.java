package org.atcraftmc.starlight.utilities.font;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

interface BitmapRenderer {
    int RESOLUTION_SCALE = 2;
    BufferedImage V_HOLDER = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    static BufferedImage generate(Font fontFamily, String text, float size) {
        var scale = 1.0f;
        var v_graphics = V_HOLDER.createGraphics();
        var font = createStyledFont(fontFamily.deriveFont((size * RESOLUTION_SCALE * scale)), false, false, false, false);

        v_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        v_graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        v_graphics.setFont(font);

        var fm = v_graphics.getFontMetrics(font);
        var width = fm.stringWidth(text);
        var height = fm.getHeight();

        if (width == 0 || height == 0) {
            throw new IllegalArgumentException("WTF");
        }

        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        g.setFont(font);
        g.drawString(text, 0, (int) (size * RESOLUTION_SCALE * scale));

        return image;
    }

    static void drawBufferedImage(BufferedImage i, World world, double x, double y, double z, int size, int paintSize) {

        int width = i.getWidth();
        int height = i.getHeight();

        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {

                int argb = i.getRGB(xx, yy);

                // 提取 alpha 通道（0-255）
                int alpha = (argb >> 24) & 0xFF;

                if (alpha == 0) {
                    continue;
                }

                assert world != null;

                var dx = xx * size / paintSize;
                var dy = yy * size / paintSize;

                Location loc = new Location(world, x + dx, y - dy, z);

                world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
            }
        }
    }

    static Font createStyledFont(Font baseFont, boolean bold, boolean italic, boolean underline, boolean strikethrough) {
        Map<TextAttribute, Object> attributes = new HashMap<>(baseFont.getAttributes());

        // 设置粗体和斜体
        int style = Font.PLAIN;
        if (bold) {
            style |= Font.BOLD;
        }
        if (italic) {
            style |= Font.ITALIC;
        }
        baseFont = baseFont.deriveFont(style);

        // 设置下划线
        if (underline) {
            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }

        // 设置删除线
        if (strikethrough) {
            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        }

        // 创建并返回新的字体
        return baseFont.deriveFont(attributes);
    }
}