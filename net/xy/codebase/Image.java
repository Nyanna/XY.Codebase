/**
 * This file is part of XY.Codebase, Copyright 2011 (C) Xyan Kruse, Xyan@gmx.net, Xyan.kilu.de
 * 
 * XY.Codebase is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * XY.Codebase is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with XY.Codebase. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.xy.codebase;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * class for manipulating images mainly as servlet proccesor used
 * 
 * @author xyan
 * 
 */
public class Image {
    /**
     * utility hint mapping
     * 
     * @author xyan
     * 
     */
    private static class Hints {
        public static final RenderingHints.Key[] KEYS = new RenderingHints.Key[] {
                RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.KEY_ANTIALIASING,
                RenderingHints.KEY_COLOR_RENDERING, RenderingHints.KEY_DITHERING, RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.KEY_INTERPOLATION, RenderingHints.KEY_RENDERING, RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.KEY_TEXT_ANTIALIASING };
    }

    /**
     * simplified quality handling
     * 
     * @author xyan
     * 
     */
    public enum Quality {
        LOW(0, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED, RenderingHints.VALUE_ANTIALIAS_OFF,
                RenderingHints.VALUE_COLOR_RENDER_SPEED, RenderingHints.VALUE_DITHER_DISABLE,
                RenderingHints.VALUE_FRACTIONALMETRICS_OFF, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
                RenderingHints.VALUE_RENDER_SPEED, RenderingHints.VALUE_STROKE_DEFAULT,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF), //
        MEDIUM(2, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED, RenderingHints.VALUE_ANTIALIAS_ON,
                RenderingHints.VALUE_COLOR_RENDER_SPEED, RenderingHints.VALUE_DITHER_ENABLE,
                RenderingHints.VALUE_FRACTIONALMETRICS_OFF, RenderingHints.VALUE_INTERPOLATION_BILINEAR,
                RenderingHints.VALUE_RENDER_SPEED, RenderingHints.VALUE_STROKE_DEFAULT,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF), //
        HIGH(-1, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY, RenderingHints.VALUE_ANTIALIAS_ON,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY, RenderingHints.VALUE_DITHER_ENABLE,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON, RenderingHints.VALUE_INTERPOLATION_BICUBIC,
                RenderingHints.VALUE_RENDER_QUALITY, RenderingHints.VALUE_STROKE_NORMALIZE,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        /**
         * rendering hints
         */
        public final Map<RenderingHints.Key, Object> hints;

        /**
         * maximum amount of stepped proccessing allowed, -1 automatic choose best
         */
        public final int stepping;

        private Quality(final int stepping, final Object... hints) {
            final Map<RenderingHints.Key, Object> mapping = new HashMap<RenderingHints.Key, Object>();
            for (int i = 0; i < hints.length; i++) {
                mapping.put(Hints.KEYS[i], hints[i]);
            }
            this.hints = Collections.unmodifiableMap(mapping);
            this.stepping = stepping;
        }
    }

    /**
     * clipping indicator
     * 
     * @author xyan
     * 
     */
    public enum Clip {
        WIDTH, HEIGHT, NONE;
    }

    /**
     * delegate using qualities
     * 
     * @param input
     * @param size
     * @param quality
     * @return
     */
    public static BufferedImage resize(final BufferedImage input, final Dimension size, final Quality quality) {
        return resize(input, size, quality.hints, quality.stepping);
    }

    /**
     * resizes an image to new dimensions don't alter the original
     * 
     * @param input
     * @param size
     * @param hints
     * @param stepping
     * @return
     */
    public static BufferedImage resize(final BufferedImage input, final Dimension size,
            final Map<RenderingHints.Key, Object> hints, final int stepping) {
        final int clip = calculate(input.getWidth(), input.getHeight(), size);
        final Hashtable<Object, Object> properties = new Hashtable<Object, Object>(input.getPropertyNames().length);
        for (final String name : input.getPropertyNames()) {
            properties.put(name, input.getProperty(name));
        }
        final BufferedImage ret = new BufferedImage(input.getColorModel(), input.getColorModel()
                .createCompatibleWritableRaster(size.width, size.height), input.isAlphaPremultiplied(), properties);
        final Graphics g = ret.getGraphics();
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).addRenderingHints(hints);
        }
        if (clip != 0) {
            final double part = clip / 2;
            final int align = (int) Math.floor(part);
            final int cut = (int) Math.ceil(part);
            if (clip > 0) { // crop height
                g.drawImage(input, 0, 0, size.width, size.height, 0, align, input.getWidth(), input.getHeight() - cut,
                        null);
            } else if (clip < 0) { // crop width
                g.drawImage(input, 0, 0, size.width, size.height, align, 0, input.getWidth() + cut, input.getHeight(),
                        null);
            }
        } else {
            g.drawImage(input, 0, 0, size.width, size.height, 0, 0, input.getWidth(), input.getHeight(), null);
        }
        return ret;
    }

    /**
     * recalculates dimension if necessary
     * 
     * @param width
     * @param height
     * @param size
     * @return true if the immage must be clipped to retain aspect
     */
    public static int calculate(final int width, final int height, final Dimension size) {
        final double aspect = width / height;
        if (size.width <= 0) {
            if (size.height <= 0) {
                size.width = width;
                size.height = height;
            } else {
                size.width = (int) Math.ceil(size.height * aspect);
            }
        } else {
            if (size.height <= 0) {
                size.height = (int) Math.ceil(size.width / aspect);
            } else {
                final double newAspect = size.width / size.height;
                if (newAspect < aspect) { // crop width return -
                    return (int) Math.ceil(height * newAspect - width);
                } else if (newAspect > aspect) { // crop height return +
                    return (int) Math.ceil(height - width / newAspect);
                }
                return 0;
            }
        }
        return 0;
    }

    /**
     * calculates how the image reactangle changes if the image is rotated
     * 
     * @param size
     * @param degrees
     */
    public static void rotate(final Dimension size, double degrees) {
        if (degrees == 0 || degrees == 180) {
            return;
        } else if (degrees == 90 || degrees == 270) {
            final int width = size.width;
            size.width = size.height;
            size.height = width;
        } else {
            // copied from php codebase
            final double midh = size.height / 2; // a
            final double midv = size.width / 2; // b
            while (degrees > 90) {
                degrees -= 90;
                final int width = size.width;
                size.width = size.height;
                size.height = width;
            }
            final double len = Math.sqrt(Math.pow(midv, 2) + Math.pow(midh, 2) - 2 * midv * midh
                    * Math.cos(Math.toRadians(90))); // c
            final double rad = Math.toDegrees(Math.acos(Math.pow(midv, 2) - Math.pow(midh, 2) - Math.pow(len, 2))
                    / (-2 * midh * len));
            size.height = (int) Math.round(len * Math.sin(Math.toRadians(90 - (rad - degrees)))
                    / Math.sin(Math.toRadians(90)) * 2);
            size.width = (int) Math.round(len * Math.sin(Math.toRadians(rad + degrees)) / Math.sin(Math.toRadians(90))
                    * 2);
        }
    }

    /**
     * add an watermark with various options
     * 
     * @param image
     */
    public static void watermark(final BufferedImage image) {
        // TODO
    }

    /**
     * adds clipped hints and borders
     * 
     * @param image
     */
    public static void clipedhint(final BufferedImage image) {
        // TODO
    }
}
