package org.atcraftmc.starlight.utilities.font;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public final class ShapeSampleRenderer implements ParticleFontRenderer {
    @Override
    public void render(ParticleFontComponent component) {
        var font = component.font();
        var text = component.text();
        var baseX = component.baseX();
        var baseY = component.baseY();
        var size = component.size();
        var density = component.density();

        if (density <= 0) {
            throw new IllegalArgumentException("dpb must be > 0");
        }


        // 1. 构造 GlyphVector 与 Outline（字体轮廓，包含曲线）
        var derived = font.deriveFont((float) (size * density));
        var context = new FontRenderContext(null, true, true);
        var gv = derived.createGlyphVector(context, text);
        var outline = gv.getOutline();

        // 2. 把 outline 移到 (0,0) 以便于网格采样
        Rectangle2D bounds2 = outline.getBounds2D();
        double minX = bounds2.getX();
        double minY = bounds2.getY();
        double w = bounds2.getWidth();
        double h = bounds2.getHeight();

        AffineTransform toOrigin = AffineTransform.getTranslateInstance(-minX, -minY);
        Shape transShape = toOrigin.createTransformedShape(outline);

        // 3. Area 用于 contains() 检查（处理曲线 / 孔洞 / 复合字形）
        Area area = new Area(transShape);

        // 4. 在像素网格上采样：我们在像素中心 (i+0.5, j+0.5) 检查是否在 area 内
        //    字体坐标的单位是“字体像素/矢量单位”。我们把每个像素映射到 Minecraft 单位为 (1.0 / dpb) 方块。
        //    worldX = baseX + (pixelX + 0.5) / dpb
        //    worldY = baseY + (pixelY + 0.5) / dpb  （但需要把 AWT 的 y 轴向下与 Minecraft 向上对齐）
        int pixelW = (int) Math.ceil(w);
        int pixelH = (int) Math.ceil(h);

        // 可选优化：采样步长（以像素为单位）。step=1 表示每个像素采样，step=2 表示每隔 1 像素采样一次，减少粒子。
        int sampleStep = 1; // 你可以改成 2、3 以减少粒子数量

        // 5. 遍历像素网格并 spawn 粒子（注意 Y 轴翻转）
        for (int py = 0; py < pixelH; py += sampleStep) {
            double sampleY = py + 0.5;
            for (int px = 0; px < pixelW; px += sampleStep) {
                double sampleX = px + 0.5;

                // area.contains 使用字体坐标系（我们已把 shape 移到左上角 0,0）
                if (area.contains(sampleX, sampleY)) {
                    // 将像素坐标映射到 Minecraft 世界坐标：
                    double worldPX = baseX + (sampleX) / (density / size);
                    // AWT Y 从上往下，字体 bounds 的原点在上侧 => 把它翻转到 Minecraft 上（从下往上）
                    double worldPY = baseY + ((pixelH - sampleY)) / (density / size);

                    component.drawPixel(worldPX, worldPY);
                }
            }
        }
    }
}
