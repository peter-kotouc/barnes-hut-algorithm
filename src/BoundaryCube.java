import codedraw.CodeDraw;
import lombok.Getter;

import java.awt.Color;

/**
 * Represents a bounding cube in 3D space used by the Octree.
 * <p>
 * Invariant: minPoint and maxPoint define the opposite 'extrema' corners of the bounding box.
 * The property 'd' defines the length of the side of the cube (calculated in the constructor).
 * The basic properties are final. The subBoundaries array is created only when subregionsBoundaries() is called and caches for reuse (lazy-loaded).
 */
public class BoundaryCube {
  // Even though these are vectors, they represent coordinates in this context
  private final Vector3 minPoint;
  private final Vector3 maxPoint;

  @Getter
  private Vector3 midPoint; // Is never changed, but cannot be final because it is calculated outside the constructor
  private BoundaryCube[] subBoundaries; // Lazy loaded on invocation of subregionsBoundaries()

  @Getter
  private final double d;   // Length of one side

  /**
   * Initializes a boundary box defined by its minimum and maximum coordinates.
   *
   * @param minPoint the vector representing the minimum spatial coordinates.
   * @param maxPoint the vector representing the maximum spatial coordinates.
   *                 Precondition: minPoint != null && correct minPoint that can be used for cube that has maxPoint
   *                 Precondition: maxPoint != null && correct maxPoint that can be used for cube that has minPoint
   *                 Postcondition: A Boundary is created and the midPoint and side length 'd' are established.
   */
  public BoundaryCube(Vector3 minPoint, Vector3 maxPoint) {
    this.minPoint = minPoint;
    this.maxPoint = maxPoint;
    this.calculateAndSetMidPoint();

    d = maxPoint.getX() - minPoint.getX();      // All sides are the same, so any coordinate works
  }

  /**
   * Checks if the body is inside the boundary.
   *
   * @param bodyToCheck the body to check.
   * @return true if the body is in this boundary, false otherwise.
   * Precondition: bodyToCheck != null
   */
  public boolean isInBoundary(Body bodyToCheck) {
    Vector3 centerOfBody = bodyToCheck.getMassCenter();

    // Checks if the x is ok
    if (minPoint.getX() <= centerOfBody.getX() && maxPoint.getX() >= centerOfBody.getX()) {
      // Checks y
      if (minPoint.getY() <= centerOfBody.getY() && maxPoint.getY() >= centerOfBody.getY()) {
        // Checks z
        return minPoint.getZ() <= centerOfBody.getZ() && maxPoint.getZ() >= centerOfBody.getZ();
      }
    }

    return false;
  }

  /**
   * Calculates the center point of this boundary box.
   * Works for non-cubes as well.
   * <p>
   * Precondition: minPoint != null && maxPoint != null
   * Postcondition: midPoint is set to the center.
   */
  private void calculateAndSetMidPoint() {
    double midX = minPoint.getX() + ((maxPoint.getX() - minPoint.getX()) * 0.5d);
    double midY = minPoint.getY() + ((maxPoint.getY() - minPoint.getY()) * 0.5d);
    double midZ = minPoint.getZ() + ((maxPoint.getZ() - minPoint.getZ()) * 0.5d);

    midPoint = new Vector3(midX, midY, midZ);
  }

  // /**
  //  * Sets all objects inside the boundary to null for explicit garbage collection processing.
  //  * <p>
  //  * Postcondition: minPoint, maxPoint, and midPoint references are released to null.
  //  */
  // public void deleteBoundary() {
  //   this.minPoint = null;
  //   this.maxPoint = null;
  //   this.midPoint = null;
  // }

  /**
   * Draws a square representing this boundary cube using CodeDraw.
   *
   * @param cd the CodeDraw canvas.
   *           Precondition: cd != null
   */
  public void drawBoundary(CodeDraw cd) {
    double x = cd.getWidth() * (minPoint.getX() + Simulation.SECTION_SIZE / 2) / Simulation.SECTION_SIZE;
    double y = cd.getWidth() * (minPoint.getY() + Simulation.SECTION_SIZE / 2) / Simulation.SECTION_SIZE;
    double xMax = cd.getWidth() * (maxPoint.getX() + Simulation.SECTION_SIZE / 2) / Simulation.SECTION_SIZE;
    double sideLength = Math.abs(xMax - x);

    cd.setColor(Color.WHITE);
    cd.setLineWidth(1.5);
    cd.drawSquare(x, y, sideLength);
  }

  /**
   * Splits this boundary into 8 equal sub-regions (octants).
   * Works for non-cubes as well.
   * <p>
   * Spatial Layout Mapping (assuming Y goes down, Z goes 'into' the screen):
   * <p>
   * Front Face (minZ to midZ)         Rear Face (midZ to maxZ) -- Suffix '2'
   * +---------+---------+             +---------+---------+
   * |         |         |             |         |         |
   * | NW (0)  | NE (2)  |             | NW2 (1) | NE2 (3) |  (minY to midY)
   * |         |         |             |         |         |
   * +---------+---------+             +---------+---------+
   * |         |         |             |         |         |
   * | SW (4)  | SE (6)  |             | SW2 (5) | SE2 (7) |  (midY to maxY)
   * |         |         |             |         |         |
   * +---------+---------+             +---------+---------+
   * (minX - midX) (midX - maxX)       (minX - midX) (midX - maxX)
   *
   * @return array of 8 sub-boundaries. Order: NW, NW2, NE, NE2, SW, SW2, SE, SE2
   * Precondition: min/mid/maxPoint are assigned.
   * Postcondition: subBoundaries array is created and cached for later use.
   */
  public BoundaryCube[] subregionsBoundaries() {

    if (subBoundaries == null) {

      BoundaryCube[] boundaryCubeArray = new BoundaryCube[8];
      // Order: NW, NW2, NE, NE2, SW, SW2, SE, SE2

      double maxX = maxPoint.getX();
      double maxY = maxPoint.getY();
      double maxZ = maxPoint.getZ();

      double minX = minPoint.getX();
      double minY = minPoint.getY();
      double minZ = minPoint.getZ();

      double midX = midPoint.getX();
      double midY = midPoint.getY();
      double midZ = midPoint.getZ();

      boundaryCubeArray[0] = new BoundaryCube(
          new Vector3(minX, minY, minZ),
          new Vector3(midX, midY, midZ)); //NW

      boundaryCubeArray[1] = new BoundaryCube(
          new Vector3(minX, minY, midZ),
          new Vector3(midX, midY, maxZ)); //NW2

      boundaryCubeArray[2] = new BoundaryCube(
          new Vector3(midX, minY, minZ),
          new Vector3(maxX, midY, midZ)); //NE

      boundaryCubeArray[3] = new BoundaryCube(
          new Vector3(midX, minY, midZ),
          new Vector3(maxX, midY, maxZ)); //NE2

      boundaryCubeArray[4] = new BoundaryCube(
          new Vector3(minX, midY, minZ),
          new Vector3(midX, maxY, midZ)); //SW

      boundaryCubeArray[5] = new BoundaryCube(
          new Vector3(minX, midY, midZ),
          new Vector3(midX, maxY, maxZ)); //SW2

      boundaryCubeArray[6] = new BoundaryCube(
          new Vector3(midX, midY, minZ),
          new Vector3(maxX, maxY, midZ)); //SE

      boundaryCubeArray[7] = new BoundaryCube(
          new Vector3(midX, midY, midZ),
          new Vector3(maxX, maxY, maxZ)); //SE2

      subBoundaries = boundaryCubeArray;
    }


    return subBoundaries;
  }
}
