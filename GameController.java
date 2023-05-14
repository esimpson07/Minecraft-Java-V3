import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class GameController extends JPanel implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    Robot robot;

    static ArrayList<DPolygon> DPolygons;
    static DPolygon polygonOver;
    static Chunk[] chunks;

    //Minecraft style font from resource folder and images for hotbar and the selector

    private String fontFilePath = "Resources/Fonts/font.ttf";
    private String hotbarFilePath = "Resources/Images/hotbar.png";
    private String selectorFilePath = "Resources/Images/selector.png";

    private Font minecraftFont;
    private BufferedImage hotbar;
    private BufferedImage selector;

    static double[] viewFrom = new double[]{0, 0, 0},
                    viewTo = new double[]{0, 0, 0},
                    lightDir = new double[]{1, 1, 1};

    static double zoom = 1000, minZoom = 500, maxZoom = 2500;
    
    static double lightPercentage = 1, time = 0, dayCycle = 3600;

    private double drawFPS = 0, maxFPS = 40, sleepTime = 1000.0/maxFPS;
    private double vertLook = 0, horLook = 0, horRotSpeed = 900, vertRotSpeed = 2200;
    private double lastRefresh = 0, lastFPSCheck = 0, checks = 0;
    private double movementFactor = 0.085, gravity = 0.01, jumpVel = 0.16, zVel = 0;
    private double heightTol = 1.4, sideTol = 0.8;
    private double mouseAimSize = 4;

    private int[] newPolygonOrder;

    private boolean canJump = false;

    private boolean[] Keys = new boolean[8];

    private int selectedItem = 1;
    private int selectedCube = -1;
    private int selectedFace = -1;

    //All of the variables for world generation -> all sizes are in cubes (meters)

    static final int worldSize = 32;
    static final int chunkSize = 1;
    static final int worldHeight = 32;
    static final double renderDistance = 16;
    static final double renderDistanceInchunks = renderDistance / chunkSize;

    //Names of the colors for hotbar selection -> using type values

    static final String[] colorNames = new String[]{"stone","cobblestone","dirt","grass","planks","logs","leaves","sand","glass","bedrock"};

    //All the values of the cube types -> used primarily for map generation, but also for color picking on the polygons

    static final int stone = 0;
    static final int cobblestone = 1;
    static final int dirt = 2;
    static final int grass = 3;
    static final int planks = 4;
    static final int logs = 5;
    static final int leaves = 6;
    static final int sand = 7;
    static final int glass = 8;
    static final int water = 9;
    static final int bedrock = 10;

    //All the colors used for cubes in the game -> gets different colors for different polygons on certain cube types

    static Color darkGreen = new Color(0,170,0);
    static Color lightGreen = new Color(0,210,0);
    static Color waterBlue = new Color(0,0,180,120);
    static Color black = new Color(20,20,20);
    static Color darkGray = new Color(65,65,65);
    static Color lightGray = new Color(100,100,100);
    static Color darkBrown = new Color(77,47,18);
    static Color midBrown = new Color(133,73,45);
    static Color lightBrown = new Color(175,125,77);
    static Color beige = new Color(232,214,158);
    static Color translucent = new Color(200, 200, 230, 40);
    static Color bgColor = new Color(50,150,255);

    public GameController() {
        this.addKeyListener(this);
        setFocusable(true);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);

        initMouse();
        initVars();
        loadResources();
        loadCubes();
        refreshAllCubes();
    }

    private void initVars() {
        DPolygons = new ArrayList<DPolygon>();
        polygonOver = null;
        chunks = new Chunk[(worldSize / chunkSize) * (worldSize / chunkSize)];
    }

    private void loadResources() {
        try {
            hotbar = ImageIO.read(new File(hotbarFilePath));
            selector = ImageIO.read(new File(selectorFilePath));
            minecraftFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontFilePath)).deriveFont(32f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(minecraftFont);
        } catch (IOException e) {
            e.printStackTrace();
        } catch(FontFormatException e) {
            e.printStackTrace();
        }
    }

    private void loadCubes() {
        final float persistence = 0.17f;
        final float treeDensity = 0.03f;
        final int octaveCount = 4;
        final int minHeight = 14;
        final int maxHeight = 22;
        final int minDirtDepth = 2;
        final int maxDirtDepth = 3;
        final int waterDepth = 16;
        final int treeCount = (int)(treeDensity * Math.pow(worldSize, 2));

        chunks = new Chunk[(worldSize / chunkSize) * (worldSize / chunkSize)];

        System.out.println("Starting map generation!");

        int[][][] map = NoiseGenerator.generatePerlinVolume(worldSize, worldSize, octaveCount, persistence, worldHeight, minHeight, maxHeight, minDirtDepth, maxDirtDepth, waterDepth, treeCount);
        for(int x = 0; x < worldSize / chunkSize; x ++) {
            for(int y = 0; y < worldSize / chunkSize; y ++) {
                chunks[x + (y * worldSize / chunkSize)] = new Chunk(map,chunkSize,worldHeight,x,y);
            }
        }

        System.out.println("Map generated.");

        viewFrom[0] = worldSize / 2 + 0.5;
        viewFrom[1] = worldSize / 2 + 0.5;
        for(int i = 0; i < map[worldSize / 2][worldSize / 2].length; i ++) {
            if(map[worldSize / 2][worldSize / 2][i] == -1) {
                viewFrom[2] = i + 1;
                break;
            }
        }

        System.out.println("Initially drawing chunks.");

        for(int i = 0; i < chunks.length; i ++) {
            drawChunk(i);
            System.out.println(100 * (double)(i + 1) / (double)chunks.length + "%");
        }

        System.out.println("Done drawing chunks.");
    }

    private void initMouse() {
        try {
            robot = new Robot();
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Cursor invisibleCursor = toolkit.createCustomCursor(new BufferedImage(1, 1, BufferedImage.TRANSLUCENT), new Point(0,0), "InvisibleCursor");        
            setCursor(invisibleCursor);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void refreshAllCubes() {
        System.out.println("Checking cube adjacencies & deleting faces");
        for(int i = 0; i < chunks.length; i ++) {
            if(chunks[i] != null) {
                for(int j = 0; j < chunks[i].getCubeArray().size(); j ++) {
                    chunks[i].getCubeArray().get(j).softAdjacencyCheck();
                }
            }
            System.out.println(100 * (double)(i + 1) / chunks.length + "%");
        }
        System.out.println("Done checking adjacencies. Finally updating polygons.");
        for(int i = 0; i < chunks.length; i ++) {
            if(chunks[i] != null) {
                for(int j = 0; j < chunks[i].getCubeArray().size(); j ++) {
                    chunks[i].getCubeArray().get(j).updatePoly();
                }
            }
            System.out.println(100 * (double)(i + 1) / chunks.length + "%");
        }

        System.out.println("Done!");
    }

    public void drawChunk(int i) {
        if(!chunks[i].isAlreadyInMap()) {
            for(int j = 0; j < chunks[i].getCubeArray().size(); j ++) {
                chunks[i].getCubeArray().get(j).updatePoly();
            }
            chunks[i].addToMap();
        }
    }

    public void undrawChunk(int i) {
        if(chunks[i].isAlreadyInMap()) {
            for(int j = 0; j < chunks[i].getCubeArray().size(); j ++) {
                chunks[i].getCubeArray().get(j).removeCubeInChunk();
            }
            chunks[i].removeFromMap();
        }
    }

    public void determineChunksToDraw() {
        for(int i = 0; i < chunks.length; i ++) {
            if(chunks[i].getDistFromCenter(viewFrom[0] / (double)chunkSize, viewFrom[1] / (double)chunkSize) <= renderDistanceInchunks) {
                drawChunk(i);
            } else {
                undrawChunk(i);
            }
        }
    }

    public int[] getChunkCoordsIn(int x, int y) {
        return new int[]{x / chunkSize,y / chunkSize};
    }

    static int getChunkNumberIn(int x, int y) {
        for(int i = 0; i < chunks.length; i ++) {
            if(chunks[i].getX() == x / chunkSize && chunks[i].getY() == y / chunkSize) {
                return i;
            }
        } 
        return -1;
    }

    private boolean withinRange(double val, double min, double max) {
        return val <= max && val >= min;
    }

    private boolean willCollide(double[] attrs) {
        if(withinRange(attrs[0],0,worldSize) && withinRange(attrs[1],0,worldSize) && withinRange(attrs[2],0,worldHeight)) { //checking if the coordinate is within the world limits
            double x = attrs[0] + (attrs[3] / 2);
            double y = attrs[1] + (attrs[4] / 2);
            double z = attrs[2] + (attrs[5] / 2);
            double px = viewFrom[0];
            double py = viewFrom[1];
            double pz = viewFrom[2];
            double xDiff = Math.abs(x - px);
            double yDiff = Math.abs(y - py);
            double zDiff = Math.abs(z - pz);
            if(zDiff <= heightTol + 0.5 - 0.005 && xDiff <= sideTol - 0.005 && yDiff <= sideTol - 0.005) { //checking if the coordinate is inside of the player
                return(true);
            } else {
                return false;
            }
        }
        return true;
    }

    private void drawMouseAim(Graphics g) {
        g.setColor(Color.black);
        g.drawLine((int)(GameRunner.screenSize.getWidth()/2 - mouseAimSize), (int)(GameRunner.screenSize.getHeight()/2), (int)(GameRunner.screenSize.getWidth()/2 + mouseAimSize), (int)(GameRunner.screenSize.getHeight()/2));
        g.drawLine((int)(GameRunner.screenSize.getWidth()/2), (int)(GameRunner.screenSize.getHeight()/2 - mouseAimSize), (int)(GameRunner.screenSize.getWidth()/2), (int)(GameRunner.screenSize.getHeight()/2 + mouseAimSize));            
    }

    private void centerMouse() {
        robot.mouseMove((int)GameRunner.screenSize.getWidth()/2, (int)GameRunner.screenSize.getHeight()/2);
    }

    void mouseMovement(double newMouseX, double newMouseY) {        
        double difX = (newMouseX - GameRunner.screenSize.getWidth()/2);
        double difY = (newMouseY - GameRunner.screenSize.getHeight()/2);
        difY *= 6 - Math.abs(vertLook) * 5;
        vertLook -= difY  / vertRotSpeed;
        horLook += difX / horRotSpeed;

        vertLook = Calculator.clamp(vertLook,-0.99999,0.99999);

        updateView();
    }

    void cameraMovement() {
        Vector viewVector = new Vector(viewTo[0] - viewFrom[0], viewTo[1] - viewFrom[1], viewTo[2] - viewFrom[2]);
        double xMove = 0, yMove = 0, zMove = 0;
        double adjMovementFactor = (60.0 * movementFactor) / Calculator.clamp(drawFPS,15,maxFPS);

        if(Keys[6]) {
            adjMovementFactor *= 1.4;
        }

        Vector VerticalVector = new Vector (0, 0, 1);
        Vector sideViewVector = viewVector.crossProduct(VerticalVector);

        viewFrom[2] += zVel;
        zVel -= gravity;
        zVel = Calculator.clamp(zVel,-15,jumpVel);

        if(Keys[0])
        {
            xMove += (adjMovementFactor * viewVector.getX());
            yMove += (adjMovementFactor * viewVector.getY());
        }

        if(Keys[2])
        {
            xMove -= (adjMovementFactor * viewVector.getX());
            yMove -= (adjMovementFactor * viewVector.getY());
        }

        if(Keys[1])
        {
            xMove += (adjMovementFactor * sideViewVector.getX());
            yMove += (adjMovementFactor * sideViewVector.getY());
        }

        if(Keys[3])
        {
            xMove -= (adjMovementFactor * sideViewVector.getX());
            yMove -= (adjMovementFactor * sideViewVector.getY());
        }

        Vector moveVector = new Vector(xMove, yMove, zMove);
        moveTo(viewFrom[0] + moveVector.getX() * adjMovementFactor, viewFrom[1] + moveVector.getY() * adjMovementFactor, viewFrom[2] + moveVector.getZ() * adjMovementFactor);

        checkCollisions(adjMovementFactor);

        updateView();
    }

    private void checkCollisions(double adjMovementFactor) {
        for(int i = 0; i < chunks.length; i ++) {
            if(chunks[i] != null) {
                if(chunks[i].getDistFromCenter(viewFrom[0] / (double)chunkSize,viewFrom[1] / (double)chunkSize) <= 1.41 * (double)chunkSize) {
                    for(int j = 0; j < chunks[i].getCubeArray().size(); j ++) {
                        if(chunks[i].getCubeArray().get(j).getDist(viewFrom[0],viewFrom[1],viewFrom[2]) < 3 && !chunks[i].getCubeArray().get(j).isWater()) {
                            double[] attrs = chunks[i].getCubeArray().get(j).getAttributes();
                            double x = attrs[0] + (attrs[3] / 2);
                            double y = attrs[1] + (attrs[4] / 2);
                            double z = attrs[2] + (attrs[5] / 2);
                            double px = viewFrom[0];
                            double py = viewFrom[1];
                            double pz = viewFrom[2];
                            double xDiff = Math.abs(x - px);//Calculator.roundTo(Math.abs(x - px),4);
                            double yDiff = Math.abs(y - py);//Calculator.roundTo(Math.abs(y - py),4);
                            double zDiff = Math.abs(z - pz);//Calculator.roundTo(Math.abs(z - pz),4);
                            double hzDiff = Math.abs(z + 0.5 - pz);
                            double pError = 0.001;
                            if(zDiff <= heightTol + 0.5 && xDiff <= sideTol - pError && yDiff <= sideTol - pError) {
                                if(hzDiff < 1 && yDiff > xDiff + pError && py >= y + (sideTol - adjMovementFactor)) { //checking if colliding in y direction from + side of block
                                    viewFrom[1] = y + sideTol;
                                } else if(hzDiff < 1 && yDiff > xDiff + pError && py <= y - (sideTol - adjMovementFactor)) { //checking y -
                                    viewFrom[1] = y - sideTol;
                                } else if(hzDiff < 1 && xDiff > yDiff + pError && px >= x + (sideTol - adjMovementFactor)) { //checking x +
                                    viewFrom[0] = x + sideTol;
                                } else if(hzDiff < 1 && xDiff > yDiff + pError && px <= x - (sideTol - adjMovementFactor)) { //checking x -
                                    viewFrom[0] = x - sideTol;
                                } else if(zDiff < heightTol + 0.5 && pz >= z + (1.5 - adjMovementFactor) && xDiff < (sideTol - adjMovementFactor) - 0.005 && yDiff < (sideTol - adjMovementFactor) - 0.005) {
                                    viewFrom[2] = z + heightTol + 0.5; //checking z + and resetting jump and stopping speed
                                    zVel = 0;
                                    canJump = true;
                                } else if(zDiff < heightTol - 0.5 - pError && pz <= z - (0.5 - adjMovementFactor) && xDiff < (sideTol - adjMovementFactor) - 0.005 && yDiff < (sideTol - adjMovementFactor) - 0.005) {
                                    viewFrom[2] = z - heightTol + 0.5; //checking z - and stopping speed
                                    zVel = 0;
                                }
                            }
                        }
                    }
                }
            }
        }

        //making sure the player stays within world bounds

        viewFrom[0] = Calculator.clamp(viewFrom[0],0,worldSize);
        viewFrom[1] = Calculator.clamp(viewFrom[1],0,worldSize);
    }

    void moveTo(double x, double y, double z) {
        viewFrom[0] = x;
        viewFrom[1] = y;
        viewFrom[2] = z;
    }

    void updateView() {
        double r = Math.sqrt(1 - (vertLook * vertLook));
        viewTo[0] = viewFrom[0] + r * Math.cos(horLook);
        viewTo[1] = viewFrom[1] + r * Math.sin(horLook);        
        viewTo[2] = viewFrom[2] + vertLook;
    }

    void setPolygonOver() {
        polygonOver = null;
        selectedCube = -1;
        for(int i = newPolygonOrder.length-1; i >= 0; i --) {
            if(DPolygons.get(newPolygonOrder[i]).getDist() <= 6) {
                if(DPolygons.get(newPolygonOrder[i]).mouseOver() && DPolygons.get(newPolygonOrder[i]).getDraw() 
                && !DPolygons.get(newPolygonOrder[i]).isWater())
                {
                    polygonOver = DPolygons.get(newPolygonOrder[i]);
                    selectedCube = DPolygons.get(newPolygonOrder[i]).getID();
                    selectedFace = DPolygons.get(newPolygonOrder[i]).getSide();
                    break;
                }
            }
        }
    }

    void sleepAndRefresh() {
        long timeSLU = (long) (System.currentTimeMillis() - lastRefresh); 

        checks ++;            
        if(checks >= 15)
        {
            drawFPS = checks/((System.currentTimeMillis() - lastFPSCheck)/1000.0);
            lastFPSCheck = System.currentTimeMillis();
            checks = 0;
        }

        if(timeSLU < 1000.0/maxFPS)
        {
            try {
                Thread.sleep((long) (1000.0/maxFPS - timeSLU));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }    
        }

        lastRefresh = System.currentTimeMillis();

        repaint();
    }

    public void paintComponent(Graphics g)
    {
        //Clear screen and draw background color
        time = System.currentTimeMillis() % dayCycle;
        lightPercentage = 0.75 * Math.pow(Math.sin((2 * Math.PI * time) / dayCycle),2) + 0.25;
        Color fillColor = new Color((int)((double)bgColor.getRed() * lightPercentage), (int)((double)bgColor.getGreen() * lightPercentage), (int)((double)bgColor.getBlue() * lightPercentage));
        g.setColor(fillColor);
        g.fillRect(0, 0, (int)GameRunner.screenSize.getWidth(), (int)GameRunner.screenSize.getHeight());

        cameraMovement();

        determineChunksToDraw();

        //Calculated all that is general for this camera position
        Calculator.setPredeterminedInfo();

        //Updates each polygon for this camera position
        for(int i = 0; i < DPolygons.size(); i ++) {
            DPolygons.get(i).updatePolygon();
        }

        //Set drawing order so closest polygons gets drawn last
        setOrder();

        //Set the polygon that the mouse is currently over
        setPolygonOver();

        //draw polygons in the Order that is set by the 'setOrder' function

        for(int i = 0; i < newPolygonOrder.length; i++) {
            DPolygons.get(newPolygonOrder[i]).drawPolygon(g);
        }
        //draw the cross in the center of the screen
        drawMouseAim(g);            

        //FPS display
        drawDeveloperTools(g);

        drawHotBar(g);

        drawSelectedItemText(g, colorNames[selectedItem - 1], minecraftFont);

        sleepAndRefresh();
    }

    private void drawDeveloperTools(Graphics g) { //draws FPS tracker and X Y Z coordinates
        g.setFont(minecraftFont);
        g.drawString("FPS: " + (int)drawFPS, 40, 40);
        g.drawString("X Y Z: " + Calculator.roundTo(viewFrom[0] - worldSize / 2,2) + " "  + Calculator.roundTo(viewFrom[1] - worldSize / 2,2) + " "  + Calculator.roundTo(viewFrom[2],2), 40, 80);
    }

    private void drawHotBar(Graphics g) { //draws the hotbar image and the selector, as well as the blocks inside the hotbar
        g.drawImage(hotbar,((int)GameRunner.screenSize.getWidth() - hotbar.getWidth()) / 2, (int)GameRunner.screenSize.getHeight() - hotbar.getHeight(), null);
        //0 at coordinate 121,28 -> increments of 92.2 pixels
        g.drawImage(selector,((int)GameRunner.screenSize.getWidth() - hotbar.getWidth()) / 2 + 121 + (int)(92.2 * (selectedItem - 1)),(int)GameRunner.screenSize.getHeight() - hotbar.getHeight() - 5, null);

        for(int i = 0; i < 9; i ++) {
            int xCoord = ((int)GameRunner.screenSize.getWidth() - hotbar.getWidth()) / 2 + 175 + (int)(92.2 * i);
            int yCoord = (int)GameRunner.screenSize.getHeight() - (hotbar.getHeight() / 2);
            int xOffset = 25;
            int yOffset = 25;

            g.setColor(Cube.getColor(i)[2]);
            g.fillRect(xCoord - xOffset, yCoord - yOffset, xOffset * 2, yOffset * 2);
            if(i == 3) {
                g.setColor(Cube.getColor(i)[1]);
                g.fillRect(xCoord - xOffset, yCoord - yOffset, xOffset * 2, (int)(yOffset * 0.25));
            } else if(i == 5) {
                g.setColor(Cube.getColor(i)[1]);
                g.fillRect(xCoord - xOffset, yCoord - yOffset, xOffset * 2, (int)(yOffset * 0.25));
            }
        }
    }

    private void drawSelectedItemText(Graphics g, String text, Font font) { //draws the name of the item selected 
        FontMetrics metrics = g.getFontMetrics(font);
        int x = ((int)GameRunner.screenSize.getWidth() - metrics.stringWidth(text)) / 2;
        int pxDist = 10;
        int y = (int)GameRunner.screenSize.getHeight() - hotbar.getHeight() - metrics.getHeight() - pxDist;
        g.setColor(Color.BLACK);
        g.setFont(font);
        g.drawString(text, x, y);
    }

    void setOrder() {
        double[] k = new double[DPolygons.size()];
        newPolygonOrder = new int[DPolygons.size()];

        for(int i=0; i<DPolygons.size(); i++)
        {
            k[i] = DPolygons.get(i).getAvgDist();
            newPolygonOrder[i] = i;
        }

        double temp;
        int tempr;        
        for (int a = 0; a < k.length-1; a++) {
            for (int b = 0; b < k.length-1; b++) {
                if(k[b] < k[b + 1])
                {
                    temp = k[b];
                    tempr = newPolygonOrder[b];
                    newPolygonOrder[b] = newPolygonOrder[b + 1];
                    k[b] = k[b + 1];

                    newPolygonOrder[b + 1] = tempr;
                    k[b + 1] = temp;
                }
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_W)
            Keys[0] = true;
        if(e.getKeyCode() == KeyEvent.VK_A)
            Keys[1] = true;
        if(e.getKeyCode() == KeyEvent.VK_S)
            Keys[2] = true;
        if(e.getKeyCode() == KeyEvent.VK_D)
            Keys[3] = true;
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            Keys[4] = true;
            if(canJump) {
                zVel = jumpVel;
                viewFrom[2] += 0.01;
                canJump = false;
            }
        }
        if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
            Keys[5] = true;
        }
        if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
            Keys[6] = true;
        }
        if(e.getKeyCode() == KeyEvent.VK_F) {
            Keys[7] = true;
        }
        if(e.getKeyCode() == KeyEvent.VK_1) { selectedItem = 1; }
        if(e.getKeyCode() == KeyEvent.VK_2) { selectedItem = 2; }
        if(e.getKeyCode() == KeyEvent.VK_3) { selectedItem = 3; }
        if(e.getKeyCode() == KeyEvent.VK_4) { selectedItem = 4; }
        if(e.getKeyCode() == KeyEvent.VK_5) { selectedItem = 5; }
        if(e.getKeyCode() == KeyEvent.VK_6) { selectedItem = 6; }
        if(e.getKeyCode() == KeyEvent.VK_7) { selectedItem = 7; }
        if(e.getKeyCode() == KeyEvent.VK_8) { selectedItem = 8; }
        if(e.getKeyCode() == KeyEvent.VK_9) { selectedItem = 9; }
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
            System.exit(0);
    }

    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_W)
            Keys[0] = false;
        if(e.getKeyCode() == KeyEvent.VK_A)
            Keys[1] = false;
        if(e.getKeyCode() == KeyEvent.VK_S)
            Keys[2] = false;
        if(e.getKeyCode() == KeyEvent.VK_D)
            Keys[3] = false;
        if(e.getKeyCode() == KeyEvent.VK_SPACE)
            Keys[4] = false;
        if(e.getKeyCode() == KeyEvent.VK_SHIFT)
            Keys[5] = false;
        if(e.getKeyCode() == KeyEvent.VK_CONTROL) 
            Keys[6] = false;
        if(e.getKeyCode() == KeyEvent.VK_F) 
            Keys[7] = false;
    }

    public void keyTyped(KeyEvent e) {

    }

    public void mouseDragged(MouseEvent m) {
        mouseMovement(m.getX(), m.getY());
        centerMouse();
    }

    public void mouseMoved(MouseEvent m) {
        mouseMovement(m.getX(), m.getY());
        centerMouse();
    }

    public void mouseClicked(MouseEvent m) {}

    public void mouseEntered(MouseEvent m) {}

    public void mouseExited(MouseEvent m) {}

    public void mousePressed(MouseEvent m) {
        if(m.getButton() == MouseEvent.BUTTON1) {
            if(selectedCube != -1) {
                for(int i = 0; i < chunks.length; i ++) {
                    for(int j = 0; j < chunks[i].getCubeArray().size(); j ++) {
                        if(chunks[i].getCubeArray().get(j).getID() == selectedCube && !chunks[i].getCubeArray().get(j).isWater() && !chunks[i].getCubeArray().get(j).isBedrock()) {
                            double[][] adjacentCoords = new double[7][3];
                            for(int f = 0; f < 6; f ++) {
                                adjacentCoords[f] = chunks[i].getCubeArray().get(j).getAdjacentCube(f);
                            }
                            adjacentCoords[6] = new double[]{chunks[i].getCubeArray().get(j).getCoords()[0],
                                chunks[i].getCubeArray().get(j).getCoords()[1], chunks[i].getCubeArray().get(j).getCoords()[2]};
                            chunks[i].getCubeArray().get(j).removeCube();
                            chunks[i].getCubeArray().get(0).hardAdjacencyCheck(adjacentCoords);
                        }
                    }
                }
            }
        }

        if(m.getButton() == MouseEvent.BUTTON3) {
            if(selectedCube != -1) {
                for(int i = 0; i < chunks.length; i ++) {
                    for(int j = 0; j < chunks[i].getCubeArray().size(); j ++) {
                        if(chunks[i].getCubeArray().get(j).getID() == selectedCube && !chunks[i].getCubeArray().get(j).isWater()) {
                            double[] coords = chunks[i].getCubeArray().get(j).getAdjacentCube(selectedFace);
                            int chunkIn = getChunkNumberIn((int)coords[0],(int)coords[1]);
                            if(!willCollide(new double[]{coords[0],coords[1],coords[2],1,1,1})) {
                                chunks[chunkIn].addCube(new Cube(coords[0],coords[1],coords[2],1,1,1,selectedItem - 1));
                                chunks[chunkIn].getCubeArray().get(chunks[chunkIn].getCubeArray().size() - 1).hardAdjacencyCheck();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    public void mouseReleased(MouseEvent m) {}

    public void mouseWheelMoved(MouseWheelEvent m) {
        if(Keys[7]) {
            zoom -= 25 * m.getUnitsToScroll();
            zoom = Calculator.clamp(zoom,minZoom,maxZoom);
        } else {
            if(Math.abs(m.getUnitsToScroll()) > 2) {
                selectedItem += m.getUnitsToScroll() / Math.abs(m.getUnitsToScroll());
                selectedItem = Calculator.roll(selectedItem,1,9);
            }
        }
    }
}