[![Build Status](https://github.com/imglib/imglib2-mesh/actions/workflows/build.yml/badge.svg)](https://github.com/imglib/imglib2-mesh/actions/workflows/build.yml)

# ImgLib2 Mesh

3D triangle mesh data structures and algorithms for [ImgLib2](https://imglib2.net/).

ImgLib2 Mesh provides a lightweight `Mesh` interface, several implementations,
and a collection of algorithms for building meshes from images, measuring them,
transforming them, and turning them back into images.

## Features

**Data structures**

* `Mesh` — a collection of `Vertices` (position, normal, texture coordinates)
  and `Triangles` (vertex indices plus a normal), with coordinates accessible as
  `float` or `double`.
* `BufferMesh` — backed by `java.nio` buffers, suitable for handing off to
  OpenGL-style renderers.
* `NaiveFloatMesh` / `NaiveDoubleMesh` — simple growable array-backed meshes.
* `TranslateMesh` and `ReadOnlyMesh` — lightweight views over an existing mesh.

**Algorithms** (mostly exposed via `Meshes` and `MeshStats`)

| Task | Classes |
|------|---------|
| Image → mesh | Marching cubes for `BooleanType` and `RealType` images |
| Mesh → image | `EuclideanDistanceVoxelization` (and the deprecated `Voxelization`) |
| Cleanup | `RemoveDuplicateVertices`, `SimplifyMesh`, `TaubinSmoothing`, normal calculation |
| Topology | `MeshConnectedComponents`, `Meshes.merge` |
| Geometry | Convex hull (quickhull), ellipsoid fitting, inertia tensor, bounding box, centroid |
| Shape descriptors | Volume, surface area, solidity, convexity, sphericity, compactness, elongation, … |
| Queries | Point-in-mesh tests (`Interior`), ray–triangle intersection (`MollerTrumbore`) |
| Slicing | `ZSlicer` — cut a two-manifold mesh with Z planes into closed contours |
| Transforms | Translate, scale |

**I/O** (`net.imglib2.mesh.io`)

* `PLYMeshIO` — read and write PLY (ASCII and binary).
* `STLMeshIO` — read and write binary STL.
* `XYZPointsIO` — read XYZ point clouds.

Many algorithms are also annotated for discovery by
[SciJava Ops](https://github.com/scijava/scijava) (e.g. `geom.size`,
`geom.boundingBox`), so they can also be called as ops.

## Installation

ImgLib2 Mesh is released to the [SciJava Maven repository](https://maven.scijava.org/).
Add the repository and dependency to your `pom.xml`:

```xml
<repositories>
	<repository>
		<id>scijava.public</id>
		<url>https://maven.scijava.org/content/groups/public</url>
	</repository>
</repositories>

<dependencies>
	<dependency>
		<groupId>net.imglib2</groupId>
		<artifactId>imglib2-mesh</artifactId>
		<version>1.2.0</version>
	</dependency>
</dependencies>
```

If your project extends [pom-scijava](https://github.com/scijava/pom-scijava),
the version is managed for you and can be omitted.

## Usage

See [`ExamplesTest.java`](src/test/java/net/imglib2/mesh/ExamplesTest.java) for
worked examples: building a mesh from an image and measuring it, building one
by hand, reading and writing files, and a tour of other operations. They run as
part of the test suite, so they stay in sync with the API. The other
[unit tests](src/test/java/net/imglib2/mesh) show more usage.

## Building from source

Building requires JDK 11.0.17 or newer and Maven; the resulting library
targets Java 8.

```sh
git clone https://github.com/imglib/imglib2-mesh
cd imglib2-mesh
mvn clean install
```

## Contributing

Bug reports and pull requests are welcome on
[GitHub](https://github.com/imglib/imglib2-mesh/issues). For questions and
discussion, visit the [Image.sc Forum](https://forum.image.sc/tag/imagej).

## License

ImgLib2 Mesh is distributed under the
[Simplified BSD License](LICENSE.txt).
