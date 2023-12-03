//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tbstcraft.quark.util;

import java.util.Objects;

public class AABB {
    private final double epsilon = 0.0;
    public double x0;
    public double y0;
    public double z0;
    public double x1;
    public double y1;
    public double z1;

    public AABB(double x0, double y0, double z0, double x1, double y1, double z1) {
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
    }

    public AABB(AABB aabb) {
        this.x0 = aabb.x0;
        this.y0 = aabb.y0;
        this.z0 = aabb.z0;
        this.x1 = aabb.x1;
        this.y1 = aabb.y1;
        this.z1 = aabb.z1;
    }

    public AABB expand(double xa, double ya, double za) {
        double xa2 = Math.abs(xa);
        double ya2 = Math.abs(ya);
        double za2 = Math.abs(za);
        double _x0 = this.x0 - xa2;
        double _y0 = this.y0 - ya2;
        double _z0 = this.z0 - za2;
        double _x1 = this.x1 + xa2;
        double _y1 = this.y1 + ya2;
        double _z1 = this.z1 + za2;
        return new AABB(_x0, _y0, _z0, _x1, _y1, _z1);
    }

    public AABB grow(double xa, double ya, double za) {
        double _x0 = this.x0 - xa;
        double _y0 = this.y0 - ya;
        double _z0 = this.z0 - za;
        double _x1 = this.x1 + xa;
        double _y1 = this.y1 + ya;
        double _z1 = this.z1 + za;
        return new AABB(_x0, _y0, _z0, _x1, _y1, _z1);
    }

    public AABB cloneMove(double xa, double ya, double za) {
        return new AABB(this.x0 + za, this.y0 + ya, this.z0 + za, this.x1 + xa, this.y1 + ya, this.z1 + za);
    }

    public double clipXCollide(AABB c, double xa) {
        if (!(c.y1 <= this.y0) && !(c.y0 >= this.y1)) {
            if (!(c.z1 <= this.z0) && !(c.z0 >= this.z1)) {
                double var10000;
                double max;
                if (xa > 0.0 && c.x1 <= this.x0) {
                    var10000 = this.x0 - c.x1;
                    Objects.requireNonNull(this);
                    if ((max = var10000 - 0.0) < xa) {
                        xa = max;
                    }
                }

                if (xa < 0.0 && c.x0 >= this.x1) {
                    var10000 = this.x1 - c.x0;
                    Objects.requireNonNull(this);
                    if ((max = var10000 + 0.0) > xa) {
                        xa = max;
                    }
                }

                return xa;
            } else {
                return xa;
            }
        } else {
            return xa;
        }
    }

    public double clipYCollide(AABB c, double ya) {
        if (!(c.x1 <= this.x0) && !(c.x0 >= this.x1)) {
            if (!(c.z1 <= this.z0) && !(c.z0 >= this.z1)) {
                double var10000;
                double max;
                if (ya > 0.0 && c.y1 <= this.y0) {
                    var10000 = this.y0 - c.y1;
                    Objects.requireNonNull(this);
                    if ((max = var10000 - 0.0) < ya) {
                        ya = max;
                    }
                }

                if (ya < 0.0 && c.y0 >= this.y1) {
                    var10000 = this.y1 - c.y0;
                    Objects.requireNonNull(this);
                    if ((max = var10000 + 0.0) > ya) {
                        ya = max;
                    }
                }

                return ya;
            } else {
                return ya;
            }
        } else {
            return ya;
        }
    }

    public double clipZCollide(AABB c, double za) {
        if (!(c.x1 <= this.x0) && !(c.x0 >= this.x1)) {
            if (!(c.y1 <= this.y0) && !(c.y0 >= this.y1)) {
                double var10000;
                double max;
                if (za > 0.0 && c.z1 <= this.z0) {
                    var10000 = this.z0 - c.z1;
                    Objects.requireNonNull(this);
                    if ((max = var10000 - 0.0) < za) {
                        za = max;
                    }
                }

                if (za < 0.0 && c.z0 >= this.z1) {
                    var10000 = this.z1 - c.z0;
                    Objects.requireNonNull(this);
                    if ((max = var10000 + 0.0) > za) {
                        za = max;
                    }
                }

                return za;
            } else {
                return za;
            }
        } else {
            return za;
        }
    }

    public boolean intersects(AABB c) {
        if (!(c.x1 <= this.x0) && !(c.x0 >= this.x1)) {
            if (!(c.y1 <= this.y0) && !(c.y0 >= this.y1)) {
                return !(c.z1 <= this.z0) && !(c.z0 >= this.z1);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void move(double xa, double ya, double za) {
        this.x0 += xa;
        this.y0 += ya;
        this.z0 += za;
        this.x1 += xa;
        this.y1 += ya;
        this.z1 += za;
    }

    public AABB grow(double r) {
        return this.grow(r, r, r);
    }

    public boolean positionInBoundYZ(double y, double z) {
        return y >= this.y0 && y <= this.y1 && z >= this.z0 && z <= this.z1;
    }

    public boolean positionInBoundXZ(double x, double z) {
        return x >= this.x0 && x <= this.x1 && z >= this.z0 && z <= this.z1;
    }

    public boolean positionInBoundXY(double x, double y) {
        return x >= this.x0 && x <= this.x1 && y >= this.y0 && y <= this.y1;
    }

    public boolean isVectorInside(double x, double y, double z) {
        return x >= this.x0 && x <= this.x1 && y >= this.y0 && y <= this.y1 && z >= this.z0 && z <= this.z1;
    }

    public String toString() {
        return "%f/%f/%f - %f/%f/%f".formatted(new Object[]{this.x0, this.y0, this.z0, this.x1, this.y1, this.z1});
    }

    public double getMaxWidth() {
        return Math.max(Math.max(this.x1 - this.x0, this.y1 - this.y0), this.z1 - this.z0);
    }

    public double getMinWidth() {
        return Math.min(Math.min(this.x1 - this.x0, this.y1 - this.y0), this.z1 - this.z0);
    }

    public boolean inbound(AABB aabb) {
        return aabb.x0 >= this.x0
                && aabb.x1 <= this.x1
                && aabb.y0 >= this.y0
                && aabb.y1 <= this.y1
                && aabb.z0 >= this.z0
                && aabb.z1 <= this.z1;
    }
}
