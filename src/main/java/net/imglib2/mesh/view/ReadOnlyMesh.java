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

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Triangles;
import net.imglib2.mesh.Vertices;

public class ReadOnlyMesh implements Mesh
{

	public static final Mesh readOnly( final Mesh in )
	{
		return new ReadOnlyMesh( in );
	}

	private ReadOnlyMesh( final Mesh in )
	{
		this.in = in;
		this.vertices = new ReadOnlyVertices();
		this.triangles = new ReadOnlyTriangles();
	}
	
	private final Mesh in;

	private final ReadOnlyVertices vertices;

	private final ReadOnlyTriangles triangles;

	@Override
	public Vertices vertices()
	{
		return vertices;
	}

	@Override
	public Triangles triangles()
	{
		return triangles;
	}

	private class ReadOnlyVertices implements DelegateVertices
	{

		@Override
		public Vertices delegate()
		{
			return in.vertices();
		}

		@Override
		public long addf( final float x, final float y, final float z )
		{
			throw new UnsupportedOperationException( "Read-only mesh: cannot add vertex." );
		}

		@Override
		public void setf( final long vIndex, final float x, final float y, final float z, final float nx, final float ny, final float nz, final float u, final float v )
		{
			throw new UnsupportedOperationException( "Read-only mesh: cannot set vertex attributes." );
		}

		@Override
		public void setPositionf( final long vIndex, final float x, final float y, final float z )
		{
			throw new UnsupportedOperationException( "Read-only mesh: cannot set vertex position." );
		}

		@Override
		public void setNormalf( final long vIndex, final float nx, final float ny, final float nz )
		{
			throw new UnsupportedOperationException( "Read-only mesh: cannot set vertex normal." );
		}

		@Override
		public void setTexturef( final long vIndex, final float u, final float v )
		{
			throw new UnsupportedOperationException( "Read-only mesh: cannot set vertex texture coordinates." );
		}
	}

	private final class ReadOnlyTriangles implements DelegateTriangles
	{

		@Override
		public Triangles delegate()
		{
			return in.triangles();
		}

		@Override
		public long addf( final float v0x, final float v0y, final float v0z, final float v1x, final float v1y, final float v1z, final float v2x, final float v2y, final float v2z, final float nx, final float ny, final float nz )
		{
			throw new UnsupportedOperationException( "Read-only mesh: cannot add triangle." );
		}


	}

}
