import codedraw.CodeDraw;

/**
 * Represents a leaf node in the octree containing exactly one celestial body.
 * As a proper subtype of OctNode, it handles base interaction methods by returning its solitary body,
 * effectively terminating the recursive descent for a single (filled) region.
 * <p>
 * Invariant: body != null (until deleteAll is called).
 * Invariant: boundaryCube encapsulates the spatial region belonging to this single body.
 */
public class BodyOctNode implements OctNode {
  private Body body;
  private BoundaryCube boundaryCube;

  /**
   * Initializes a new BodyOctNode with a body and a boundary cube.
   *
   * @param body         the celestial body held in this node.
   * @param boundaryCube the boundary cube for this node.
   */
  public BodyOctNode(Body body, BoundaryCube boundaryCube) {
    this.body = body;
    this.boundaryCube = boundaryCube;
  }

  /**
   * Inserts a new body into this node's space by upgrading it to a ParentOctNode.
   *
   * @param bodyToInsert the body descending the tree.
   *                     Precondition: bodyToInsert != null && bodyToInsert belongs in this boundaryCube
   * @return the newly created ParentOctNode (with this.boundaryCube) with the inserted body in a leaf node.
   * Postcondition: The body is inserted into the structure in a leaf node.
   */
  @Override
  public OctNode insertBody(Body bodyToInsert) {
    OctNode out = new ParentOctNode(null, boundaryCube);
    out.insertBody(this.body);
    out.insertBody(bodyToInsert);
    return out;
  }

  /**
   * Does nothing since the barycenter is already defined by the single body.
   */
  @Override
  public void updateCenters() {
  }

  /**
   * Gets the body contained within this leaf node.
   * <p>
   * Precondition: updateCenters() was called right before invoking getCenterBody()
   *
   * @return the body inside this node.
   */
  @Override
  public Body getCenterBody() {
    return body;
  }

  /**
   * Clears references so this node's elements can be garbage collected.
   * Boundaries persist elsewhere but the local reference is nullified.
   * <p>
   * Postcondition: boundaryCube and body are null.
   */
  @Override
  public void deleteAll() {
    //this.boundary.deleteBoundary();
    boundaryCube = null;    // set to null, only for garbage collection, boundaries still persist and can be reused in next iteration
    this.body = null;
  }

  /**
   * Draws the body inside this node.
   *
   * @param cd the CodeDraw canvas.
   *           Precondition: cd != null
   */
  @Override
  public void drawBodies(CodeDraw cd) {
    body.draw(cd);
  }

  /**
   * Draws the boundary for this node.
   *
   * @param cd the CodeDraw canvas.
   *           Precondition: cd != null
   */
  @Override
  public void drawBoundaries(CodeDraw cd) {
    boundaryCube.drawBoundary(cd);
  }

  /**
   * Adds the body in this node to the stack for force calculation.
   *
   * @param key      the body forces are calculated for (unused in this implementation).
   * @param stckBody the stack to add this node's body to.
   *                 Precondition: stckBody != null
   *                 Postcondition: This node's body is pushed to the stack.
   */
  @Override
  public void bodiesInteractingWith(Body key, BodyStack stckBody) {
    stckBody.push(body);
  }
}
