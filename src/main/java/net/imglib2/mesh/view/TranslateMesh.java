/*-
 * #%L
 * 3D mesh structures for ImgLib2-related projects.
 * %%
 * Copyright (C) 2016 - 2024 ImgLib2 developers.
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

import java.util.Iterator;

import net.imglib2.RealLocalizable;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Triangles;
import net.imglib2.mesh.Vertex;
import net.imglib2.mesh.Vertices;

/**
 * Read-only view of a mesh where vertices are translated by a fixed amount.
 * Does not copy the source mesh.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class TranslateMesh implements Mesh
{

	private final Mesh in;

	private final TranslatedVertices vertices;

	public static final Mesh translate( final Mesh in, final RealLocalizable t )
	{
		return new TranslateMesh( in, t );
	}

	private TranslateMesh( final Mesh in, final RealLocalizable t )
	{
		this.in = in;
		this.vertices = new TranslatedVertices( in.vertices(), t );
	}

	@Override
	public Vertices vertices()
	{
		return vertices;
	}

	@Override
	public Triangles triangles()
	{
		return in.triangles();
	}

	private static final class TranslatedVertices implements Vertices
	{

		private final Vertices invs;

		private final RealLocalizable t;

		public TranslatedVertices( final Vertices invs, final RealLocalizable t )
		{
			this.invs = invs;
			this.t = t;
		}

		@Override
		public Iterator< Vertex > iterator()
		{
			return new Iterator< Vertex >()
			{

				private long index = -1;

				private final Vertex vertex = new Vertex()
				{

					@Override
					public Mesh mesh()
					{
						return TranslatedVertices.this.mesh();
					}

					@Override
					public long index()
					{
						return index;
					}

					@Override
					public double x()
					{
						return TranslatedVertices.this.x( index() );
					}

					@Override
					public double y()
					{
						return TranslatedVertices.this.y( index() );
					}

					@Override
					public double z()
					{
						return TranslatedVertices.this.z( index() );
					}

					@Override
					public float xf()
					{
						return TranslatedVertices.this.xf( index() );
					}

					@Override
					public float yf()
					{
						return TranslatedVertices.this.yf( index() );
					}

					@Override
					public float zf()
					{
						return TranslatedVertices.this.zf( index() );
					}
				};

				@Override
				public boolean hasNext()
				{
					return index + 1 < sizel();
				}

				@Override
				public Vertex next()
				{
					index++;
					return vertex;
				}
			};
		}

		@Override
		public Mesh mesh()
		{
			return invs.mesh();
		}

		@Override
		public long sizel()
		{
			return invs.sizel();
		}

		@Override
		public float xf( final long vIndex )
		{
			return invs.xf( vIndex ) + t.getFloatPosition( 0 );
		}

		@Override
		public double x( final long vIndex )
		{
			return invs.x( vIndex ) + t.getDoublePosition( 0 );
		}

		@Override
		public float yf( final long vIndex )
		{
			return invs.yf( vIndex ) + t.getFloatPosition( 1 );
		}

		@Override
		public double y( final long vIndex )
		{
			return invs.y( vIndex ) + t.getDoublePosition( 1 );
		}

		@Override
		public float zf( final long vIndex )
		{
			return invs.zf( vIndex ) + t.getFloatPosition( 2 );
		}

		@Override
		public double z( final long vIndex )
		{
			return invs.z( vIndex ) + t.getDoublePosition( 2 );
		}

		@Override
		public float nxf( final long vIndex )
		{
			return invs.nxf( vIndex );
		}

		@Override
		public float nyf( final long vIndex )
		{
			return invs.nyf( vIndex );
		}

		@Override
		public float nzf( final long vIndex )
		{
			return invs.nzf( vIndex );
		}

		@Override
		public float uf( final long vIndex )
		{
			return invs.uf( vIndex );
		}

		@Override
		public float vf( final long vIndex )
		{
			return invs.vf( vIndex );
		}

		@Override
		public long addf( final float x, final float y, final float z, final float nx, final float ny, final float nz,
				final float u, final float v )
		{
			throw new IllegalArgumentException( "The position of mesh views are not modifiable." );
		}

		@Override
		public void setf( final long vIndex, final float x, final float y, final float z, final float nx, final float ny,
				final float nz, final float u, final float v )
		{
			throw new IllegalArgumentException( "The position of mesh views are not modifiable." );
		}

		@Override
		public void setf( final long vIndex, final float x, final float y, final float z )
		{
			throw new IllegalArgumentException( "The position of mesh views are not modifiable." );
		}

		@Override
		public void set( final long vIndex, final double x, final double y, final double z )
		{
			throw new IllegalArgumentException( "The position of mesh views are not modifiable." );
		}

		@Override
		public void set( final long vIndex, final double x, final double y, final double z, final double nx,
				final double ny, final double nz, final double u, final double v )
		{
			throw new IllegalArgumentException( "The position of mesh views are not modifiable." );
		}

		@Override
		public void setPositionf( final long vIndex, final float x, final float y, final float z )
		{
			throw new IllegalArgumentException( "The position of mesh views are not modifiable." );
		}

		@Override
		public void setPosition( final long vIndex, final double x, final double y, final double z )
		{
			throw new IllegalArgumentException( "The position of mesh views are not modifiable." );
		}

		@Override
		public void setNormalf( final long vIndex, final float nx, final float ny, final float nz )
		{
			invs.setNormalf( vIndex, nx, ny, nz );
		}

		@Override
		public void setTexturef( final long vIndex, final float u, final float v )
		{
			invs.setTexturef( vIndex, u, v );
		}
	}
}
