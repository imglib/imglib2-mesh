/*-
 * #%L
 * 3D mesh structures for ImgLib2-related projects.
 * %%
 * Copyright (C) 2016 - 2026 ImgLib2 developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
