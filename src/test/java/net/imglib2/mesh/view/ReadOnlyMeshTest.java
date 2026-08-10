package net.imglib2.mesh.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.MeshExamples;
import net.imglib2.mesh.Triangle;
import net.imglib2.mesh.Vertex;
import net.imglib2.mesh.impl.nio.BufferMesh;

public class ReadOnlyMeshTest
{

	private static final double EPSILON = 1e-10;

	@Test
	public void testVertexPositions()
	{
		final BufferMesh original = MeshExamples.createTetrahedron();
		final Mesh readOnly = ReadOnlyMesh.readOnly( original );

		int vertexIndex = 0;
		for ( final Vertex origVertex : original.vertices() )
		{
			final Vertex readOnlyVertex = getVertex( readOnly, vertexIndex );
			assertEquals( "X coordinate mismatch for vertex " + vertexIndex, origVertex.x(), readOnlyVertex.x(), EPSILON );
			assertEquals( "Y coordinate mismatch for vertex " + vertexIndex, origVertex.y(), readOnlyVertex.y(), EPSILON );
			assertEquals( "Z coordinate mismatch for vertex " + vertexIndex, origVertex.z(), readOnlyVertex.z(), EPSILON );
			vertexIndex++;
		}
	}

	@Test
	public void testTriangleConnectivity()
	{
		final BufferMesh original = MeshExamples.createTetrahedron();
		final Mesh readOnly = ReadOnlyMesh.readOnly( original );

		int triIndex = 0;
		for ( final Triangle origTri : original.triangles() )
		{
			final Triangle readOnlyTri = getTriangle( readOnly, triIndex );
			assertEquals( "Triangle " + triIndex + " vertex0 index mismatch", origTri.vertex0(), readOnlyTri.vertex0() );
			assertEquals( "Triangle " + triIndex + " vertex1 index mismatch", origTri.vertex1(), readOnlyTri.vertex1() );
			assertEquals( "Triangle " + triIndex + " vertex2 index mismatch", origTri.vertex2(), readOnlyTri.vertex2() );
			triIndex++;
		}
	}

	@Test
	public void testTriangleNormals()
	{
		final BufferMesh original = MeshExamples.createTetrahedron();
		final Mesh readOnly = ReadOnlyMesh.readOnly( original );

		int triIndex = 0;
		for ( final Triangle origTri : original.triangles() )
		{
			final Triangle readOnlyTri = getTriangle( readOnly, triIndex );
			assertEquals( "Triangle " + triIndex + " normal X mismatch", origTri.nx(), readOnlyTri.nx(), EPSILON );
			assertEquals( "Triangle " + triIndex + " normal Y mismatch", origTri.ny(), readOnlyTri.ny(), EPSILON );
			assertEquals( "Triangle " + triIndex + " normal Z mismatch", origTri.nz(), readOnlyTri.nz(), EPSILON );
			triIndex++;
		}
	}

	@Test
	public void testReadOnlyMeshCannotAddVertex()
	{
		final BufferMesh original = MeshExamples.createTetrahedron();
		final Mesh readOnly = ReadOnlyMesh.readOnly( original );

		assertThrows( "Adding vertex should throw UnsupportedOperationException",
			UnsupportedOperationException.class,
			() -> readOnly.vertices().add( 1.0f, 2.0f, 3.0f ) );
	}

	@Test
	public void testReadOnlyMeshCannotSetVertexPosition()
	{
		final BufferMesh original = MeshExamples.createTetrahedron();
		final Mesh readOnly = ReadOnlyMesh.readOnly( original );

		assertThrows( "Setting vertex position should throw UnsupportedOperationException",
			UnsupportedOperationException.class,
			() -> readOnly.vertices().setPosition( 0, 10.0, 10.0, 10.0 ) );
	}

	@Test
	public void testReadOnlyMeshCannotSetVertexNormal()
	{
		final BufferMesh original = MeshExamples.createTetrahedron();
		final Mesh readOnly = ReadOnlyMesh.readOnly( original );

		assertThrows( "Setting vertex normal should throw UnsupportedOperationException",
			UnsupportedOperationException.class,
			() -> readOnly.vertices().setNormal( 0, 1.0, 0.0, 0.0 ) );
	}

	@Test
	public void testReadOnlyMeshCannotSetVertexTexture()
	{
		final BufferMesh original = MeshExamples.createTetrahedron();
		final Mesh readOnly = ReadOnlyMesh.readOnly( original );

		assertThrows( "Setting vertex texture should throw UnsupportedOperationException",
			UnsupportedOperationException.class,
			() -> readOnly.vertices().setTexture( 0, 0.5, 0.5 ) );
	}

	@Test
	public void testReadOnlyMeshCannotSetVertexAttributes()
	{
		final BufferMesh original = MeshExamples.createTetrahedron();
		final Mesh readOnly = ReadOnlyMesh.readOnly( original );

		assertThrows( "Setting vertex attributes should throw UnsupportedOperationException",
			UnsupportedOperationException.class,
			() -> readOnly.vertices().set( 0, 1.0, 2.0, 3.0, 0.0, 0.0, 1.0, 0.0, 0.0 ) );
	}

	@Test
	public void testVertexIterator()
	{
		final BufferMesh original = MeshExamples.createTetrahedron();
		final Mesh readOnly = ReadOnlyMesh.readOnly( original );

		int vertexCount = 0;
		for ( final Vertex v : readOnly.vertices() )
		{
			vertexCount++;
			assertNotNull( "Vertex should not be null", v );
		}
		assertEquals( "Should iterate over all vertices", original.vertices().sizel(), vertexCount );
	}

	@Test
	public void testTriangleIterator()
	{
		final BufferMesh original = MeshExamples.createTetrahedron();
		final Mesh readOnly = ReadOnlyMesh.readOnly( original );

		int triangleCount = 0;
		for ( final Triangle t : readOnly.triangles() )
		{
			triangleCount++;
			assertNotNull( "Triangle should not be null", t );
		}
		assertEquals( "Should iterate over all triangles", original.triangles().sizel(), triangleCount );
	}

	@Test
	public void testReadOnlyMeshChaining()
	{
		// Test that read-only wrapper can be chained
		final BufferMesh original = MeshExamples.createTetrahedron();
		final Mesh readOnly1 = ReadOnlyMesh.readOnly( original );
		final Mesh readOnly2 = ReadOnlyMesh.readOnly( readOnly1 );

		assertNotNull( "Chained read-only mesh should not be null", readOnly2 );
		assertEquals( "Chained read-only mesh should preserve vertex count",
			original.vertices().sizel(), readOnly2.vertices().sizel() );
	}

	@Test
	public void testVertexReads()
	{
		// Verify that read operations delegate to the original mesh
		final BufferMesh original = MeshExamples.createTetrahedron();
		final Mesh readOnly = ReadOnlyMesh.readOnly( original );

		final double x = 100.;
		final double y = 200.;
		final double z = 300.;
		original.vertices().setPosition( 0, x, y, z );

		final Vertex readOnlyVertex = getVertex( readOnly, 0 );
		assertEquals( "Read-only mesh should delegate vertex reads", x, readOnlyVertex.x(), EPSILON );
		assertEquals( "Read-only mesh should delegate vertex reads", y, readOnlyVertex.y(), EPSILON );
		assertEquals( "Read-only mesh should delegate vertex reads", z, readOnlyVertex.z(), EPSILON );
	}

	@Test
	public void testTriangleReads()
	{
		// Verify that read operations delegate to the original mesh
		final BufferMesh original = MeshExamples.createTetrahedron();
		final Mesh readOnly = ReadOnlyMesh.readOnly( original );

		final Triangle origTri = getTriangle( original, 0 );
		final long v0 = origTri.vertex0();
		final long v1 = origTri.vertex1();
		final long v2 = origTri.vertex2();

		final Triangle readOnlyTri = getTriangle( readOnly, 0 );
		assertEquals( "Read-only mesh should delegate triangle reads", v0, readOnlyTri.vertex0() );
		assertEquals( "Read-only mesh should delegate triangle reads", v1, readOnlyTri.vertex1() );
		assertEquals( "Read-only mesh should delegate triangle reads", v2, readOnlyTri.vertex2() );
	}

	/** Get a vertex by index from a mesh. */
	private Vertex getVertex( final Mesh mesh, final int index )
	{
		int i = 0;
		for ( final Vertex v : mesh.vertices() )
		{
			if ( i == index )
				return v;

			i++;
		}
		throw new IndexOutOfBoundsException( "Vertex index " + index + " out of bounds" );
	}

	/** Get a triangle by index from a mesh. */
	private Triangle getTriangle( final Mesh mesh, final int index )
	{
		int i = 0;
		for ( final Triangle t : mesh.triangles() )
		{
			if ( i == index )
				return t;
			i++;
		}
		throw new IndexOutOfBoundsException( "Triangle index " + index + " out of bounds" );
	}
}
