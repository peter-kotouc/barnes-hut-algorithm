import codedraw.CodeDraw;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This class represents a vector in 3D space. (It can be also used to represent 3D coordinate)
 * It is assumed, that x, y, z have the same units, else the calculations could be possibly not mathematically sound.
 * <p>
 * Invariant: The vector is defined by its three components (x, y, z) in
 * Euclidean space.
 * The state transitions of this vector maintain mathematically sound
 * floating-point coordinates. Specifically, x, y, and z are never NaN.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Vector3 {
  private double x;
  private double y;
  private double z;

  /**
   * Creates a new Vector3 where all coordinates are set to the given value.
   *
   * @param setAll the value to be set for all three components (x, y, z).
   *               Precondition: setAll is not NaN
   */
  public Vector3(double setAll) {
    this.x = this.y = this.z = setAll;
  }

  /**
   * Returns the sum of this vector and vector 'v'.
   *
   * @param v the vector to be added to this vector.
   *          Precondition: v != null && both vectors use the same units
   * @return a new Vector3 representing the vector sum.
   * Postcondition: Original vectors remain unchanged.
   */
  public Vector3 plus(Vector3 v) {
    return new Vector3(x + v.x, y + v.y, z + v.z);
  }

  /**
   * Returns the product of this vector and 'd'.
   *
   * @param d the scalar value to multiply with this vector.
   *          Precondition: d is not NaN
   * @return a new Vector3 scaled by 'd'.
   * Postcondition: Original vector remains unchanged.
   */
  public Vector3 times(double d) {
    return new Vector3(x * d, y * d, z * d);
  }

  /**
   * Returns the difference of this vector and vector 'v'.
   *
   * @param v the vector to be subtracted from this vector.
   *          Precondition: v != null
   * @return a new Vector3 representing the vector difference.
   * Postcondition: Original vectors remain unchanged.
   */
  public Vector3 minus(Vector3 v) {
    return plus(v.times(-1));
  }

  /**
   * Returns the Euclidean distance of this vector
   * to the specified vector 'v'.
   *
   * @param v the vector to which the distance is calculated.
   *          Precondition: v != null && both vectors need to represent position and use the same units for all the dimensions.
   * @return the Euclidean distance as a double (unit is the same, as the unit used for the vectors).
   */
  public double distanceTo(Vector3 v) {
    // sqrt(dx^2 + dy^2 + dz^2)
    Vector3 result = this.minus(v);

    return result.length();
  }

  /**
   * Returns the length (norm) of this vector.
   *
   * @return the Euclidean length of this vector.
   */
  public double length() {
    return Math.sqrt(x * x + y * y + z * z); // Difference between this vector and 0 vector
  }

  /**
   * Normalizes this vector: changes the length of this vector such that it
   * becomes 1 (or very close to 1, since we use double).
   * The direction and orientation of the vector is not affected.
   * <p>
   * Precondition: length() > 0 (The vector must not be the zero vector [0, 0, 0])
   * Postcondition: the length of this vector becomes (almost) exactly 1.0d. The original
   * orientation is conserved.
   */
  public void normalize() {
    double length = length();

    x /= length;
    y /= length;
    z /= length;
  }

  /**
   * Draws a filled circle with a specified radius centered at the (x,y)
   * coordinates of this vector
   * in the canvas associated with 'cd'. The z-coordinate is not used.
   * The x,y are mapped accordingly based on global SECTION_SIZE.
   * The color of the circle is not set by this method,
   * therefore the caller can specify the color beforehand by using cd.setColor(...).
   *
   * @param cd     the CodeDraw canvas on which to draw the circle.
   * @param radius the radius of the circle to be drawn.
   *               Precondition: this represents a coordinate
   *               Precondition: cd != null
   *               Precondition: radius >= 0
   *               Postcondition: A filled circle is drawn on the CodeDraw canvas at the mapped
   *               (x,y). Vector state remains unchanged.
   */
  public void drawAsFilledCircle(CodeDraw cd, double radius) {
    // Section size is size of 1/2 of the edge of the initial drawing board ... x=0,y=0 is exactly in the middle of the drawing board
    double x = cd.getWidth() * (this.x + Simulation.SECTION_SIZE) / (Simulation.SECTION_SIZE * 2);
    double y = cd.getWidth() * (this.y + Simulation.SECTION_SIZE) / (Simulation.SECTION_SIZE * 2);
    radius = cd.getWidth() * radius / Simulation.SECTION_SIZE;

    cd.fillCircle(x, y, Math.max(radius, 1.5d));
  }

  /**
   * Returns the coordinates of this vector in brackets as a string
   * in the form "[x,y,z]", e.g., "[1.48E11,0.0,0.0]".
   *
   * @return a formatted string representation of the vector.
   */
  public String toString() {
    return String.format("[%.2e, %.2e, %.2e]", x, y, z);
  }
}
