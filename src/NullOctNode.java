import codedraw.CodeDraw;

/**
 * Represents an empty leaf node in the octree that contains no bodies.
 * As a proper subtype of OctNode, it implements the Null Object Pattern
 * to prevent NullPointerExceptions during recursive descents, handling calls safely by doing nothing.
 * <p>
 * Invariant: This node natively contains no bodies and generates no force.
 */
public class NullOctNode implements OctNode {
  private BoundaryCube boundaryCube;

  /**
   * Generates a node acting as an empty placeholder.
   *
   * @param body         intentionally ignored.
   * @param boundaryCube the spatial boundary for this empty region.
   */
  public NullOctNode(Body body, BoundaryCube boundaryCube) {
    this.boundaryCube = boundaryCube;
  }

  /**
   * Upgrades this empty node into a BodyOctNode.
   * The replacement occurs in the ParentOctNode.
   *
   * @param bodyToInsert the body descending the tree.
   *                     Precondition: bodyToInsert != null && bodyToInsert belongs in this boundaryCube
   * @return updated new leaf node (with this.boundaryCube)
   * Postcondition: The body is inserted into the structure in a leaf node.
   */
  @Override
  public OctNode insertBody(Body bodyToInsert) {
    return new BodyOctNode(bodyToInsert, this.boundaryCube);
  }


  /**
   * Does nothing since there is no mass to calculate.
   */
  @Override
  public void updateCenters() {
  }

  /**
   * Always returns null to indicate no center of mass exists here.
   * <p>
   * Precondition: No precondition, since it always returns null
   *
   * @return null.
   */
  @Override
  public Body getCenterBody() {
    return null;
  }

  /**
   * Clears the boundary reference to help the garbage collector.
   * <p>
   * Postcondition: boundaryCube is null.
   */
  @Override
  public void deleteAll() {
    boundaryCube = null;    // set to null, only for garbage collection, boundaries still persist and can be reused in next iteration
    //this.boundary.deleteBoundary();
  }

  /**
   * Does nothing since there are no bodies to draw.
   *
   * @param cd the CodeDraw canvas.
   */
  @Override
  public void drawBodies(CodeDraw cd) {
  }

  /**
   * Draws nothing since this is an empty node.
   *
   * @param cd the CodeDraw canvas.
   */
  @Override
  public void drawBoundaries(CodeDraw cd) {
  }

  /**
   * Does nothing because there are no bodies to interact with.
   *
   * @param key      the body requesting interactions.
   * @param stckBody the stack aggregating bodies.
   */
  @Override
  public void bodiesInteractingWith(Body key, BodyStack stckBody) {
  }
}
