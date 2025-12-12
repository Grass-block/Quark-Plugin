package org.atcraftmc.starlight.utilities.font;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CurveFillingV2 implements ParticleFontRenderer {
    static void renderTextParticles(ParticleFontComponent component) {
        // ==============================
        // 计算 spacing
        // ==============================
        double spacing = Math.max(0.02, 1.0 / (component.density()));

        // ==============================
        // 设置 flatness 预设
        // ==============================
        double FLATNESS_LOW = component.size() * 0.015;
        double FLATNESS_MED = component.size() * 0.03;
        double FLATNESS_HIGH = component.size() * 0.06;

        double flatness = FLATNESS_MED;

        // ==============================
        // 生成文本 Shape
        // ==============================
        FontRenderContext frc = new FontRenderContext(null, true, true);
        GlyphVector gv = component.font().deriveFont(component.size()).createGlyphVector(frc, component.text());
        Shape shape = gv.getOutline();

        // 获取边界，用于扫描线范围
        Rectangle bounds = shape.getBounds();

        var width = bounds.getWidth();

        // ==============================
        // 扫描线：按 spacing 遍历 Y
        // ==============================
        for (double py = bounds.getMinY(); py <= bounds.getMaxY(); py += spacing) {

            List<Double> xIntersections = new ArrayList<>();

            PathIterator it = shape.getPathIterator(null, flatness);
            double[] c = new double[6];

            double sx = 0, sy = 0;
            double px0 = 0, py0 = 0;
            boolean first = true;

            while (!it.isDone()) {
                int type = it.currentSegment(c);

                double x1 = c[0];
                double y1 = c[1];

                if (type == PathIterator.SEG_MOVETO) {
                    px0 = x1;
                    py0 = y1;
                    sx = x1;
                    sy = y1;
                    first = false;

                } else if (type == PathIterator.SEG_LINETO) {
                    addIntersection(px0, py0, x1, y1, py, xIntersections);
                    px0 = x1;
                    py0 = y1;

                } else if (type == PathIterator.SEG_CLOSE) {
                    addIntersection(px0, py0, sx, sy, py, xIntersections);
                }

                it.next();
            }

            // 必须排序，否则出现“反转”
            Collections.sort(xIntersections);

            // 成对填充
            for (int i = 0; i + 1 < xIntersections.size(); i += 2) {
                double xStart = xIntersections.get(i);
                double xEnd = xIntersections.get(i + 1);

                for (double px = xStart; px <= xEnd; px += spacing) {
                    component.drawPixel(px - width / 2, py);
                }
            }
        }
    }

    private static void addIntersection(
            double x0, double y0, double x1, double y1, double scanY, List<Double> list
    ) {
        // 排除水平线段（避免重复或浮点误差）
        if ((scanY < Math.min(y0, y1)) || (scanY > Math.max(y0, y1))) {
            return;
        }

        if (y0 == y1) {
            return;
        }

        // 计算交点 X
        double t = (scanY - y0) / (y1 - y0);
        if (t >= 0 && t <= 1) {
            double x = x0 + t * (x1 - x0);
            list.add(x);
        }
    }

    @Override
    public void render(ParticleFontComponent component) {
        renderTextParticles(component);
    }
}
