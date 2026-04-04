import codedraw.CodeDraw;

import java.awt.Color;
import java.util.Random;

/**
 * Main class for the Barnes-Hut simulation.
 * <p>
 * Invariant: Runs the simulation loop and applies gravity to all bodies continuously.
 */
public class Simulation {
  // --- BEGIN: Physical constants (Do not change, else the simulation will not be based on real numbers)
  // Gravitational constant
  public static final double G = 6.6743e-11;
  // One astronomical unit (AU) is the average distance of earth to the sun.
  public static final double AU = 150e9; // meters
  // Sun as reference
  public static final double SUN_MASS = 1.989e30; // kilograms
  public static final double SUN_RADIUS = 696340e3; // meters
  // --- END: Physical constants


  // --- BEGIN: Simulation globals (Try changing these to see what happens)
  // ca. 15.0e-6 ... smaller SIMULATION_G makes the simulation more precise, but much slower
  public static final double SIMULATION_G = G * 200000;
  // d/r < T used for Octree, d is side length of quadrant, r is distance between body and center of quadrant
  public static final int T = 1;
  // Size of the canvas in x/y dimensions (creates cube)
  public static final int CANVAS_SIZE = 1_000; // pixels

  // Set to true to draw boundaries of the octets (also sets all z axis for movement vectors and mass centers of bodies to 0,
  // so that the bodies are on the same plane)
  public static final boolean drawBoundaries = false;
  // Set to true to generate two galaxies colliding
  public static final boolean generateCollidingGalaxies = false;  // If true, takes precedence over the 'generateGalaxy'
  // Set to true to generate a single rotating disc galaxy
  public static final boolean generateGalaxy = false;
  // Section size is the length of the side 1/2 of the drawing space (basically edge length of on of the first octets)
  public static final double SECTION_SIZE = 10 * AU; // Cannot be 0

  // Overall number of spawned initial bodies in the system
  public static final int NUMBER_OF_BODIES = 10_000;
  // Overall weight of the solar system
  public static final double OVERALL_SYSTEM_MASS = 200 * SUN_MASS; // Kilograms
  // --- END: Simulation globals

  /**
   * Initializes the CodeDraw canvas, generates the simulation bodies, and runs the infinite simulation loop.
   *
   * @param args command-line arguments (currently unused).
   *             Precondition: Global constants (NUMBER_OF_BODIES, AU, SUN_MASS) are defined.
   *             Postcondition: This method loops infinitely and does not return under normal execution.
   */
  public static void main(String[] args) {
    // Initialize the canvas
    CodeDraw cd = new CodeDraw(CANVAS_SIZE, CANVAS_SIZE);

    Random random = new Random(2022);


    // Works like associative map (linear)
    Body[] bodiesArray;

    // Initialize bodies using the requested global toggle
    if (generateCollidingGalaxies) {
      bodiesArray = createCollidingGalaxies(NUMBER_OF_BODIES, random);
    } else if (generateGalaxy) {
      bodiesArray = createGalaxy(NUMBER_OF_BODIES, random);
    } else {
      bodiesArray = createRandomCloud(NUMBER_OF_BODIES, random);
    }
    Vector3[] forcesArray = new Vector3[bodiesArray.length];    // Used in simulation to save all forces acting on that body

    // Min point and max point for the symmetrical first bounding cube (since 0d,0d,0d is the middle, extremes are +-SECTION_SIZE in all directions)
    Vector3 minPoint = new Vector3(-SECTION_SIZE, -SECTION_SIZE, -SECTION_SIZE);
    Vector3 maxPoint = new Vector3(SECTION_SIZE, SECTION_SIZE, SECTION_SIZE);

    BoundaryCube mainBoundaryCube = new BoundaryCube(minPoint, maxPoint);

    Octree octrNew;
    while (true) {
      // 1. Create new octree structure
      octrNew = new Octree(mainBoundaryCube);

      // 2. Insert all bodies into the structure (Optional: Do not consider bodies outside the bounds anymore)
      for (int i = 0; i < NUMBER_OF_BODIES; i++) {
        // Version without deletion
        // octrNew.insertBody(bodiesArray[i])

        // Version with deletion (Saves some computational time if bodies are outside the simulation space)
        if (bodiesArray[i] != null) {
          if (!octrNew.insertBody(bodiesArray[i])) {
            bodiesArray[i] = null; // Delete body if it falls out of bounds
          }
        }
      }

      // 3. After all the bodies are in the octree, calculate the gravitational centers for each node
      octrNew.updateCenters();

      // 4. For each body, calculate the force exerted on that body by other bodies
      for (int i = 0; i < NUMBER_OF_BODIES; i++) {
        if (bodiesArray[i] != null) {
          forcesArray[i] = octrNew.forceExertedOnBody(bodiesArray[i]);
        }
      }

      // 5. Move all the bodies by one step, based on forces exerted on them (updates the positons and speed vectors)
      for (int i = 0; i < NUMBER_OF_BODIES; i++) {
        if (bodiesArray[i] != null) {
          bodiesArray[i].move(forcesArray[i]);
        }
      }

      // 6. Reset canvas, redraw new positions and optionally draw boundaries of octree
      cd.clear(Color.BLACK);
      octrNew.drawBodies(cd);
      if (drawBoundaries) {
        octrNew.drawBoundaries(cd);
      }
      cd.show();

      // 7. Set all references in octree recursively to null, so that the garbage collector can collect them in one sweep
      // This improves the peek RAM usage
      octrNew.deleteOctree();
    }
  }

  /**
   * Creates a rotating spiral galaxy around a central mass.
   * Uses Keplerian velocity formulation: v = sqrt((G * M) / r).
   */
  private static Body[] createGalaxy(int numOfBodies, Random random) {
    Body[] bodiesArray = new Body[numOfBodies];

    // 1. Create the anchor (Supermassive Black Hole) at the origin of the system
    // Assign it 90% of the entire system's mass
    double centralMass = OVERALL_SYSTEM_MASS * 0.9;
    bodiesArray[0] = new Body(centralMass,
        new Vector3(0, 0, 0),
        new Vector3(0, 0, 0)
    );

    double remainingMass = OVERALL_SYSTEM_MASS * 0.1;
    double avgStarMass = remainingMass / (NUMBER_OF_BODIES - 1);

    // 2. Generate the spinning spiral disc
    int numArms = 2;              // Number of primary spiral arms
    double armOffsetMax = 0.5;    // Standard deviation scatter out of the arms
    double rotationFactor = 5.0;  // How tightly the arms are wound
    double spinMultiplier = 1.3;  // Increases or decreases average orbital speed (keep under ~1.41 to prevent galaxy dispersing)

    Vector3 corePosition = new Vector3(0, 0, 0);
    Vector3 bulkVelocity = new Vector3(0, 0, 0);
    double radiusScalar = SECTION_SIZE * 0.2; // Changes the size of the galaxy

    for (int i = 1; i < NUMBER_OF_BODIES; i++) {
      initGalaxyHelper(random, bodiesArray, centralMass, avgStarMass, corePosition, bulkVelocity, numArms, armOffsetMax, rotationFactor, radiusScalar,
          spinMultiplier, i);
    }

    return bodiesArray;
  }

  /**
   * Creates two separate spiral galaxies colliding. (Almost the same logic as the simulation with 1 galaxy)
   */
  private static Body[] createCollidingGalaxies(int numOfBodies, Random random) {
    Body[] bodiesArray = new Body[numOfBodies];

    int bodiesA = numOfBodies / 2;

    double centralMass = (OVERALL_SYSTEM_MASS * 0.9) / 2.0;
    double remainingMass = OVERALL_SYSTEM_MASS * 0.1;
    double avgStarMass = remainingMass / (numOfBodies - 2);

    Vector3 corePositionA = new Vector3(-3 * AU, -1 * AU, 0);
    Vector3 bulkVelocityA = new Vector3(8e3, 2e3, 0);

    Vector3 corePositionB = new Vector3(3 * AU, 1 * AU, 0);
    Vector3 bulkVelocityB = new Vector3(-8e3, -2e3, 0);

    // Anchor A
    bodiesArray[0] = new Body(centralMass, corePositionA, bulkVelocityA);
    // Anchor B
    bodiesArray[bodiesA] = new Body(centralMass, corePositionB, bulkVelocityB);

    int numArms = 2;
    double armOffsetMax = 0.5;
    double rotationFactor = 5.0;
    double spinMultiplier = 1.3;

    double radiusScalar = AU;

    // Galaxy A stars
    for (int i = 1; i < bodiesA; i++) {
      initGalaxyHelper(random, bodiesArray, centralMass, avgStarMass, corePositionA, bulkVelocityA, numArms, armOffsetMax, rotationFactor, radiusScalar,
          spinMultiplier, i);
    }

    // Galaxy B stars
    for (int i = bodiesA + 1; i < numOfBodies; i++) {
      initGalaxyHelper(random, bodiesArray, centralMass, avgStarMass, corePositionB, bulkVelocityB, numArms, armOffsetMax, rotationFactor, radiusScalar,
          spinMultiplier, i);
    }

    return bodiesArray;
  }

  /**
   * Helper method to initialize a single body within a spiral galaxy structure.
   * (See the invocation in code above for more details)
   *
   * @param corePosition   anchor point / center of this galaxy
   * @param bulkVelocity   directional speed vector describing movement of the entire galaxy
   * @param radiusScalar   scale of the galaxy (used for positioning of stars)
   * @param spinMultiplier scalar adjustment for the velocity
   * @param index          location in bodiesArray to save this new Body
   */
  private static void initGalaxyHelper(Random random, Body[] bodiesArray, double centralMass, double avgStarMass, Vector3 corePosition, Vector3 bulkVelocity,
                                       int numArms, double armOffsetMax, double rotationFactor, double radiusScalar, double spinMultiplier, int index) {
    // 1. Assign the star to a specific arm
    int armVal = random.nextInt(numArms);

    // 2. Evaluate distance from center (using absolute Gaussian for dense core)
    double r = Math.abs(random.nextGaussian()) * radiusScalar;

    // 3. Archimedean Spiral (for the Shape)
    // The defining property of an Archimedean spiral is that the angle increases linearly with the radius.
    // We calculate angular position mapping distance to an offset, creating an organic spiral arm.
    double baseAngle = (2.0 * Math.PI / numArms) * armVal;
    double windAngle = rotationFactor * (r / radiusScalar);
    double scatterAngle = random.nextGaussian() * armOffsetMax;

    double angle = baseAngle + windAngle + scatterAngle;

    // 4. Polar to Cartesian Transformation (for the Coordinates)
    // Converts the mathematical spiral polar degrees (r, angle) back into literal 3D space grids (x, y, z).
    double x = r * Math.cos(angle);
    double y = r * Math.sin(angle);
    double z = drawBoundaries ? 0.0 : 0.02 * random.nextGaussian() * AU; // Flat Z axis to resemble a disc

    Vector3 localPosition = new Vector3(x, y, z);
    double distance = localPosition.length();
    if (distance == 0) {
      distance = 1; // Prevent division by zero at a center
    }

    // 5. Keplerian Mechanics (for the Motion)
    // Uses Johannes Kepler's planetary motion formula to calculate the exact velocity required to maintain orbit.
    // v = sqrt((G * M) / r)
    // Objects closer to the center of massive gravity wells must move exponentially faster.
    double orbitalSpeed = Math.sqrt((SIMULATION_G * centralMass) / distance) * spinMultiplier;

    // To orbit, the velocity vector must be perfectly perpendicular to the position vector.
    // In 2D (XY plane), the perpendicular to (x, y) is (-y, x).
    Vector3 tangent = new Vector3(-y, x, 0);
    tangent.normalize();

    // Tangent times orbit speed gives a perfect circular orbit velocity.
    // We add a bit of random variance to make it look organic rather than mechanical.
    Vector3 localVelocity = tangent.times(orbitalSpeed)
        .plus(new Vector3(random.nextGaussian() * 1e3, random.nextGaussian() * 1e3, drawBoundaries ? 0.0 : random.nextGaussian() * 1e2));

    double mass = Math.abs(random.nextGaussian()) * avgStarMass;

    // 6. Combine calculation with passed core location and bulk velocity
    bodiesArray[index] = new Body(mass, localPosition.plus(corePosition), localVelocity.plus(bulkVelocity));
  }

  /**
   * Creates a random cloud of bodies using normal distribution.
   */
  private static Body[] createRandomCloud(int numOfBodies, Random random) {
    Body[] bodiesArray = new Body[numOfBodies];

    for (int i = 0; i < NUMBER_OF_BODIES; i++) {
      Body temp;
      if (!drawBoundaries) {
        temp = new Body(Math.abs(random.nextGaussian()) * (OVERALL_SYSTEM_MASS / 223 * NUMBER_OF_BODIES) / NUMBER_OF_BODIES,
            new Vector3(0.2 * random.nextGaussian() * SECTION_SIZE, 0.2 * random.nextGaussian() * SECTION_SIZE, 0.2 * random.nextGaussian() * SECTION_SIZE),
            new Vector3(0 + (random.nextGaussian() * 5e3 / 22) * NUMBER_OF_BODIES, 0 + (random.nextGaussian() * 5e3 / 22) * NUMBER_OF_BODIES,
                0 + (random.nextGaussian() * 5e3 / 22) * NUMBER_OF_BODIES));
      } else {    // Set z axis to 0 to better draw the boundaries
        temp = new Body(Math.abs(random.nextGaussian()) * (OVERALL_SYSTEM_MASS / 223 * NUMBER_OF_BODIES) / NUMBER_OF_BODIES,
            new Vector3(0.2 * random.nextGaussian() * SECTION_SIZE, 0.2 * random.nextGaussian() * SECTION_SIZE, 0),
            new Vector3(0 + (random.nextGaussian() * 5e3 / 22) * NUMBER_OF_BODIES, 0 + (random.nextGaussian() * 5e3 / 22) * NUMBER_OF_BODIES, 0));
      }
      bodiesArray[i] = temp;
    }

    return bodiesArray;
  }
}
