public class Plane {
    private Vector v1, v2, ret;
    private double[] point = new double[3];
    
    // constructs a new plane using a DPolygon object as a parameter
    
    public Plane(DPolygon d) {
        point[0] = d.getX()[0]; 
        point[1] = d.getY()[0]; 
        point[2] = d.getZ()[0]; 
        
        v1 = new Vector(d.getX()[1] - d.getX()[0], 
                        d.getY()[1] - d.getY()[0], 
                        d.getZ()[1] - d.getZ()[0]);

        v2 = new Vector(d.getX()[2] - d.getX()[0], 
                        d.getY()[2] - d.getY()[0], 
                        d.getZ()[2] - d.getZ()[0]);
        
        ret = v1.crossProduct(v2);
    }
    
    // constructs a plane using two vectors and a 3d point
    
    public Plane(Vector ve1, Vector ve2, double[] coord) {
        point = coord;
        
        v1 = ve1;
        
        v2 = ve2;
        
        ret = v1.crossProduct(v2);
    }
    
    public Vector getRetVector() { //returns cross product of 2 vectors that form the plane
        return ret;
    }
    
    public Vector getV1() { //returns the first vector
        return v1;
    }
    
    public Vector getV2() { //returns the second vector
        return v2;
    }
    
    public double[] getP() { //returns the X Y Z coordinate of point 0 from the DPolygon
        return point;
    }
}