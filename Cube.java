import java.awt.Color;

public class Cube {
    private double x, y, z, width, length, height, rotation = Math.PI * 0.75;
    private double[] rotAdd = new double[4];
    private double[] angle = new double[4];
    private double x1, x2, x3, x4, y1, y2, y3, y4;
    private DPolygon[] polys = new DPolygon[6];
    private Color[] c;
    private boolean[] polysToDraw;
    private int id;
    private int type;
    private boolean normal;
    
    public Cube(double x, double y, double z, double width, double length, double height, int type) {
        this.id = (int)(Math.random() * 2147483647);
        this.c = getColor(type);
        this.polysToDraw = new boolean[]{true,true,true,true,true,true};
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.type = type;
        this.width = width;
        this.length = length;
        this.height = height;
        
        setValuesForType(type);
        setRotAdd();
        updatePoly();
    }
    
    static Color[] getColor(int type) {
        if(type == GameController.stone) {
            return(new Color[]{GameController.lightGray,GameController.lightGray,GameController.lightGray,GameController.lightGray,GameController.lightGray,GameController.lightGray});
        } else if(type == GameController.cobblestone) {
            return(new Color[]{GameController.darkGray,GameController.darkGray,GameController.darkGray,GameController.darkGray,GameController.darkGray,GameController.darkGray});
        } else if(type == GameController.dirt) {
            return(new Color[]{GameController.darkBrown,GameController.darkBrown,GameController.darkBrown,GameController.darkBrown,GameController.darkBrown,GameController.darkBrown});
        } else if(type == GameController.grass) {
            return(new Color[]{GameController.darkBrown,GameController.lightGreen,GameController.darkBrown,GameController.darkBrown,GameController.darkBrown,GameController.darkBrown});
        } else if(type == GameController.planks) {
            return(new Color[]{GameController.lightBrown,GameController.lightBrown,GameController.lightBrown,GameController.lightBrown,GameController.lightBrown,GameController.lightBrown});
        } else if(type == GameController.logs) {
            return(new Color[]{GameController.lightBrown,GameController.lightBrown,GameController.darkBrown,GameController.darkBrown,GameController.darkBrown,GameController.darkBrown});
        } else if(type == GameController.leaves) {
            return(new Color[]{GameController.darkGreen,GameController.darkGreen,GameController.darkGreen,GameController.darkGreen,GameController.darkGreen,GameController.darkGreen});
        } else if(type == GameController.sand) {
            return(new Color[]{GameController.beige,GameController.beige,GameController.beige,GameController.beige,GameController.beige,GameController.beige});
        } else if(type == GameController.glass) {
            return(new Color[]{GameController.translucent,GameController.translucent,GameController.translucent,GameController.translucent,GameController.translucent,GameController.translucent});
        } else if(type == GameController.water) {
            return(new Color[]{GameController.waterBlue,GameController.waterBlue,GameController.waterBlue,GameController.waterBlue,GameController.waterBlue,GameController.waterBlue});
        } else if(type == GameController.bedrock) {
            return(new Color[]{GameController.black,GameController.black,GameController.black,GameController.black,GameController.black,GameController.black});
        } else {
            return(new Color[]{});
        }
    }
    
    public void setValuesForType(int type) {
        if(type == GameController.glass || type == GameController.water) {
            normal = false;
        } else {
            normal = true;
        }
    }
    
    public double[] getAttributes() {
        return new double[]{x,y,z,width,length,height};
    }
    
    public double[] getCoords() {
        return new double[]{x,y,z};
    }
    
    public double[] getAdjacentCube(int face) {
        if(face == 0) {
            return new double[]{x, y, z - height};
        } else if(face == 1) {
            return new double[]{x, y, z + height};
        } else if(face == 2) {
            return new double[]{x, y + length, z};
        } else if(face == 3) {
            return new double[]{x - width, y, z};
        } else if(face == 4) {
            return new double[]{x, y - length, z};
        } else if(face == 5) {
            return new double[]{x + width, y, z};
        } else {
            return new double[]{0,0,0};
        }
    }
    
    public int getID() {
        return id;
    }
    
    public double getDist(double x, double y, double z) {
        return Math.sqrt(Math.pow(this.x - x,2) + Math.pow(this.y - y,2) + Math.pow(this.z - z,2));
    }
    
    public void softAdjacencyCheck() {
        int chunk = GameController.getChunkNumberIn((int)x,(int)y);
        int sideLength = GameController.worldSize / GameController.chunkSize;
        int[] adjacentChunks = new int[5]; //left (chunk - 1) right (chunk + 1) above (chunk + sideLength) below (chunk - sideLength)
        if(chunk % sideLength != 0 && (int)x % GameController.chunkSize == 0) {
            adjacentChunks[0] = chunk - 1;
        } else {
            adjacentChunks[0] = -1;
        }
        if(chunk % sideLength != sideLength - 1 && (int)x % GameController.chunkSize == GameController.chunkSize - 1) {
            adjacentChunks[1] = chunk + 1;
        } else {
            adjacentChunks[1] = -1;
        }
        if(chunk >= sideLength && (int)y % GameController.chunkSize == 0) {
            adjacentChunks[2] = chunk - sideLength;
        } else {
            adjacentChunks[2] = -1;
        }
        if(chunk < Math.pow(sideLength,2) - sideLength && (int)y % GameController.chunkSize == GameController.chunkSize - 1) {
            adjacentChunks[3] = chunk + sideLength;
        } else {
            adjacentChunks[3] = -1;
        }
        adjacentChunks[4] = chunk;
        
        for(int i = 0; i < adjacentChunks.length; i ++) {
            for(int f = 0; f < 6; f ++) {
                if(adjacentChunks[i] != -1) {
                    for(int j = 0; j < GameController.chunks[adjacentChunks[i]].getCubeArray().size(); j ++) {
                        if(((GameController.chunks[adjacentChunks[i]].getCubeArray().get(j).getCoords()[0] == getAdjacentCube(f)[0] && GameController.chunks[adjacentChunks[i]].getCubeArray().
                            get(j).getCoords()[1] == getAdjacentCube(f)[1] && GameController.chunks[adjacentChunks[i]].getCubeArray().get(j).getCoords()[2] == getAdjacentCube(f)[2] && 
                            ((GameController.chunks[adjacentChunks[i]].getCubeArray().get(j).isWater() == isWater()) || isWater()) && GameController.chunks[adjacentChunks[i]].getCubeArray().
                            get(j).isGlass() == isGlass())) || !Calculator.withinRange(getAdjacentCube(f)[0],0,GameController.worldSize - 1) || !Calculator.withinRange(getAdjacentCube(f)[1],0,
                            GameController.worldSize - 1) || !Calculator.withinRange(getAdjacentCube(f)[2],0,GameController.worldHeight - 1)){
                            polysToDraw[f] = false;
                            updatePoly();
                        }
                    }
                }
            }
        }
    }
    
    public void hardAdjacencyCheck() {
        for(int i = 0; i < GameController.chunks.length; i ++) {
            if(GameController.chunks[i] != null) {
                for(int j = 0; j < GameController.chunks[i].getCubeArray().size(); j ++) {
                    for(int f = 0; f < 6; f ++) {
                        if(GameController.chunks[i].getCubeArray().get(j).getCoords()[0] == getAdjacentCube(f)[0] && GameController.chunks[i].getCubeArray().get(j).getCoords()[1] == 
                            getAdjacentCube(f)[1] && GameController.chunks[i].getCubeArray().get(j).getCoords()[2] == getAdjacentCube(f)[2] && 
                            ((GameController.chunks[i].getCubeArray().get(j).isWater() == isWater()) || isWater()) && GameController.chunks[i].getCubeArray().
                            get(j).isGlass() == isGlass()) {
                            polysToDraw[f] = false;
                            updatePoly();
                            GameController.chunks[i].getCubeArray().get(j).softAdjacencyCheck();
                        }
                    }
                }
            }
        }
    }
    
    static void hardAdjacencyCheck(double[][] adjacentCubes) {
        for(int i = 0; i < GameController.chunks.length; i ++) {
            if(GameController.chunks[i] != null) {
                for(int j = 0; j < GameController.chunks[i].getCubeArray().size(); j ++) {
                    for(int f = 0; f < 6; f ++) {
                        if(GameController.chunks[i].getCubeArray().get(j).getCoords()[0] == adjacentCubes[f][0] && GameController.chunks[i].getCubeArray().get(j).getCoords()[1] == 
                            adjacentCubes[f][1] && GameController.chunks[i].getCubeArray().get(j).getCoords()[2] == adjacentCubes[f][2]) {
                            GameController.chunks[i].getCubeArray().get(j).softAdjacencyCheck();
                        }
                    }
                }
            }
        }
    }
    
    public void changeAdjacentCubePoly(int face, boolean state) {
        for(int i = 0; i < GameController.chunks.length; i ++) {
            if(GameController.chunks[i] != null) {
                for(int j = 0; j < GameController.chunks[i].getCubeArray().size(); j ++) {
                    for(int f = 0; f < 6; f ++) {
                        if(GameController.chunks[i].getCubeArray().get(j).getCoords()[0] == getAdjacentCube(f)[0] && GameController.chunks[i].getCubeArray().get(j).getCoords()[1] == 
                            getAdjacentCube(f)[1] && GameController.chunks[i].getCubeArray().get(j).getCoords()[2] == getAdjacentCube(f)[2] && !GameController.chunks[i].getCubeArray().get(j).isWater()) {
                            GameController.chunks[i].getCubeArray().get(j).changeAdjacentPoly(face,state);
                            GameController.chunks[i].getCubeArray().get(j).updatePoly();
                        }
                    }
                }
            }
        }
    }
    
    public void setRotAdd() {
        
        double xDif = - width/2 + 0.00001;
        double yDif = - length/2 + 0.00001;
        
        angle[0] = Math.atan(yDif/xDif);
        
        if(xDif<0) {
            angle[0] += Math.PI;
        }
        
        xDif = width/2 + 0.00001;
        yDif = - length/2 + 0.00001;
        
        angle[1] = Math.atan(yDif/xDif);
        
        if(xDif<0) {
            angle[1] += Math.PI;
        }
        
        xDif = width/2 + 0.00001;
        yDif = length/2 + 0.00001;
        
        angle[2] = Math.atan(yDif/xDif);
        
        if(xDif<0) {
            angle[2] += Math.PI;
        }
        
        xDif = - width/2 + 0.00001;
        yDif = length/2 + 0.00001;
        
        angle[3] = Math.atan(yDif/xDif);
        
        if(xDif<0) {
            angle[3] += Math.PI;    
        }
        
        rotAdd[0] = angle[0] + 0.25 * Math.PI;
        rotAdd[1] =    angle[1] + 0.25 * Math.PI;
        rotAdd[2] = angle[2] + 0.25 * Math.PI;
        rotAdd[3] = angle[3] + 0.25 * Math.PI;
    }
    
    public void updateDirection(double toX, double toY) {
        double xDif = toX - (x + width/2) + 0.00001;
        double yDif = toY - (y + length/2) + 0.00001;
        
        double angleT = Math.atan(yDif/xDif) + 0.75 * Math.PI;

        if(xDif<0) {
            angleT += Math.PI;
        }
        
        rotation = angleT;
        updatePoly();        
    }

    public void updatePoly() {
        for(int i = 0; i < 6; i ++) {
            if(polys[i] != null) {
                GameController.DPolygons.remove(polys[i]);
            }
        }
        
        double radius = Math.sqrt(width*width + length*length);
        
        x1 = x+width*0.5+radius*0.5*Math.cos(rotation + rotAdd[0]);
        x2 = x+width*0.5+radius*0.5*Math.cos(rotation + rotAdd[1]);
        x3 = x+width*0.5+radius*0.5*Math.cos(rotation + rotAdd[2]);
        x4 = x+width*0.5+radius*0.5*Math.cos(rotation + rotAdd[3]);
           
        y1 = y+length*0.5+radius*0.5*Math.sin(rotation + rotAdd[0]);
        y2 = y+length*0.5+radius*0.5*Math.sin(rotation + rotAdd[1]);
        y3 = y+length*0.5+radius*0.5*Math.sin(rotation + rotAdd[2]);
        y4 = y+length*0.5+radius*0.5*Math.sin(rotation + rotAdd[3]);
   
        if(polysToDraw[0] == true && polys[0] != null) {
            polys[0].setX(new double[]{x1, x2, x3, x4});
            polys[0].setY(new double[]{y1, y2, y3, y4});;
            polys[0].setZ(new double[]{z, z, z, z});
        } else  if(polysToDraw[0] == true && polys[0] == null){
            polys[0] = new DPolygon(new double[]{x1, x2, x3, x4}, new double[]{y1, y2, y3, y4}, new double[]{z, z, z, z}, c[0], 0, id);
        } else {
            polys[0] = null;
        }
        if(polysToDraw[1] == true && polys[1] != null) {
            polys[1].setX(new double[]{x4, x3, x2, x1});
            polys[1].setY(new double[]{y4, y3, y2, y1});
            polys[1].setZ(new double[]{z+height, z+height, z+height, z+height});
        } else if(polysToDraw[1] == true && polys[1] == null){
            polys[1] = new DPolygon(new double[]{x4, x3, x2, x1}, new double[]{y4, y3, y2, y1}, new double[]{z+height, z+height, z+height, z+height}, c[1], 1, id);
        } else {    
            polys[1] = null;
        } 
        if(polysToDraw[2] == true && polys[2] != null) {
            polys[2].setX(new double[]{x1, x1, x2, x2});
            polys[2].setY(new double[]{y1, y1, y2, y2});
            polys[2].setZ(new double[]{z, z+height, z+height, z});
        } else if(polysToDraw[2] == true && polys[2] == null){
            polys[2] = new DPolygon(new double[]{x1, x1, x2, x2}, new double[]{y1, y1, y2, y2}, new double[]{z, z+height, z+height, z}, c[2], 2, id);
        } else {
            polys[2] = null;
        }
        if(polysToDraw[3] == true && polys[3] != null) {
            polys[3].setX(new double[]{x2, x2, x3, x3});
            polys[3].setY(new double[]{y2, y2, y3, y3});
            polys[3].setZ(new double[]{z, z+height, z+height, z});
        } else if(polysToDraw[3] == true && polys[3] == null){
            polys[3] = new DPolygon(new double[]{x2, x2, x3, x3}, new double[]{y2, y2, y3, y3},  new double[]{z, z+height, z+height, z}, c[3], 3, id);
        } else {
            polys[3] = null;
        }
        if(polysToDraw[4] == true && polys[4] != null) {
            polys[4].setX(new double[]{x3, x3, x4, x4});
            polys[4].setY(new double[]{y3, y3, y4, y4});
            polys[4].setZ(new double[]{z, z+height, z+height, z});
        } else if(polysToDraw[4] == true && polys[4] == null){
            polys[4] = new DPolygon(new double[]{x3, x3, x4, x4}, new double[]{y3, y3, y4, y4},  new double[]{z, z+height, z+height, z}, c[4], 4, id);
        } else {
            polys[4] = null;
        }
        if(polysToDraw[5] == true && polys[5] != null) {
            polys[5].setX(new double[]{x4, x4, x1, x1});
            polys[5].setY(new double[]{y4, y4, y1, y1});
            polys[5].setZ(new double[]{z, z+height, z+height, z});
        } else if(polysToDraw[5] == true && polys[5] == null){
            polys[5] = new DPolygon(new double[]{x4, x4, x1, x1}, new double[]{y4, y4, y1, y1},  new double[]{z, z+height, z+height, z}, c[5], 5, id);
        } else {
            polys[5] = null;
        }
        
        for(int i = 0; i < 6; i++)
        {
            if(polys[i] != null) {
                GameController.DPolygons.add(polys[i]);
            }
        }
    }
    
    public void changeAdjacentPoly(int face, boolean state) {
        polysToDraw[getAdjacentPoly(face)] = state;
    }
    
    public int getAdjacentPoly(int face) {
        if(face == 0) {
            return 1;
        } else if(face == 1) {
            return 0;
        } else if(face == 2) {
            return 3;
        } else if(face == 3) {
            return 2;
        } else if(face == 4) {
            return 5;
        } else if(face == 5) {
            return 4;
        } else {
            return -1;
        }
    }
    
    public boolean isBedrock() {
        return type == GameController.bedrock;
    }
        
    public boolean isWater() {
        return type == GameController.water;
    }
    
    public boolean isGlass() {
        return type == GameController.glass;
    }

    public void removeCubeInChunk() {
        for(int i = 0; i < 6; i ++) {
            GameController.DPolygons.remove(polys[i]);
        }
    }
    
    void removeCube()
    {
        for(int i = 0; i < 6; i ++) {
            changeAdjacentCubePoly(i,true);
            GameController.DPolygons.remove(polys[i]);
        }
        for(int i = 0; i < GameController.chunks.length; i ++) {
            GameController.chunks[i].removeCube(this);
        }
    }
}
