package net.imglib2.mesh.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.imglib2.RealPoint;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.MeshExamples;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.Vertex;
import net.imglib2.mesh.impl.nio.BufferMesh;

public class TranslateMeshTest
{

	private static final double EPSILON = 1e-10;

	@Test
	public void testTranslateMeshZeroTranslation()
	{
		final BufferMesh original = MeshExamples.createTetrahedron();
		final RealPoint translation = new RealPoint( 0., 0., 0. );
		final Mesh translated = TranslateMesh.translate( original, translation );

		assertTrue( "Translated by 0,0,0 should result in identical meshes.", Meshes.equals( original, translated ) );
	}

	@Test
	public void testTranslateMeshIntegerTranslation()
	{
		final double dx = 5.;
		final double dy = -3.;
		final double dz = 2.;
		final BufferMesh original = MeshExamples.createTetrahedron();
		final RealPoint translation = new RealPoint( dx, dy, dz );
		final Mesh translated = TranslateMesh.translate( original, translation );

		assertVerticesTranslated( original, translated, dx, dy, dz );
	}

	private void assertVerticesTranslated(
			final Mesh original,
			final Mesh translated,
			final double dx,
			final double dy,
			final double dz )
	{
		assertEquals( "Vertex count mismatch", original.vertices().sizel(), translated.vertices().sizel() );

		int vertexIndex = 0;
		for ( final Vertex origVertex : original.vertices() )
		{
			final Vertex transVertex = getVertex( translated, vertexIndex );
			assertEquals( "X coordinate mismatch for vertex " + vertexIndex, origVertex.x() + dx, transVertex.x(), EPSILON );
			assertEquals( "Y coordinate mismatch for vertex " + vertexIndex, origVertex.y() + dy, transVertex.y(), EPSILON );
			assertEquals( "Z coordinate mismatch for vertex " + vertexIndex, origVertex.z() + dz, transVertex.z(), EPSILON );
			vertexIndex++;
		}
	}

	/** Get the i-th vertex from the mesh. */
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
}
