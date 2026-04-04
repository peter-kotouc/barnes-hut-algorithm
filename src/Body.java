import codedraw.CodeDraw;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a physical celestial body like a star, planet, or asteroid.
 * This class holds the physical state (mass, position, movement) of the body and
 * provides physics calculations such as gravitational interaction and movement calculation.
 */
@NoArgsConstructor
public class Body {
  @Getter
  private double mass;
  @Getter
  private Vector3 massCenter; // position of the mass center.
  private Vector3 currentMovement;

  /**
   * Creates a new body with the specified mass, position, and movement.
   *
   * @param mass            the mass of the body in kilograms.
   * @param massCenter      the initial position vector.
   *                        Precondition: massCenter != null
   * @param currentMovement the initial movement vector.
   *                        Precondition: currentMovement != null
   */
  public Body(double mass, Vector3 massCenter, Vector3 currentMovement) {
    this.mass = mass;
    this.massCenter = massCenter;
    this.currentMovement = currentMovement;
  }

  /**
   * Calculates the distance (=units of the simulation) between the mass centers of this body and another body.
   *
   * @param b the other body.
   *          Precondition: b != null && this and b need to have same units for massCenters
   * @return the Euclidean distance between the two bodies.
   */
  public double distanceTo(Body b) {
    return massCenter.distanceTo(b.massCenter);
  }

  /**
   * Calculates the gravitational force exerted by the given body 'b' on this body.
   * Uses Newton's law of universal gravitation: F = G*(m1*m2)/(r^2).
   *
   * @param b other body used to calculate exerted force on this body.
   *          Precondition: b != null
   * @return the force vector exerted on this body.
   */
  public Vector3 gravitationalForce(Body b) {
    Vector3 direction = b.massCenter.minus(massCenter);
    double distance = direction.length();
    direction.normalize();
    double force = Simulation.SIMULATION_G * mass * b.mass / (distance * distance);

    return direction.times(force);
  }

  /**
   * Updates the position and movement of this body based on the exerted force.
   *
   * @param force the total net force acting on this body.
   *              Precondition: force != null
   *              Postcondition: massCenter and currentMovement are updated to their new state.
   */
  public void move(Vector3 force) {
    // Force, Mass, Acceleration
    // F = m*a -> a = F/m
    // (massCenter + (force / mass)) + currentMovement
    Vector3 newPosition = currentMovement.plus(massCenter.plus(force.times(1 / mass)));

    // New minus old position.
    Vector3 newMovement = newPosition.minus(massCenter);

    // Update body state
    massCenter = newPosition;
    currentMovement = newMovement;

  }

  /**
   * Creates a new body that is the merged result of a collision between this body and another.
   * Aggregates mass and applies conservation of momentum.
   * <p>
   * Not used yet... Maybe the implementation is wrong.
   *
   * @param b the second body involved in the collision.
   *          Precondition: b != null
   * @return a new combined Body representing the merged mass and momentum.
   */
  public Body merge(Body b) {
    Body result = new Body();
    result.mass = mass + b.mass;

    result.massCenter = massCenter.times(mass).plus(
        b.massCenter.times(b.mass)).times(
        1 / result.mass);

    result.currentMovement = currentMovement.times(
        mass).plus(
        b.currentMovement.times(
            b.mass)).times(
        1.0 / result.mass);

    return result;
  }

  /**
   * Draws the body to the specified canvas as a filled circle.
   * The radius and color correspond directly to the body's mass.
   *
   * @param cd the CodeDraw canvas to draw on.
   *           Precondition: cd != null
   */
  public void draw(CodeDraw cd) {
    cd.setColor(SpaceDraw.massToColor(mass));
    massCenter.drawAsFilledCircle(cd, SpaceDraw.massToRadius(mass));
  }

  /**
   * Returns a string representation of this body's state elements.
   *
   * @return formatted string including mass, mass center, and current movement.
   */
  public String toString() {
    return String.format("%.3e kg, position: ", mass) + massCenter.toString() + " + m, movement: " + currentMovement.toString() + " m/s";
  }
}

