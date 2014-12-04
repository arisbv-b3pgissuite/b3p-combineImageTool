/*
 * Copyright (C) 2012 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.imagetool;

import java.awt.image.BufferedImage;

/**
 *
 * @author Roy Braam
 */
public class ReferencedImage {
    private BufferedImage image;
    private ImageCollector ic;
    private Integer x=0;
    private Integer y=0;
    private Integer width=null;
    private Integer height=null;

    private Float alpha;

    ReferencedImage(BufferedImage bufferedImage) {
        this.image=bufferedImage;
    }

    ReferencedImage(ImageCollector ic) {
        this.ic = ic;
    }

    ReferencedImage() {}

    public BufferedImage getImage() throws Exception {
        if(this.image != null) {
            return image;
        } else if(ic != null) {
            image = ic.getBufferedImage();
            return image;
        } else {
            return null;
        }
    }

    public void dispose() {
        if(this.image != null) {
            this.image = null;
        }
        if(this.ic != null) {
            this.ic.dispose();
        }
    }

    //<editor-fold defaultstate="collapsed" desc="comment">

    public Integer getX() {
        return x;
    }

    public void setX(Integer offsetX) {
        this.x = offsetX;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer offsetY) {
        this.y = offsetY;
    }

    public Float getAlpha() {
        return alpha;
    }

    public void setAlpha(Float alpha) {
        this.alpha = alpha;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    //</editor-fold>
}
