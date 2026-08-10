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
