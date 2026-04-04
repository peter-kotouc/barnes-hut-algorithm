import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single node (part) within the linked-list implementation of the BodyStack.
 * <p>
 * Invariant: 'body' stores the immutable body reference
 * 'previous' points to the preceding node in the stack structure or null, if this is the first node.
 */
@Getter
public class BodyStackPart {

  @Setter
  private BodyStackPart previous;     // Reference of the last body before this one
  private final Body body;

  /**
   * Creates a new node for the stack containing a specific Body and a reference to the previous node.
   *
   * @param newBody  the body to store in this node.
   *                 Precondition: newBody != null
   * @param previous the reference to the node immediately below this one in the stack || null
   *                 Postcondition: The node is initialized with the newBody as current body and pointer/no pointer to the previous body stack
   */
  public BodyStackPart(Body newBody, BodyStackPart previous) {
    this.body = newBody;
    this.previous = previous;
  }
}
