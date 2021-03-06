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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Roy Braam
 */
public abstract class CombineTileImageUrl extends CombineImageUrl{
    
    private Bbox serviceBbox = null;
    private Double[] resolutions = null;
    private Integer tileWidth = 256;
    private Integer tileHeight = 256;
    private Integer nextPosX =null;
    private Integer nextPosY = null;
    private boolean correctTiles = true;
    protected String extension;
    
    protected static double epsilon=0.0000000001;

    public CombineTileImageUrl(CombineTileImageUrl ctiu){
        super(ctiu);
    }
    public CombineTileImageUrl() {
        super();
    }
    
    
    public Integer getClosestZoomlevel(ImageBbox requestBbox){
        /* calc resolution */        
        Double res = null;    
        if (requestBbox != null) {
            res = requestBbox.getUnitsPixelX();
        }
        
        Integer zoomlevel = resolutions.length-1;
        if (resolutions!=null) {                        
            for (int i=0; i < resolutions.length; i++) {
                Double testRes = resolutions[i];
                
                if ( ((res - testRes) < 0.0000000001) && ((res-testRes) > -0.0000000001) ) {
                    zoomlevel = i;
                    break;
                } else if (res >= testRes) {
                    zoomlevel = i;
                    break;
                }
            }
           
        }
        return zoomlevel;
    }
    
    public List<CombineImageUrl> calculateNewUrl(ImageBbox imbbox){
        List<CombineImageUrl> tileImages = new ArrayList<CombineImageUrl>();
        
        //get closest res
        Integer zoomlevel = getClosestZoomlevel(imbbox);
        Double closestResolution = resolutions[zoomlevel];
        
        /*calc width in mapRes per tile*/
        Double tileWidthMapUnits = null;
        Double tileHeightMapUnits = null;        
        if (this.getTileWidth() != null && closestResolution != null) {
            tileWidthMapUnits = this.getTileWidth() * closestResolution;
        }
        if (this.getTileWidth() != null && closestResolution != null) {
            tileHeightMapUnits = this.getTileWidth() * closestResolution;
        }
        
        /* calc which tiles are needed */
        Integer minTileIndexX = null;
        Integer maxTileIndexX = null;
        Integer minTileIndexY = null;
        Integer maxTileIndexY = null;
        
        Bbox bbox = imbbox.getBbox();
        if (tileWidthMapUnits != null && tileWidthMapUnits > 0 
                && tileHeightMapUnits != null && tileHeightMapUnits > 0) {  
            
            minTileIndexX = getTileIndexX(bbox.getMinx(), closestResolution,false);
            maxTileIndexX = getTileIndexX(bbox.getMaxx(), closestResolution,true);
            minTileIndexY = getTileIndexY(bbox.getMiny(),closestResolution,false);
            maxTileIndexY = getTileIndexY(bbox.getMaxy(),closestResolution,true);            
        }
        
        
         /* 5) Opbouwen nieuwe tile url en per url ook x,y positie van tile bepalen 
         zodat drawImage deze op de juiste plek kan zetten */
        for (int ix = minTileIndexX; ix <= maxTileIndexX; ix++) {
            nextPosY=null;
            for (int iy = minTileIndexY; iy <= maxTileIndexY; iy++) {
                double[] tempBbox = new double[4];
                
                tempBbox[0] = serviceBbox.getMinx() + (ix * tileWidthMapUnits);
                tempBbox[1] = serviceBbox.getMiny() + (iy * tileHeightMapUnits);
                tempBbox[2] = tempBbox[0] + tileWidthMapUnits;                
                tempBbox[3] = tempBbox[1] + tileHeightMapUnits;
                
                Bbox tileBbox = new Bbox(tempBbox);                

                CombineStaticImageUrl tile = createTile(imbbox, tileBbox, ix, iy,zoomlevel);
                
                Integer tempNextY=tile.getY()+ (getTileDirectionY() * tile.getHeight());
                Integer tempNextX=null;
                if (iy==maxTileIndexY){
                    tempNextX = tile.getX() + (getTileDirectionX() * tile.getWidth());
                }
                if (correctTiles){
                    tile = correctTile(tile);
                }
                nextPosY = tempNextY;
                if (tempNextX!=null){
                    nextPosX = tempNextX;
                }
                //set next tile position                
                
                tileImages.add(tile);
            }            
        }
        return tileImages;
    }
    
    /**
     * create the tile
     * @param imageBbox the original imageBbox reqeust  
     * @param tileBbox the bbox of this tile
     * @param indexX 
     * @param indexY
     * @param zoomlevel
     * @return 
     */
    public CombineStaticImageUrl createTile(ImageBbox imageBbox, Bbox tileBbox, int indexX, int indexY,int zoomlevel) {
        
        CombineStaticImageUrl tile = new CombineStaticImageUrl();
        tile.setBbox(tileBbox);
        tile.setUrl(this.url);
        Bbox requestBbox = imageBbox.getBbox();
        Double msx = (requestBbox.getMaxx() - requestBbox.getMinx()) / imageBbox.getWidth();
        Double msy = (requestBbox.getMaxy() - requestBbox.getMiny()) / imageBbox.getHeight();
        
        Long posX = Math.round((tileBbox.getMinx() - requestBbox.getMinx()) / msx );
        Long posY = Math.round( (requestBbox.getMaxy() - tileBbox.getMaxy()) / msy );
        Long width = Math.round( (tileBbox.getMaxx() - tileBbox.getMinx()) / msx );
        Long height = Math.round( (tileBbox.getMaxy() - tileBbox.getMiny()) / msy );
        
        tile.setX(posX.intValue());
        tile.setY(posY.intValue());
        
        tile.setWidth(width.intValue());
        tile.setHeight(height.intValue());
        
        tile.setUrl(createUrl(imageBbox, tileBbox, indexX, indexY,zoomlevel));
                
        return tile;
    }
    
    /** Get the index number X of the tile on coordinate 'xCoord'.
     * @param xCoord The x coord.
     * @param zoomLevel the zoomLevel of the server.
	 * @see coremodel.service.tiling.factory.TileFactoryInterface#getTileIndexX
    */
    public Integer getTileIndexX(Double xCoord,Double res, boolean max){
        Double tileSpanX= res*getTileWidth();
        Double tileIndexX = Math.floor((xCoord - serviceBbox.getMinx()) / (tileSpanX+epsilon));
        if (tileIndexX < 0) {
            tileIndexX=0.0;
        }
        Double maxBboxX = Math.floor(( serviceBbox.getMaxx() - serviceBbox.getMinx() ) / (tileSpanX+epsilon));
        if (tileIndexX > maxBboxX) {
            tileIndexX = maxBboxX;
        }
        return tileIndexX.intValue();
    }
    /** Get the index number Y of the tile on coordinate 'yCoord' on zoomlevel 'zoomLevel'.
     * @param yCoord The y coord.
     * @param zoomLevel the zoomLevel of the server.
    */
    public Integer getTileIndexY(double yCoord,Double res, boolean max){
       Double tileSpanY= res*getTileHeight();
        Double tileIndexY = Math.floor((yCoord - serviceBbox.getMiny()) / (tileSpanY+epsilon));
        if (tileIndexY < 0) {
            tileIndexY=0.0;
        }
        Double maxBboxY = Math.floor(( serviceBbox.getMaxy() - serviceBbox.getMiny() ) / (tileSpanY+epsilon));
        if (tileIndexY > maxBboxY) {
            tileIndexY = maxBboxY;
        }
        return tileIndexY.intValue();
    }

    private CombineStaticImageUrl correctTile(CombineStaticImageUrl tile) {
        if (this.nextPosX!=null && !this.nextPosX.equals(tile.getX())){
            Integer diff = this.nextPosX - tile.getX();
            if (getTileDirectionX() < 0){
                /*when tiles are build from right to left, correct the width of
                 * the tile. The right of this image must connect with the previous
                 * y image left.
                 */
                tile.setWidth(tile.getWidth()+diff);
            }else{
                tile.setX(tile.getX()+diff);
                tile.setWidth(tile.getWidth()-diff);
            }
        }
        if (this.nextPosY!=null && !this.nextPosY.equals(tile.getY())){
            Integer diff = this.nextPosY - tile.getY();
            if (getTileDirectionY() < 0){
                /*when tiles are build from bottom to top, correct the height of
                 * the tile. The bottom of this image must connect with the previous
                 * y image top.
                 */
                tile.setHeight(tile.getHeight()+diff);
            }else{
                tile.setY(tile.getY()+diff);
                tile.setHeight(tile.getHeight()-diff);
            }
        }
        return tile;
    }
    //<editor-fold defaultstate="collapsed" desc="Getters setters">
    /**
     * @return the bbox
     */
    public Bbox getServiceBbox() {
        return serviceBbox;
    }
    
    /**
     * @param bbox the bbox to set
     */
    public void setServiceBbox(Bbox bbox) {
        this.serviceBbox = bbox;
    }
    
    /**
     * @return the resolutions
     */
    public Double[] getResolutions() {
        return resolutions;
    }
    
    /**
     * @param resolutions the resolutions to set
     */
    public void setResolutions(Double[] resolutions) {
        this.resolutions = resolutions;
    }
    
    /**
     * @return the tileWidth
     */
    public Integer getTileWidth() {
        return tileWidth;
    }
    
    /**
     * @param tileWidth the tileWidth to set
     */
    public void setTileWidth(Integer tileWidth) {
        this.tileWidth = tileWidth;
    }
    
    /**
     * @return the tileHeight
     */
    public Integer getTileHeight() {
        return tileHeight;
    }
    
    /**
     * @param tileHeight the tileHeight to set
     */
    public void setTileHeight(Integer tileHeight) {
        this.tileHeight = tileHeight;
    }
    
    public boolean isCorrectTiles() {
        return correctTiles;
    }

    public void setCorrectTiles(boolean correctTiles) {
        this.correctTiles = correctTiles;
    }
    
    
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
    
    //</editor-fold>

    protected abstract String createUrl(ImageBbox imageBbox, Bbox tileBbox, int indexX, int indexY, int zoomlevel);
    
    /**
     * Return -1 or 1 to indicate the direction of the tile in the image.
     * 0/0 tile is on 'x','y'
     * 1/1 tile is on 'x' + (<getTileDirectionX> * tileWidth), 'y' + (<getTileDirectionY> * tileHeight) 
     */
    protected abstract int getTileDirectionX();
    protected abstract int getTileDirectionY();

    
}
