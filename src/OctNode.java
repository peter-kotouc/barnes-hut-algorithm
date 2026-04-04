import codedraw.CodeDraw;

/**
 * Interface for nodes within the octree.
 * Implementations handle spatial partitioning and calculating mass centers.
 * <p>
 * Invariant: Implementations must respect spatial bounds and maintain the tree structure (assuming once inserted, the bodies do not change).
 */
public interface OctNode {
  /**
   * Inserts a body into the node.
   *
   * @param bodyToInsert the body descending the tree (If drawBoundaries is later called, then all inserted bodies should have same z coordinate).
   *                     Precondition: bodyToInsert != null && bodyToInsert belongs in this boundaryCube
   * @return updated new leaf node (with this.boundaryCube) || new intermediary node (with this.boundaryCube) || null, if this is not leaf node
   * Postcondition: The body is inserted into the structure in a leaf node.
   */
  OctNode insertBody(Body bodyToInsert);

  /**
   * Updates the total mass and center of mass.
   * Required for the Barnes-Hut approximation.
   * <p>
   * Postcondition: This has a variable that e holds a pseudo-body representing exactly the barycenter of this node space OR null, if there are no children.
   */
  void updateCenters();

  /**
   * Returns the center of mass body (barycenter).
   * <p>
   * Precondition: updateCenters() was called right before invoking getCenterBody()
   *
   * @return the Body representing the total mass, or null if the node is empty.
   */
  Body getCenterBody(); // NOTE: This can be also something else then public in implementations, but it has proper server-client contract

  /**
   * Removes references inside this node and all sub-nodes to help the garbage collector.
   * <p>
   * Postcondition: All class fields (subNodes, center, boundaryCube...) are set to null. If there are subnodes, deleteAll() is also invoked on them.
   */
  void deleteAll();

  /**
   * Draws all bodies contained in this node.
   *
   * @param cd the CodeDraw canvas.
   *           Precondition: cd != null
   */
  void drawBodies(CodeDraw cd);

  /**
   * Draws the boundary outlines for all the nodes that have bodies in this node.
   * (For better representation of boundaries, all bodies in this must be at exactly the same Z plane and have no Z speed).
   *
   * @param cd the CodeDraw canvas.
   *           Precondition: cd != null && all bodies recursively in this have same z axis and 0 z-speed vector
   */
  void drawBoundaries(CodeDraw cd);

  /**
   * Adds top barycenter of nodes to the stack if they are far enough away to be treated as a single mass block,
   * or recurses into children and invokes bodiesInteractingWith(...) for each of the children until leaf nodes are reached.
   *
   * @param key      the body that forces are acting on.
   *                 Precondition: key != null
   * @param stckBody the stack to add interacting bodies to.
   *                 Precondition: The updateCenters() recursion has fully executed and after that the structure did not change.
   *                 Precondition: stckBody != null
   *                 Postcondition: Nodes (barycenter bodies) meeting the Barnes-Hut condition are added to the stack.
   */
  void bodiesInteractingWith(Body key, BodyStack stckBody);
}
