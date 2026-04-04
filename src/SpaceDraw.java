import java.awt.Color;

/**
 * A utility class providing static methods for the visual representation of space bodies.
 * <p>
 * Invariant: The class maintains no state and only provides deterministic visual
 * transformations from physical properties (mass/kelvin) to graphics.
 */
public class SpaceDraw {

  /**
   * Returns the approximate radius of a celestial body with the specified mass.
   * For main-sequence stars (like our Sun), the radius of the star scales proportionally
   * to the square root of its mass relative to the sun (R ~ M^0.5).
   *
   * @param mass the mass of the body in kg.
   *             Precondition: mass >= 0.0d
   * @return the computed radius of the body as a double. >= 0.0d
   */
  public static double massToRadius(double mass) {
    return Simulation.SUN_RADIUS * (Math.pow(mass / Simulation.SUN_MASS, 0.5));
  }

  /**
   * Returns the approximate color of a celestial body with the specified mass.
   * This uses a simplified linear mapping assuming the Sun surface temperature is roughly 5,500 Kelvin.
   * It scales the temperature directly by mass to simulate how heavier main-sequence stars burn hotter (blue)
   * and lighter stars burn cooler (red). For mass lower than 1/10 of the sun mass, the color is fixed as LIGHT_GRAY.
   *
   * @param mass the mass of the body in kg.
   *             Precondition: mass >= 0
   * @return a mapped java.awt.Color approximating a color of the body/star based on mass.
   */
  public static Color massToColor(double mass) {
    Color color;
    if (mass < Simulation.SUN_MASS / 10) {
      // Not a star-like body below this mass
      color = Color.LIGHT_GRAY;
    } else {
      // Assume a main sequence star
      color = SpaceDraw.kelvinToColor((int) (5500 * mass / Simulation.SUN_MASS));
    }

    return color;
  }

  /**
   * Returns the approximate display color for a specific star temperature in kelvin.
   * <p>
   * This calculation utilizes Tanner Helland's algorithm (based on Mitchell Charity's black-body data),
   * which uses a curve-fitting polynomial regression to convert Kelvin temperatures directly into
   * RGB values without computationally heavy Physics conversions into the CIE XYZ color space.
   * <p>
   * Further readings: https://tannerhelland.com/2012/09/18/convert-temperature-rgb-algorithm-code.html
   *
   * @param kelvin the approximate temperature of the body in Kelvin points.
   *               Precondition: kelvin >= 0
   * @return a java.awt.Color calculated from scaled RGB wavelength limits.
   * Postcondition: The returned Color object containing valid RGB values [0-255].
   */
  private static Color kelvinToColor(int kelvin) {

    double k = kelvin / 100D;
    double red = k <= 66 ? 255 : 329.698727446 * Math.pow(k - 60, -0.1332047592);
    double green = k <= 66 ? 99.4708025861 * Math.log(k) - 161.1195681661 : 288.1221695283 * Math.pow(k - 60, -0.0755148492);
    double blue = k >= 66 ? 255 : (k <= 19 ? 0 : 138.5177312231 * Math.log(k - 10) - 305.0447927307);

    return new Color(
        limitAndDarken(red, kelvin),
        limitAndDarken(green, kelvin),
        limitAndDarken(blue, kelvin)
    );
  }

  /**
   * Transforms a raw color channel value based on temperature to simulate
   * physical luminosity. This method implements an infrared cutoff for
   * temperatures below 373K and applies a linear dimming for
   * low-temperatures. All colors above 255 are rounded to 255 because of RGB.
   *
   * @param color  the raw calculated, potentially unbounded color channel value.
   * @param kelvin the temperature in Kelvin characterizing the body.
   *               Precondition: kelvin >= 0
   * @return the constrained color channel representation mapped as a valid RGB int.
   * Postcondition: Output is in [0-255]
   */
  private static int limitAndDarken(double color, int kelvin) {
    int kelvinNorm = kelvin - 373;

    if (color < 0 || kelvinNorm < 0) {
      return 0;
    } else if (color > 255) {
      return 255;
    } else if (kelvinNorm < 500) {
      return (int) ((color / 256D) * (kelvinNorm / 500D) * 256);
    } else {
      return (int) color;
    }
  }
}
