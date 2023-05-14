public class Calculator {
    static double[] calcFocusPos = new double[2];
    static double t = 0;
    
    static Vector w1, w2, viewVector, rotationVector, directionVector, planeVector1, planeVector2;
    static Plane p;
    
    public static double[] calculatePositionP(double[] viewFrom, double[] viewTo, double x, double y, double z)
    {        
        double[] projP = getProj(viewFrom, viewTo, x, y, z, p);
        double[] drawP = getDrawP(projP[0], projP[1], projP[2]);
        return drawP;
    }

    public static double[] getProj(double[] viewFrom, double[] viewTo, double x, double y, double z, Plane P)
    {
        Vector viewToPoint = new Vector(x - viewFrom[0], y - viewFrom[1], z - viewFrom[2]);

               t = (P.getRetVector().getX() *P.getP()[0] + P.getRetVector().getY() * P.getP()[1] +  P.getRetVector().getZ() * P.getP()[2]
                 - (P.getRetVector().getX() *viewFrom[0] + P.getRetVector().getY() *viewFrom[1] + P.getRetVector().getZ() * viewFrom[2]))
                 / (P.getRetVector().getX() *viewToPoint.getX() + P.getRetVector().getY() * viewToPoint.getY() + P.getRetVector().getZ() * viewToPoint.getZ());

        x = viewFrom[0] + viewToPoint.getX() * t;
        y = viewFrom[1] + viewToPoint.getY() * t;
        z = viewFrom[2] + viewToPoint.getZ() * t;
        
        return new double[] {x, y, z};
    }
    
    public static double[] getDrawP(double x, double y, double z)
    {
        double DrawX = w2.getX() * x + w2.getY() * y + w2.getZ() * z;
        double DrawY = w1.getX() * x + w1.getY() * y + w1.getZ() * z;        
        return new double[]{DrawX, DrawY};
    }
    
    public static double clamp(double val, double min, double max) {
        if(val > max) {
            return(max);
        } else if(val < min) {
            return(min);
        } else {
            return(val);
        }
    }
    
    public static int clamp(int val, int min, int max) {
        if(val > max) {
            return(max);
        } else if(val < min) {
            return(min);
        } else {
            return(val);
        }
    }
    
    public static double roll(double val, double min, double max) {
        if(val > max) {
            return(min);
        } else if(val < min) {
            return(max);
        } else {
            return(val);
        }
    }
    
    public static int roll(int val, int min, int max) {
        if(val > max) {
            return(min);
        } else if(val < min) {
            return(max);
        } else {
            return(val);
        }
    }
    
    public static boolean withinRange(double val, double min, double max) {
        return val >= min && val <= max;
    }
    
    public static boolean withinRange(int val, int min, int max) {
        return val >= min && val <= max;
    }
    
    public static double roundTo(double val, double places) {
        double scale = Math.pow(10,places);
        return Math.round(val * scale) / scale;
    }
    
    public static Vector getRotationVector(double[] viewFrom, double[] viewTo)
    {
        double dx = Math.abs(viewFrom[0]-viewTo[0]);
        double dy = Math.abs(viewFrom[1]-viewTo[1]);
        
        double xRot, yRot;
        
        xRot=dy/(dx+dy);        
        yRot=dx/(dx+dy);

        if(viewFrom[1] > viewTo[1]) {
            xRot = -xRot;
        }
        
        if(viewFrom[0] < viewTo[0]) {
            yRot = -yRot;
        }
        
        Vector v = new Vector(xRot, yRot, 0);
        return v;
    }
    
    public static void setPredeterminedInfo()
    {
        viewVector = new Vector(GameController.viewTo[0] - GameController.viewFrom[0], GameController.viewTo[1] - GameController.viewFrom[1], GameController.viewTo[2] - GameController.viewFrom[2]);            
        directionVector = new Vector(1, 1, 1);                
        planeVector1 = viewVector.crossProduct(directionVector);
        planeVector2 = viewVector.crossProduct(planeVector1);
        p = new Plane(planeVector1, planeVector2, GameController.viewTo);

        rotationVector = getRotationVector(GameController.viewFrom, GameController.viewTo);
        w1 = viewVector.crossProduct(rotationVector);
        w2 = viewVector.crossProduct(w1);

        calcFocusPos = calculatePositionP(GameController.viewFrom, GameController.viewTo, GameController.viewTo[0], GameController.viewTo[1], GameController.viewTo[2]);
        calcFocusPos[0] = GameController.zoom * calcFocusPos[0];
        calcFocusPos[1] = GameController.zoom * calcFocusPos[1];
    }
}