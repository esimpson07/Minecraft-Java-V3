import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

public class DPolygon {
    private Polygon poly;
    private Color c;

    private double[] calcPos, newX, newY;
    private double[] x, y, z;
    private double lighting = 1;
    private double avgDist;

    private int side, id;

    private boolean draw = true, outlines = true;;

    public DPolygon(double[] x, double[] y,  double[] z, Color c, int side, int id) { //constructs a drawable polygon
        this.x = x;
        this.y = y;
        this.z = z;        
        this.c = c;
        this.id = id;
        this.side = side;
        createPolygonObject(new double[x.length], new double[x.length]);
    }

    public void createPolygonObject(double[] x, double[] y) { //creates a polygon object and gives it points as given in the constructor
        poly = new Polygon();
        for(int i = 0; i<x.length; i++) {
            poly.addPoint((int)x[i], (int)y[i]);
        }
    }

    public void updatePolygon() { //does the math to calculate the x and y positions on the screen for the polygon
        newX = new double[x.length];
        newY = new double[x.length];
        draw = true;
        for(int i=0; i<x.length; i++)
        {
            calcPos = Calculator.calculatePositionP(GameController.viewFrom, GameController.viewTo, x[i], y[i], z[i]);
            newX[i] = (GameRunner.screenSize.getWidth()/2 - Calculator.calcFocusPos[0]) + calcPos[0] * GameController.zoom;
            newY[i] = (GameRunner.screenSize.getHeight()/2 - Calculator.calcFocusPos[1]) + calcPos[1] * GameController.zoom;            
            if(Calculator.t < 0) {
                draw = false;
            }
        }

        calcLighting();

        setDraw(draw);
        updatePolygon(newX, newY);
        avgDist = getDist();
    }

    public void calcLighting() { //Calculates the lighting on this plane
        Plane lightingPlane = new Plane(this); 
        double angle = Math.acos(((lightingPlane.getRetVector().getX() * GameController.lightDir[0]) + 
                    (lightingPlane.getRetVector().getY() * GameController.lightDir[1]) + (lightingPlane.getRetVector().getZ() * GameController.lightDir[2]))
                /(Math.sqrt(GameController.lightDir[0] * GameController.lightDir[0] + GameController.lightDir[1] * GameController.lightDir[1] + GameController.lightDir[2] * GameController.lightDir[2])));

        lighting = Calculator.clamp(0.2 + 1 - Math.sqrt(Math.toDegrees(angle)/180),0,1);
    }

    //Accessor and modifier methods

    public void setDraw(boolean value) {
        draw = value;
    }

    public boolean getDraw() {
        return draw;
    }

    public boolean isWater() {
        return c.equals(GameController.waterBlue);
    }

    public boolean mouseOver() {
        return poly.contains(GameRunner.screenSize.getWidth()/2, GameRunner.screenSize.getHeight()/2);
    }

    public int getSide() {
        return side;
    }

    public int getID() {
        return id;
    }

    public double getDist() {
        double total = 0;
        for(int i=0; i<x.length; i++) {
            total += getDistanceToPoint(i);
        }
        return total / x.length;
    }

    public double getAvgDist() {
        return avgDist;
    }

    public double[] getX() { return x; }

    public double[] getY() { return y; }

    public double[] getZ() { return z; }

    public void setX(double[] value) { x = value; }

    public void setY(double[] value) { y = value; }

    public void setZ(double[] value) { z = value; } 

    //Math methods for calculating points to draw to

    private double getDistanceToPoint(int i) {
        return Math.sqrt((GameController.viewFrom[0]-x[i])*(GameController.viewFrom[0]-x[i]) + 
            (GameController.viewFrom[1]-y[i])*(GameController.viewFrom[1]-y[i]) +
            (GameController.viewFrom[2]-z[i])*(GameController.viewFrom[2]-z[i]));
    }

    private void updatePolygon(double[] x, double[] y) {
        poly.reset();
        for(int i = 0; i<x.length; i++)
        {
            poly.xpoints[i] = (int) x[i];
            poly.ypoints[i] = (int) y[i];
            poly.npoints = x.length;
        }
    }

    public void drawPolygon(Graphics g) { //fills the polygon using the 4 points previously calculated
        if(draw) { //if going to draw
            if((GameController.polygonOver == this)) { //if the polygon is selected then it will draw with full brightness
                g.setColor(c);
                g.fillPolygon(poly);
            } else { //draws the polygon with lighting adjustments
                g.setColor(new Color((int)(c.getRed() * GameController.lightPercentage), (int)(c.getGreen() * GameController.
                    lightPercentage), (int)(c.getBlue() * GameController.lightPercentage), c.getAlpha()));
                g.fillPolygon(poly);
            }
            if(outlines) { //draws wire frame outlines
                g.setColor(new Color(0, 0, 0));
                g.drawPolygon(poly);
            }
        }
    }
}