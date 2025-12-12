package org.atcraftmc.starlight.utilities.font;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;

public final class OutlineV2 implements ParticleFontRenderer {
    public static final float SAMPLE_FLATNESS = 0.02f;

    public static void drawTextAsParticles(ParticleFontComponent component) {
        final var font = component.font();
        final var text = component.text();
        final var size = component.size();
        final var density = component.density();
        final var largeFont = font.deriveFont(size);
        final var context = new FontRenderContext(null, true, true);

        //由 GlyphVector 获取轮廓 Shape
        var gv = largeFont.createGlyphVector(context, text);
        var outline = gv.getOutline(); // 以字体单位（基于 baseFontSize）为坐标

        var bounds = outline.getBounds2D();
        var outlineHeight = bounds.getHeight();
        if (outlineHeight <= 0.000001) {
            return;
        }
        double scale = size / outlineHeight; // 把 outline 高度映射到目标米数

        var at = new AffineTransform();
        at.translate(-bounds.getX() * scale, 0);

        Shape scaledShape = at.createTransformedShape(outline);

        // 6) 用 FlatteningPathIterator 将曲线近似为直线段（便于采样）
        // 单位：米（输出坐标单位已是米），更小越精确但越多点
        var pit = new FlatteningPathIterator(scaledShape.getPathIterator(null), SAMPLE_FLATNESS);
        var width = bounds.getWidth();

        // 7) 遍历线段并按 density 采样
        var coords = new double[2];
        // 当前 sub-path 的起点（用于 close）
        var moveX = 0d;
        var moveY = 0d;

        // 上一个点
        var lastX = 0d;
        var lastY = 0d;

        int segType;

        while (!pit.isDone()) {
            segType = pit.currentSegment(coords);
            if (segType == PathIterator.SEG_MOVETO) {
                lastX = coords[0];
                lastY = coords[1];
                moveX = lastX;
                moveY = lastY;
            } else if (segType == PathIterator.SEG_LINETO) {
                sample(lastX, lastY, coords[0], coords[1], width, density, component);

                lastX = coords[0];
                lastY = coords[1];
            } else if (segType == PathIterator.SEG_CLOSE) {
                sample(lastX, lastY, moveX, moveY, density, width, component);

                lastX = moveX;
                lastY = moveY;
            }
            pit.next();
        }
    }

    static void sample(double x1, double y1, double x2, double y2, double w, double density, ParticleFontComponent component) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double segLen = Math.hypot(dx, dy);
        if (segLen > 1e-9) {
            int count = Math.max(1, (int) Math.ceil(segLen * density));
            for (int i = 0; i <= count; i++) {
                double t = (double) i / (double) count;
                double px = x1 + dx * t;
                double py = y1 + dy * t;
                component.drawPixel(px - w / 2, py - 1);
            }
        }
    }

    @Override
    public void render(ParticleFontComponent component) {
        drawTextAsParticles(component);
    }
}


