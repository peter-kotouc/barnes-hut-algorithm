import codedraw.CodeDraw;

/**
 * Main octree structure for the Barnes-Hut algorithm.
 * Holds the root node and bounds of the simulation.
 * <p>
 * Invariant: root is a valid OctNode until deleteOctree() is called.
 * Invariant: mainBoundaryCube represents the simulation boundaries.
 * Invariant: Each body inserted does not change between its insertion and deleteOctree() invocation.
 */
public class Octree {
  private OctNode root;    // Main region, positions outside the boundaries do not exist in the simulation
  private BoundaryCube mainBoundaryCube;

  /**
   * Creates a new empty Octree with the given boundaries.
   *
   * @param boundaryCube the boundary of the simulation space.
   *                     Precondition: boundaryCube != null
   *                     Postcondition: root is initialized as a ParentOctNode.
   */
  public Octree(BoundaryCube boundaryCube) {
    this.mainBoundaryCube = boundaryCube;
    root = new ParentOctNode(null, boundaryCube);
  }

  /**
   * Inserts a body into the octree.
   *
   * @param bodyToInsert the body to insert.
   *                     Precondition: bodyToInsert != null
   * @return true if the body is within bounds and inserted, false otherwise.
   * Postcondition: body is inserted if returning true; tree is unchanged if returning false.
   */
  public boolean insertBody(Body bodyToInsert) {
    if (!mainBoundaryCube.isInBoundary(bodyToInsert)) {
      return false;
    } else {
      root.insertBody(bodyToInsert);
      return true;
    }
  }

  /**
   * Deletes the octree and clears references in all subnodes to null.
   * This ensures quicker garbage collection and smaller peak RAM consumption.
   * <p>
   * Postcondition: root and mainBoundaryCube are null.
   */
  public void deleteOctree() {
    root.deleteAll();
    root = null;
    mainBoundaryCube = null;
  }

  /**
   * Draws all bodies using CodeDraw.
   *
   * @param cd the CodeDraw canvas.
   *           Precondition: cd != null and tree is initialized.
   */
  public void drawBodies(CodeDraw cd) {
    root.drawBodies(cd);
  }

  /**
   * Updates mass and centers of mass for all nodes.
   * Needs to be called before computing forces.
   * <p>
   * Precondition: All bodies have been inserted.
   * Postcondition: Node mass centers are updated.
   */
  public void updateCenters() {
    root.updateCenters();
  }

  /**
   * Draws the boundaries of the octree.
   *
   * @param cd the CodeDraw canvas.
   *           Precondition: cd != null && all bodies in octree have same z axis and 0 z-speed vector
   */
  public void drawBoundaries(CodeDraw cd) {
    root.drawBoundaries(cd);
  }

  /**
   * Calculates the total force exerted on a body.
   * Uses the Barnes-Hut approximation for distant clusters.
   *
   * @param key the body to calculate the force for.
   * @return the total gravitational force vector on the body.
   * Precondition: updateCenters() must be invoked before calling this method and no other bodies can be inserted, else updateCenters() must be reinvoked.
   * Postcondition: Returns the calculated force vector.
   */
  public Vector3 forceExertedOnBody(Body key) {
    Vector3 forceOnBody = new Vector3(); // begin with zeros
    BodyStack stckTemp = new BodyStack();

    root.bodiesInteractingWith(key, stckTemp);

    while (stckTemp.getSize() != 0) {
      if (stckTemp.peek() != key) {
        Vector3 forceToAdd = key.gravitationalForce(stckTemp.poll());
        forceOnBody = forceOnBody.plus(forceToAdd);
      } else {  // Skip calculation for the same body
        stckTemp.poll();
      }
    }

    return forceOnBody;
  }

}
