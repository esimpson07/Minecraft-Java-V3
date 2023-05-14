import java.util.ArrayList;

public class Chunk {
    private ArrayList<Cube> cubes;
    private int x, y;
    private int size;
    private int height;
    private boolean inMap = false, alreadyChecked = false;
    
    public Chunk(int[][][] array, int size, int height, int startX, int startY) {
        this.size = size;
        this.x = startX;
        this.y = startY;
        cubes = new ArrayList<Cube>();
        for(int x = 0; x < size; x ++) {
            for(int y = 0; y < size; y ++) {
                for(int z = 0; z < height; z ++) {
                    if(array[startX * size + x][startY * size + y][z] != -1) {
                        cubes.add(new Cube(startX * size + x, startY * size + y, z, 1, 1, 1, array[startX * size + x][startY * size + y][z]));
                    }
                }
            }
        }
    }
    
    public void addCube(Cube cube) {
        cubes.add(cube);
    }
    
    public void removeCube(Cube cube) {
        cubes.remove(cube);
    }
    
    public boolean isAlreadyInMap() {
        return inMap;
    }
    
    public void addToMap() {
        inMap = true;
    }
    
    public void removeFromMap() {
        inMap = false;
    }
    
    public double getDistFromCenter(double x, double y) {
        return(Math.sqrt(Math.pow(x - (this.x + 0.5),2) + Math.pow(y - (this.y + 0.5), 2)));
    }
    
    public ArrayList<Cube> getCubeArray() {
        return cubes;
    }
    
    public int getX() {return x;}
    public int getY() {return y;}
    public int getSize() {return size;}
}