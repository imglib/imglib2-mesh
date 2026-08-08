package net.imglib2.mesh;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.imglib2.mesh.impl.nio.BufferMesh;

public class MeshesEqualTest
{

	@Test
	public void testMeshesEqualIdenticalMeshes()
	{
		final BufferMesh mesh1 = exampleMesh();
		final BufferMesh mesh2 = exampleMesh();
		assertTrue( Meshes.equals( mesh1, mesh2 ) );
	}

	@Test
	public void testMeshesEqualSameReference()
	{
		final BufferMesh mesh = exampleMesh();
		assertTrue( Meshes.equals( mesh, mesh ) );
	}

	@Test
	public void testMeshesEqualBothNull()
	{
		assertTrue( Meshes.equals( null, null ) );
	}

	@Test
	public void testMeshesEqualOneNull()
	{
		final BufferMesh mesh = exampleMesh();
		assertFalse( Meshes.equals( mesh, null ) );
		assertFalse( Meshes.equals( null, mesh ) );
	}

	@Test
	public void testMeshesEqualDifferentVertexPosition()
	{
		final BufferMesh mesh1 = exampleMesh();
		final BufferMesh mesh2 = exampleMesh();
		mesh2.vertices().setPositionf( 0, 1.0f, 0.0f, 1.0f );
		assertFalse( Meshes.equals( mesh1, mesh2 ) );
	}

	@Test
	public void testMeshesEqualDifferentTriangle()
	{
		final BufferMesh mesh1 = exampleMesh();
		// Create mesh with different triangle connectivity
		final BufferMesh mesh2 = new BufferMesh( 4, 4 );
		mesh2.vertices().add( 0.0f, 0.0f, 1.0f );
		mesh2.vertices().add( 0.0f, 0.942809f, -0.333333f );
		mesh2.vertices().add( -0.816497f, -0.471405f, -0.333333f );
		mesh2.vertices().add( 0.816497f, -0.471405f, -0.333333f );
		// Different triangle connectivity (swapped vertex order)
		mesh2.triangles().add( 0, 2, 1 ); // Was: 0, 1, 2
		mesh2.triangles().add( 0, 3, 2 ); // Was: 0, 2, 3
		mesh2.triangles().add( 0, 1, 3 ); // Was: 0, 3, 1
		mesh2.triangles().add( 1, 2, 3 ); // Was: 1, 3, 2
		assertFalse( Meshes.equals( mesh1, mesh2 ) );
	}

	@Test
	public void testMeshesEqualDifferentVertexCount()
	{
		final BufferMesh mesh1 = exampleMesh();
		final BufferMesh mesh2 = new BufferMesh( 5, 4 );
		mesh2.vertices().add( 0.0f, 0.0f, 1.0f );
		mesh2.vertices().add( 0.0f, 0.942809f, -0.333333f );
		mesh2.vertices().add( -0.816497f, -0.471405f, -0.333333f );
		mesh2.vertices().add( 0.816497f, -0.471405f, -0.333333f );
		mesh2.vertices().add( 1.0f, 1.0f, 1.0f ); // Extra vertex
		mesh2.triangles().add( 0, 1, 2 );
		mesh2.triangles().add( 0, 2, 3 );
		mesh2.triangles().add( 0, 3, 1 );
		mesh2.triangles().add( 1, 3, 2 );
		assertFalse( Meshes.equals( mesh1, mesh2 ) );
	}

	@Test
	public void testMeshesEqualDifferentTriangleCount()
	{
		final BufferMesh mesh1 = exampleMesh();
		final BufferMesh mesh2 = new BufferMesh( 4, 3 );
		mesh2.vertices().add( 0.0f, 0.0f, 1.0f );
		mesh2.vertices().add( 0.0f, 0.942809f, -0.333333f );
		mesh2.vertices().add( -0.816497f, -0.471405f, -0.333333f );
		mesh2.vertices().add( 0.816497f, -0.471405f, -0.333333f );
		mesh2.triangles().add( 0, 1, 2 );
		mesh2.triangles().add( 0, 2, 3 );
		mesh2.triangles().add( 0, 3, 1 );
		// One less triangle
		assertFalse( Meshes.equals( mesh1, mesh2 ) );
	}

	private static BufferMesh exampleMesh()
	{
		final BufferMesh mesh = new BufferMesh( 4, 4 );
		// 4 vertices of a tetrahedron
		mesh.vertices().add( 0.0f, 0.0f, 1.0f );
		mesh.vertices().add( 0.0f, 0.942809f, -0.333333f );
		mesh.vertices().add( -0.816497f, -0.471405f, -0.333333f );
		mesh.vertices().add( 0.816497f, -0.471405f, -0.333333f );
		// 4 triangles
		mesh.triangles().add( 0, 1, 2 );
		mesh.triangles().add( 0, 2, 3 );
		mesh.triangles().add( 0, 3, 1 );
		mesh.triangles().add( 1, 3, 2 );
		return mesh;
	}
}
