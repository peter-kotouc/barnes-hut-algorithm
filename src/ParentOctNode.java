import codedraw.CodeDraw;

/**
 * Represents an internal branching node within the octree structure.
 * As a proper subtype of OctNode, it acts as a composite holding up to eight partitioned sub-nodes,
 * managing recursive distribution of bodies and computational aggregation of its sub-regions mass centers.
 * <p>
 * Invariant: subNodes always contains exactly 8 initialized OctNode elements (NullOctNode by default).
 * Invariant: The computed center body accurately acts as the combined center of mass for all sub-nodes.
 */
public class ParentOctNode implements OctNode {
  private final OctNode[] subNodes = new OctNode[8];
  private BoundaryCube[] subNodesBoundaries;
  private BoundaryCube boundaryCube;
  private Body center;

  /**
   * Creates a branching octree node dynamically dividing its given space into 8 sub-regions.
   *
   * @param body         intentionally ignored.
   * @param boundaryCube the full boundary containing the 8 sub-cubes.
   *                     Precondition: boundaryCube != null
   *                     Postcondition: All 8 subnodes are securely initialized as empty NullOctNodes.
   */
  public ParentOctNode(Body body, BoundaryCube boundaryCube) {
    this.boundaryCube = boundaryCube;
    subNodesBoundaries = boundaryCube.subregionsBoundaries();

    for (int i = 0; i < subNodesBoundaries.length; i++) {
      subNodes[i] = new NullOctNode(null, subNodesBoundaries[i]); // Initializes all subnodes as null nodes
    }
  }

  /**
   * Finds which of the 8 sub-nodes spatially contains the body and hands it downwards.
   * No two bodies can be in the same exact dimensional BodyOctNode.
   *
   * @param bodyToInsert the body descending the tree.
   *                     Precondition: bodyToInsert != null && bodyToInsert belongs in this boundaryCube
   * @return null (since this is not a leaf node)
   * Postcondition: The body is inserted into the structure in a leaf node.
   */
  @Override
  public OctNode insertBody(Body bodyToInsert) {
    for (int i = 0; i < subNodesBoundaries.length; i++) {
      if (subNodesBoundaries[i].isInBoundary(bodyToInsert)) {
        OctNode returnNode = subNodes[i].insertBody(bodyToInsert);
        if (returnNode != null) {
          subNodes[i] = returnNode;
        }
        break;
      }
    }

    return null;
  }

  /**
   * Recursively aggregates the mass and mathematically centers the coordinate balance of all sub-nodes.
   * <p>
   * Postcondition: The 'center' variable holds a pseudo-body representing exactly the barycenter of this node space.
   */
  @Override
  public void updateCenters() {
    for (OctNode subNode : subNodes) {
      subNode.updateCenters();
    }

    double centerMass = 0d;
    double centerX = 0d;
    double centerY = 0d;
    double centerZ = 0d;

    for (OctNode subNode : subNodes) {
      Body currentSubCenterBody = subNode.getCenterBody(); // updateCenters() was invoked few lines above
      if (currentSubCenterBody != null) {
        centerMass += currentSubCenterBody.getMass();
        centerX += currentSubCenterBody.getMassCenter().getX() * currentSubCenterBody.getMass();
        centerY += currentSubCenterBody.getMassCenter().getY() * currentSubCenterBody.getMass();
        centerZ += currentSubCenterBody.getMassCenter().getZ() * currentSubCenterBody.getMass();
      }
    }

    // mean based on mass
    centerX /= centerMass;
    centerY /= centerMass;
    centerZ /= centerMass;

    center = new Body(centerMass, new Vector3(centerX, centerY, centerZ), new Vector3());   // Direction doesnt matter
  }


  /**
   * Returns the aggregated pseudo-body barycenter computed via updateCenters().
   * <p>
   * Precondition: updateCenters() was called right before invoking getCenterBody()
   *
   * @return the mathematical gravity center of this parent node.
   */
  @Override
  public Body getCenterBody() {
    return center;
  }

  /**
   * Completely tears down references for all 8 subnodes to help Java Garbage Collection.
   * <p>
   * Postcondition: references are successfully cleared to null including all the subnodes.
   */
  @Override
  public void deleteAll() {
    for (OctNode subNode : subNodes) {
      subNode.deleteAll();
    }
    //boundary.deleteBoundary();
    boundaryCube = null;    // Set to null, only for garbage collection, boundaries still persist and can be reused in next iteration
    subNodesBoundaries = null;
    center = null;
  }

  /**
   * Draws all bodies in this node by recursively sending drawBodies(...) invocation request to all the subnodes.
   *
   * @param cd the CodeDraw canvas.
   *           Precondition: cd != null
   */
  @Override
  public void drawBodies(CodeDraw cd) {
    for (OctNode subNode : subNodes) {
      subNode.drawBodies(cd); // recursively draw all BodyOctNode bodies
    }
  }

  /**
   * Draws the boundary outlines for all the nodes that have bodies in this node. This is done by recursive drawBoundaries(...)
   * invocations until all the full leaf nodes are drawn.
   * (For better representation of boundaries, all bodies in this must be at exactly the same Z plane and have no Z speed).
   *
   * @param cd the CodeDraw canvas.
   *           Precondition: cd != null && all bodies recursively in this have same z axis and 0 z-speed vector
   */
  @Override
  public void drawBoundaries(CodeDraw cd) {
    for (OctNode subNode : subNodes) {
      subNode.drawBoundaries(cd); // Recursively draw all BodyOctNodes boundaries
    }
  }

  /**
   * Adds top barycenter of nodes to the stack if they are far enough away to be treated as a single mass block,
   * or recurses into children and invokes bodiesInteractingWith(...) for each of the children until leaf nodes are reached.
   *
   * @param key      the body that forces are acting on.
   *                 Precondition: key != null
   * @param stckBody the stack to add interacting bodies to.
   *                 Precondition: stckBody != null
   *                 Precondition: The updateCenters() recursion has fully executed.
   *                 Postcondition: Nodes (barycenter bodies) meeting the Barnes-Hut condition are added to the stack.
   */
  @Override
  public void bodiesInteractingWith(Body key, BodyStack stckBody) {
    double r = key.distanceTo(new Body(3d, this.boundaryCube.getMidPoint(), new Vector3())); // mass and direction does not matter

    if (this.boundaryCube.getD() / r < Simulation.T) {
      stckBody.push(this.center);
    } else {
      for (OctNode subNode : subNodes) {
        subNode.bodiesInteractingWith(key, stckBody);
      }
    }
  }
}
