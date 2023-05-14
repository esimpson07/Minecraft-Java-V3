import java.util.Random; 
import java.util.Scanner;
 
/**
 * @author Edward Simpson
 * generates a cube type array that gets processed as the map
 * using code from http://devmag.org.za/2009/04/25/perlin-noise/
 */
public class NoiseGenerator {
    /**
     * @param width -> map width
     * @param height -> map width
     * @param octaveCount -> amount of times blending ground
     * @param persistence -> how uniform the ground is
     * @param worldHeight -> map height
     * @param minGroundHeight -> lowest spot for air
     * @param maxGroundHeight -> highest spot for ground
     * @param minDirtDepth -> least amount of dirt above stone
     * @param maxDirtDepth -> most dirt above stone
     * @param waterDepth -> height that water is filled at
     * @param treeCount -> amount of trees attempted to be placed
     */
    
    static int[][][] generatePerlinVolume (int width, int height, int octaveCount, float persistence, int worldHeight, int minGroundHeight, int maxGroundHeight, int minDirtDepth, int maxDirtDepth, int waterDepth, int treeCount) {
        int range = maxGroundHeight - minGroundHeight;
        Random r = new Random();
        
        int[][][] retVal = new int[width][height][worldHeight];
        
        // Fill with nothing
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < worldHeight; k++) {
                    retVal[i][j][k] = -1;
                }
            }
        }

        int[][] heightmap = generatePerlinNoise(width, height, octaveCount, persistence, minGroundHeight, maxGroundHeight);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int d = heightmap[i][j];
                int dirtDepth = minDirtDepth + (r.nextInt(maxDirtDepth - minDirtDepth));
                int dirtCount = 0;
                for (int k = maxGroundHeight - 1; k >= 0; k--) {
                    if (k >= d) { //nothing
                        retVal[i][j][k] = -1;
                    } else if(dirtCount == 0 && k >= waterDepth) {//setting the 1 grass block
                        retVal[i][j][k] = GameController.grass;
                        dirtCount ++;
                    } else if (dirtCount < dirtDepth) {//filling below grass with random amount of dirt
                        retVal[i][j][k] = GameController.dirt;
                        dirtCount ++;
                    } else if(k > 0) {//filling with stone
                        retVal[i][j][k] = GameController.stone;
                    } else {//bottom layer set as bedrock
                        retVal[i][j][k] = GameController.bedrock;
                    }
                }
            }
        }

        for(int i = 0; i < width; i ++) {
            for(int j = 0; j < height; j ++) {
                int d = heightmap[i][j];
                for(int k = waterDepth; k >= minGroundHeight; k --) {
                    if(retVal[i][j][k] == -1) {//if air -> change to water
                        retVal[i][j][k] = GameController.water;
                    }
                }
            }
        }
        addTrees(retVal,treeCount);

        return (retVal);
    }
    
    /**
     * @param mapdata     the generated Perlin Noise map
     * @param treeVolume  the amount of trees that will be attempted to be placed
     * @return 3d int array of the map including trees
     */
    
    static void addTrees(int[][][] mapdata, int treeVolume) {
        final int minTreeHeight = 3;
        final int maxTreeHeight = 6;
        
        int startX, startY, startZ;
        Random r = new Random();

        for (int t = 0; t < treeVolume; t++) {
            //find tree location, must be on grass
            int limiter = 0;
            boolean notFound = true;
            startX = 0;
            startY = 0;
            startZ = 0;
            
            while (limiter < treeVolume && notFound) {
                //find a random X / Y coordinate away from the edge of the map (radius of tree leaves will currently cause index out of range)
                startX = 3 + r.nextInt(mapdata.length - 6);
                startY = 3 + r.nextInt(mapdata[0].length - 6);
                startZ = 0;
                
                // for the current map, go from the top down
                for (int h = mapdata[0][0].length - 1; h > 0; h--) {
                    int val = mapdata[startX][startY][h];
                    //if the current block, going from the top, is occupied
                    if (val != 0) {
                        //is this a grass block?
                        if (val == GameController.grass) {
                            startZ = h + 1;
                        }
                        notFound = false;
                    }
                }
            }

            //grass block found
            if (startZ > 0) {
                //set random tree height
                int treeHeight = r.nextInt(maxTreeHeight - minTreeHeight) + minTreeHeight;
                //for each block of height, fill the wood
                for (int i = 0; i < treeHeight; i++) {
                    //only add block if not already occupied
                    if (mapdata[startX][startY][startZ + i] == -1) {
                        mapdata[startX][startY][startZ + i] = GameController.logs;
                    }
                }
                //leaves
                //random small or large
                int leavesSize = r.nextInt(2);
                //determine z max cap
                int cap = 3 + leavesSize;
                for (int z = 0; z < cap; z++) {
                    //if the z value is either the bottom or top, use smaller radius, otherwise use larger radius
                    int rad = z == 0 || z == (cap - 1) ? cap - (leavesSize * 2) : cap + (leavesSize);
                    //fill all blocks inside tree radius with leaves
                    for (int x = 0; x < rad; x++) {
                        for (int y = 0; y < rad; y++) {
                            //only add block if not already occupied
                            if (mapdata[startX + x - (rad/2)][startY + y - (rad/2)][startZ + treeHeight - 1 + z] == -1) {
                                mapdata[startX + x - (rad/2)][startY + y - (rad/2)][startZ + treeHeight - 1 + z] = GameController.leaves;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * @param width       width of noise array
     * @param height      height of noise array
     * @param octaveCount numbers of layers used for blending noise
     * @param persistence value of impact each layer get while blending
     * @param minHeight   minimum height of grass
     * @param maxHeight   maximum height of grass
     * @return float array containing calculated "Perlin-Noise" values
     */
    
    static int[][] generatePerlinNoise(int width, int height, int octaveCount, float persistence, int minHeight, int maxHeight) {
        int[][] retVal = new int[width][height];
        
        Random seed = new Random();
        final float[][] noise = generatePerlinNoise(width, height, octaveCount, persistence, seed.nextLong());
        
        int range = maxHeight - minHeight;
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                float f = noise[i][j];
                int v = ((int)(f * range)) + minHeight;
                retVal[i][j] = v;
            }
        }
        
        return (retVal);
    }
    
    /**
     * @param width       width of noise array
     * @param height      height of noise array
     * @param octaveCount numbers of layers used for blending noise
     * @param persistence value of impact each layer get while blending
     * @param seed        used for randomizer
     * @return float array containing calculated "Perlin-Noise" values
     */
    static float[][] generatePerlinNoise(int width, int height, int octaveCount, float persistence, long seed) {
        final float[][] base = new float[width][height];
        final float[][] perlinNoise = new float[width][height];
        final float[][][] noiseLayers = new float[octaveCount][][];
 
        Random random = new Random(seed);
        //fill base array with random values as base for noise
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                base[x][y] = random.nextFloat();
            }
        }
 
        //calculate octaves with different roughness
        for (int octave = 0; octave < octaveCount; octave++) {
            noiseLayers[octave] = generatePerlinNoiseLayer(base, width, height, octave);
        }
 
        float amplitude = 1f;
        float totalAmplitude = 0f;
 
        //calculate perlin noise by blending each layer together with specific persistence
        for (int octave = octaveCount - 1; octave >= 0; octave--) {
            amplitude *= persistence;
            totalAmplitude += amplitude;
 
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    //adding each value of the noise layer to the noise
                    //by increasing amplitude the rougher noises will have more impact
                    perlinNoise[x][y] += noiseLayers[octave][x][y] * amplitude;
                }
            }
        }
 
        //normalize values so that they stay between 0..1
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                perlinNoise[x][y] /= totalAmplitude;
            }
        }
 
        return perlinNoise;
    }
 
    /**
     * @param base   base random float array
     * @param width  width of noise array
     * @param height height of noise array
     * @param octave current layer
     * @return float array containing calculated "Perlin-Noise-Layer" values
     */
    static float[][] generatePerlinNoiseLayer(float[][] base, int width, int height, int octave) {
        float[][] perlinNoiseLayer = new float[width][height];
 
        //calculate period (wavelength) for different shapes
        int period = 1 << octave; //2^k
        float frequency = 1f / period; // 1/2^k
 
        for (int x = 0; x < width; x++) {
            //calculates the horizontal sampling indices
            int x0 = (x / period) * period;
            int x1 = (x0 + period) % width;
            float horizintalBlend = (x - x0) * frequency;
 
            for (int y = 0; y < height; y++) {
                //calculates the vertical sampling indices
                int y0 = (y / period) * period;
                int y1 = (y0 + period) % height;
                float verticalBlend = (y - y0) * frequency;
 
                //blend top corners
                float top = interpolate(base[x0][y0], base[x1][y0], horizintalBlend);
 
                //blend bottom corners
                float bottom = interpolate(base[x0][y1], base[x1][y1], horizintalBlend);
 
                //blend top and bottom interpolation to get the final blend value for this cell
                perlinNoiseLayer[x][y] = interpolate(top, bottom, verticalBlend);
            }
        }
 
        return perlinNoiseLayer;
    }
 
    /**
     * @param a -> value of point a
     * @param b -> value of point b
     * @param alpha determines which value is more impactful (0 -> a, 1 -> b)
     * @return interpolated value
     */
    static float interpolate(float a, float b, float alpha) {
        return a * (1 - alpha) + alpha * b;
    }
}