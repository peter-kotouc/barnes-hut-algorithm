import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A Last-In-First-Out (LIFO) stack data structure specifically designed for Body objects.
 * <p>
 * Invariant: The 'size' properly reflects the number of active elements in the stack.
 * The 'size' is always >= 0.
 * The 'last' reference always points to the top of the stack, or null if empty.
 */
@NoArgsConstructor
public class BodyStack {
  @Getter
  private int size = 0;
  private BodyStackPart last = null;

  /**
   * Pushes a new Body onto the top of the stack.
   *
   * @param bodyToAdd the body object to add to the stack.
   *                  Precondition: bodyToAdd != null
   *                  Postcondition: The stack size is increased by 1 and the added body becomes the new 'last' element.
   */
  public void push(Body bodyToAdd) {
    last = new BodyStackPart(bodyToAdd, last);
    size++;
  }

  /**
   * Removes and returns the Body at the top of the stack.
   *
   * @return the Body that was most recently added, or null if the stack is completely empty.
   * Postcondition: If the stack was not empty, the size is decreased by 1, the top element
   * is removed, and the previous element becomes the new 'last'.
   */
  public Body poll() {
    if (size == 0) {
      return null;
    }

    Body lastOut = this.last.getBody();

    BodyStackPart oldLast = this.last;
    last = oldLast.getPrevious();

    oldLast.setPrevious(null);      // Deleting reference manually for faster garbage collection

    size--;
    return lastOut;
  }

  /**
   * Returns the Body at the top of the stack without removing it.
   *
   * @return the current Body at the top of the stack, or null if empty.
   * Postcondition: The stack's state (size and references) is unchanged.
   */
  public Body peek() {
    if (last == null) {
      return null;
    }

    return last.getBody();
  }
}
