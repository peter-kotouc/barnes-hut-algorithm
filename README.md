# Barnes-Hut 3D N-Body Simulation

## Preview

|        5,000 Bodies (With Boundaries)        |           10,000 Bodies (No Boundaries)           |
| :------------------------------------------: | :-----------------------------------------------: |
|     ![5k with Boundaries](assets/5k.gif)     |     ![10k without Boundaries](assets/10k.gif)     |
|      **Single Galaxy (10,000 Bodies)**       |      **Colliding Galaxies (20,000 Bodies)**       |
| ![Single Galaxy](assets/singleGalaxy10k.gif) | ![Colliding Galaxies](assets/doubleGalaxy20k.gif) |

An efficient, fully functional 3D N-Body simulation written in Java, utilizing the **Barnes-Hut algorithm** and an
internally implemented **Octree** data structure to accelerate gravitational force calculations.

> This project was originally created in 2022 as part of the **Introduction to Programming 2** course at TU Wien, since
> then this project was improved by adding more formal pre- and postconditions in the comments and improving the overall
> code structure. I have also added simulation of galaxies.
>
> Later I became a tutor for the same course and other courses at TU Wien to help guide other students.
> Special thanks to professor Franz Puntigam for his expertise and allowing me to work at the university for this course.

## Features

- **Efficient N-Body Simulation:** By utilizing the Barnes-Hut algorithm with an Octree, the computational complexity is
  reduced from $O(N^2)$ to $O(N*\log N)$, enabling the real-time simulation of thousands of bodies (currently configured
  for `10 000` bodies).
- **Realistic Physical Context:** The simulation relies on real astronomical constants (Gravitational Constant $G$,
  Astronomical Units $AU$, Solar Mass, and Solar Radius) to perform calculations in a 3D space.
- **Star Color Rendering:** Calculates star colors based on mass-temperature relationships (automatically approximating
  Kelvin body temperature and mapping it to RGB color limits). For this, I used Tanner Helland's algorithm as described in SpaceDraw.java.
- **Visualized with CodeDraw:** Employs the fast and lightweight `CodeDraw` Java graphics library for plotting bodies
  frame by frame without lag. Developed by Niklas Kraßnig and extensively used by students at TU Wien for introductory
  programming courses.
- **Object-Oriented Design:** Built with a strong focus on correct OOP methodology. The octree structure leverages
  custom subtype relationships and polymorphism for the different types of tree nodes (such as internal nodes, leaf
  nodes containing bodies, and empty nodes), keeping the recursive tree logic elegant and robust.

## How it Works (The Magic of Barnes-Hut)

In a brute-force N-body simulation, every single body must calculate gravity against every other body, resulting in
an $O(N^2)$ algorithm that quickly grinds to a halt as the number of bodies grows.

The Barnes-Hut algorithm solves this by grouping distant bodies and approximating them as a single, massive point in
space. It achieves this in two steps:

1. **Tree Construction (The Octree):** The 3D space is recursively divided into 8 equally-sized sub-cubes (octants). If
   an octant contains more than one body, it splits again. This continues until every body sits isolated in its own leaf
   node. Finally, every parent node calculates the total mass and the unified center of mass for all of its children.
2. **Force Calculation (The Threshold `T`):** When calculating the gravitational force acting on a specific body, we
   traverse the Octree. If a grouped node is sufficiently far away—determined by the quotient $d / r < T$ (where $d$ is
   the region's width and $r$ is the distance to its center of mass)—the simulation treats that entire region as one
   giant body. If it is too close, the algorithm digs deeper into the node's children and evaluates them individually.

## Project Structure

### Class & Subtype Relationships

For a zoomable version, open this repo in a browser and open the dropdown under this picture with the full mermaid code.

```mermaid
classDiagram
    direction TB

    class OctNode {
        <<interface>>
        +insertBody(Body bodyToInsert) OctNode
        +updateCenters() void
        +getCenterBody() Body
        +deleteAll() void
        +drawBodies(CodeDraw cd) void
        +drawBoundaries(CodeDraw cd) void
        +bodiesInteractingWith(Body key, BodyStack stckBody) void
    }

    class Simulation {
        +double G$
        +double SIMULATION_G$
        +int T$
        +double AU$
        +double SUN_MASS$
        +double SUN_RADIUS$
        +double SECTION_SIZE$
        +int CANVAS_SIZE$
        +int NUMBER_OF_BODIES$
        +double OVERALL_SYSTEM_MASS$
        +boolean drawBoundaries$
        +boolean generateGalaxy$
        +boolean generateCollidingGalaxies$
        +main(String[] args)$ void
        -createGalaxy(int, Random)$ Body[]
        -createCollidingGalaxies(int, Random)$ Body[]
        -createRandomCloud(int, Random)$ Body[]
        -initGalaxyHelper(...)$ void
    }

    class Octree {
        -OctNode root
        -BoundaryCube mainBoundaryCube
        +Octree(BoundaryCube boundaryCube)
        +insertBody(Body bodyToInsert) boolean
        +deleteOctree() void
        +drawBodies(CodeDraw cd) void
        +updateCenters() void
        +drawBoundaries(CodeDraw cd) void
        +forceExertedOnBody(Body key) Vector3
    }

    class ParentOctNode {
        -OctNode[] subNodes
        -BoundaryCube[] subNodesBoundaries
        -BoundaryCube boundaryCube
        -Body center
        +ParentOctNode(Body body, BoundaryCube boundaryCube)
    }

    class BodyOctNode {
        -Body body
        -BoundaryCube boundaryCube
        +BodyOctNode(Body body, BoundaryCube boundaryCube)
    }

    class NullOctNode {
        -BoundaryCube boundaryCube
        +NullOctNode(Body body, BoundaryCube boundaryCube)
    }

    class Body {
        -double mass
        -Vector3 massCenter
        -Vector3 currentMovement
        +Body(double mass, Vector3 massCenter, Vector3 currentMovement)
        +Body()
        +distanceTo(Body b) double
        +gravitationalForce(Body b) Vector3
        +move(Vector3 force) void
        +radius() double
        +merge(Body b) Body
        +draw(CodeDraw cd) void
        +mass() double
        +getMass() double
        +getMassCenter() Vector3
    }

    class BoundaryCube {
        -Vector3 minPoint
        -Vector3 maxPoint
        -Vector3 midPoint
        -BoundaryCube[] subBoundaries
        -double d
        +BoundaryCube(Vector3 minPoint, Vector3 maxPoint)
        +isInBoundary(Body bodyToCheck) boolean
        +drawBoundary(CodeDraw cd) void
        +subregionsBoundaries() BoundaryCube[]
        +getMidPoint() Vector3
        +getD() double
    }

    class Vector3 {
        -double x
        -double y
        -double z
        +Vector3(double x, double y, double z)
        +Vector3(double setAll)
        +Vector3()
        +plus(Vector3 v) Vector3
        +minus(Vector3 v) Vector3
        +times(double d) Vector3
        +distanceTo(Vector3 v) double
        +length() double
        +normalize() void
        +drawAsFilledCircle(CodeDraw cd, double radius) void
        +getX() double
        +getY() double
        +getZ() double
    }

    class SpaceDraw {
        <<Utility>>
        +massToColor(double mass)$ Color
        +massToRadius(double mass)$ double
        -kelvinToColor(int kelvin)$ Color
        -limitAndDarken(double color, int kelvin)$ int
    }

    class BodyStack {
        -int size
        -BodyStackPart last
        +push(Body bodyToAdd) void
        +poll() Body
        +peek() Body
        +getSize() int
    }

    class BodyStackPart {
        -BodyStackPart previous
        -Body body
        +BodyStackPart(Body newBody, BodyStackPart previous)
        +getPrevious() BodyStackPart
        +setPrevious(BodyStackPart previous) void
        +getBody() Body
    }

    OctNode <|.. ParentOctNode : implements
    OctNode <|.. BodyOctNode : implements
    OctNode <|.. NullOctNode : implements

    Simulation ..> Octree : creates and runs
    Simulation ..> Body : creates
    Simulation ..> BoundaryCube : creates

    Octree "1" *-- "1" OctNode : root
    Octree "1" o-- "1" BoundaryCube : mainBoundaryCube
    Octree ..> BodyStack : uses in forceExertedOnBody

    ParentOctNode "1" *-- "8" OctNode : subNodes
    ParentOctNode "1" *-- "1" Body : center
    ParentOctNode "1" o-- "1" BoundaryCube : boundaryCube
    ParentOctNode "1" o-- "8" BoundaryCube : subNodesBoundaries

    BodyOctNode "1" o-- "1" Body : body
    BodyOctNode "1" o-- "1" BoundaryCube : boundaryCube

    NullOctNode "1" o-- "1" BoundaryCube : boundaryCube

    Body "1" *-- "2" Vector3 : massCenter, currentMovement
    Body ..> SpaceDraw : uses for drawing
    Body ..> Simulation : uses SIMULATION_G

    BoundaryCube "1" *-- "3" Vector3 : minPoint, maxPoint, midPoint
    BoundaryCube "1" *-- "8" BoundaryCube : subBoundaries
    BoundaryCube ..> Simulation : uses SECTION_SIZE

    Vector3 ..> Simulation : uses SECTION_SIZE

    SpaceDraw ..> Simulation : uses constants

    BodyStack "1" *-- "*" BodyStackPart : linked list
    BodyStackPart "1" o-- "1" Body : body
    BodyStackPart "0..1" o-- "0..1" BodyStackPart : previous
```

### Simplified Overview

```mermaid
classDiagram
    direction TB

    class Simulation
    class Octree
    class OctNode {
        <<interface>>
    }
    class ParentOctNode
    class BodyOctNode
    class NullOctNode
    class Body
    class BoundaryCube
    class Vector3
    class SpaceDraw {
        <<Utility>>
    }
    class BodyStack

    OctNode <|.. ParentOctNode
    OctNode <|.. BodyOctNode
    OctNode <|.. NullOctNode

    Simulation ..> Octree
    Simulation ..> Body
    Octree *-- OctNode : root
    Octree o-- BoundaryCube
    ParentOctNode *-- OctNode : subNodes
    ParentOctNode o-- BoundaryCube
    BodyOctNode o-- Body
    BodyOctNode o-- BoundaryCube
    NullOctNode o-- BoundaryCube
    Body *-- Vector3
    BoundaryCube *-- Vector3
    BoundaryCube *-- BoundaryCube : subBoundaries
    Body ..> SpaceDraw
    Octree ..> BodyStack
```

### Simulation Loop (one frame)

```mermaid
sequenceDiagram
    participant S as Simulation
    participant O as Octree
    participant R as root (ParentOctNode)
    participant B as Body

    S->>O: new Octree(mainBoundaryCube)

    loop for each body
        S->>O: insertBody(body)
        O->>R: insertBody(body)
        Note right of R: Recurses down the tree,<br/>promotes NullOctNode → BodyOctNode<br/>or BodyOctNode → ParentOctNode
    end

    S->>O: updateCenters()
    O->>R: updateCenters()
    Note right of R: Recursively computes<br/>barycenter for each node

    loop for each body
        S->>O: forceExertedOnBody(body)
        O->>R: bodiesInteractingWith(body, stack)
        Note right of R: If d/r < T → push center to stack<br/>else → recurse into children
        O-->>S: total force vector
    end

    loop for each body
        S->>B: move(force)
        Note right of B: Updates position and<br/>velocity using F=ma
    end

    S->>O: drawBodies(cd)
    S->>O: deleteOctree()
    Note over S: Repeat
```

The core algorithms and data structures are all located in `src/`:

- `Simulation.java`: The primary entry point. Initializes bodies within randomized normal distributions and runs the
  infinite calculation/render loop.
- `Octree.java` / `OctNode.java` / `ParentOctNode.java` / `BodyOctNode.java`: The core custom Octree structure that maps
  bounds and delegates force interactions across regions of space based on the $T$ quotient constraint ($d/r < T$).
- `Body.java`: Contains physical states (position, velocity, mass).
- `SpaceDraw.java`: Handles the visual mappings (mass-to-radius and kelvin-to-color mapping).
- `Vector3.java`: Performs fast 3D vector arithmetic.

## Getting Started

### Prerequisites

- Java 8 or higher
- IntelliJ IDEA (Recommended) or another standard IDE.

### Setup & Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/peter-kotouc/barnes-hut-algorithm.git
   cd barnes-hut-algorithm
   ```
2. **Open the project in IntelliJ IDEA:**
   - Ensure the `lib/CodeDraw.jar` is marked as a project dependency (it should be set up automatically if you open as
     an IntelliJ project).
3. **Run the Simulation:**
   - Run the `main` method located in `src/Simulation.java`.
   - A window size of `1000x1000` will open and start rendering the bodies.

### Configuration

You can tweak standard physics and rendering settings directly from within `Simulation.java`. Some examples are:

- `NUMBER_OF_BODIES`: Adjust the system body count. Default is `10,000`.
- `drawBoundaries`: Set to `true` to visualize the structural boundaries of the Octree processing regions.
- `T`: Adjust the threshold parameter. Larger values process faster but with less precision.

## Possible Next Steps

While the core computations of the simulation are robust, there are several exciting possibilities for future features:

- **True 3D Rendering:** The internal mathematics and positioning vectors are already fully three-dimensional. However,
  the simulation is currently drawn on a flat 2D canvas using the CodeDraw library. Implementing an actual 3D renderer
  or camera perspective would fully capture the spatial depth of the simulation.
- **Merging Bodies on Collision:** Right now, bodies pass through or orbit tightly around each other. A great
  improvement would be to implement physical collisions, allowing bodies that surpass a minimum distance threshold to
  merge into larger bodies with combined masses and momentum. (For this, the octree structure can be used to make it
  very efficient) Some preliminary merge functionality is implemented in Body class.
- **Different Star Distributions (Already roughly implemented):** The current simulation uses a normal distribution to generate stars. A different
  distribution could be used to create different types of galaxies. For example, a distribution that creates a spiral
  galaxy.

## Further Reading

If you would like to learn more about the Barnes-Hut algorithm, here are some excellent sources:

- **[Barnes-Hut Galaxy Simulator (arborjs)](https://arborjs.org/docs/barnes-hut):** A highly recommended, intuitive, and
  visual explanation of how the spatial tree (Quadtree/Octree) divides space and calculates the centers of mass to
  optimize the simulation.
- **J. Barnes and P. Hut:** _"A hierarchical O(N log N) force-calculation algorithm"_ in _Nature_, 324:446-449, 1986.

## Dependencies

- **[CodeDraw by Niklas Kraßnig](https://github.com/Krassnig/CodeDraw):** A lightweight drawing library designed for
  beginners in Java. Included locally in the `lib` folder.
- **[Project Lombok](https://projectlombok.org/):** A popular java library that plugs into the IDE and build tools to
  minimize boilerplate code (such as getters and constructors). Included locally in the `lib` folder.

## Responsible Use of AI

Parts of this README and some code comments were drafted using AI after the initial university submission. However, all original code, logic, and core comments were designed and written by me. I have manually reviewed and verified all AI-assisted content—including the post-submission galaxy simulation (which was researched by AI, but implemented by me) to ensure accuracy and correctness.

## License

This project is licensed under the [MIT License](LICENSE). Feel free to use and explore this code for educational
purposes or to study the Barnes-Hut algorithm!
