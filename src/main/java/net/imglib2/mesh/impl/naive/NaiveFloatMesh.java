/*-
 * #%L
 * 3D mesh structures for ImgLib2-related projects.
 * %%
 * Copyright (C) 2016 - 2023 ImgLib2 developers.
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

package net.imglib2.mesh.impl.naive;

import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import net.imglib2.mesh.Mesh;

public class NaiveFloatMesh implements Mesh
{

	private final Vertices vertices;

	private final Triangles triangles;

	public NaiveFloatMesh()
	{
		vertices = new Vertices();
		triangles = new Triangles();
	}

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

	// -- Inner classes --

	public class Vertices implements net.imglib2.mesh.Vertices
	{

		private final TFloatArrayList xs, ys, zs;

		private final TFloatArrayList nxs, nys, nzs;

		private final TFloatArrayList us, vs;

		public Vertices()
		{
			xs = new TFloatArrayList();
			ys = new TFloatArrayList();
			zs = new TFloatArrayList();
			nxs = new TFloatArrayList();
			nys = new TFloatArrayList();
			nzs = new TFloatArrayList();
			us = new TFloatArrayList();
			vs = new TFloatArrayList();
		}

		@Override
		public Mesh mesh()
		{
			return NaiveFloatMesh.this;
		}

		@Override
		public long sizel()
		{
			return xs.size();
		}

		@Override
		public float xf( final long vIndex )
		{
			return xs.get( safeIndex( vIndex ) );
		}

		@Override
		public float yf( final long vIndex )
		{
			return ys.get( safeIndex( vIndex ) );
		}

		@Override
		public float zf( final long vIndex )
		{
			return zs.get( safeIndex( vIndex ) );
		}

		@Override
		public float nxf( final long vIndex )
		{
			return nxs.get( safeIndex( vIndex ) );
		}

		@Override
		public float nyf( final long vIndex )
		{
			return nys.get( safeIndex( vIndex ) );
		}

		@Override
		public float nzf( final long vIndex )
		{
			return nzs.get( safeIndex( vIndex ) );
		}

		@Override
		public float uf( final long vIndex )
		{
			return us.get( safeIndex( vIndex ) );
		}

		@Override
		public float vf( final long vIndex )
		{
			return vs.get( safeIndex( vIndex ) );
		}

		@Override
		public long addf( final float x, final float y, final float z, final float nx, final float ny, final float nz,
				final float u, final float v )
		{
			final int index = xs.size();
			xs.add( x );
			ys.add( y );
			zs.add( z );
			nxs.add( nx );
			nys.add( ny );
			nzs.add( nz );
			us.add( u );
			vs.add( v );
			return index;
		}

		@Override
		public void setf( final long vIndex, final float x, final float y, final float z, final float nx, final float ny,
				final float nz, final float u, final float v )
		{
			final int index = safeIndex( vIndex );
			xs.set( index, x );
			ys.set( index, y );
			zs.set( index, z );
			nxs.set( index, nx );
			nys.set( index, ny );
			nzs.set( index, nz );
			us.set( index, u );
			vs.set( index, v );
		}

		@Override
		public void setPositionf( final long vIndex, final float x,
				final float y, final float z )
		{
			final int index = safeIndex( vIndex );
			xs.set( index, x );
			ys.set( index, y );
			zs.set( index, z );

		}

		@Override
		public void setNormalf( final long vIndex, final float nx,
				final float ny, final float nz )
		{
			final int index = safeIndex( vIndex );
			nxs.set( index, nx );
			nys.set( index, ny );
			nzs.set( index, nz );
		}

		@Override
		public void setTexturef( final long vIndex, final float u, final float v )
		{
			final int index = safeIndex( vIndex );
			us.set( index, u );
			vs.set( index, v );
		}

		private int safeIndex( final long index )
		{
			if ( index > Integer.MAX_VALUE )
			{ throw new IndexOutOfBoundsException( "Index too large: " + index ); }
			return ( int ) index;
		}
	}

	public class Triangles implements net.imglib2.mesh.Triangles
	{

		private final TIntArrayList v0s, v1s, v2s;

		private final TFloatArrayList nxs, nys, nzs;

		public Triangles()
		{
			v0s = new TIntArrayList();
			v1s = new TIntArrayList();
			v2s = new TIntArrayList();
			nxs = new TFloatArrayList();
			nys = new TFloatArrayList();
			nzs = new TFloatArrayList();
		}

		@Override
		public Mesh mesh()
		{
			return NaiveFloatMesh.this;
		}

		@Override
		public long sizel()
		{
			return v1s.size();
		}

		@Override
		public long vertex0( final long tIndex )
		{
			return v0s.get( safeIndex( tIndex ) );
		}

		@Override
		public long vertex1( final long tIndex )
		{
			return v1s.get( safeIndex( tIndex ) );
		}

		@Override
		public long vertex2( final long tIndex )
		{
			return v2s.get( safeIndex( tIndex ) );
		}

		@Override
		public float nxf( final long tIndex )
		{
			return nxs.get( safeIndex( tIndex ) );
		}

		@Override
		public float nyf( final long tIndex )
		{
			return nys.get( safeIndex( tIndex ) );
		}

		@Override
		public float nzf( final long tIndex )
		{
			return nzs.get( safeIndex( tIndex ) );
		}

		@Override
		public void setNormal( final int tIndex, final float nxf, final float nyf, final float nzf )
		{
			nxs.set( tIndex, nxf );
			nys.set( tIndex, nyf );
			nzs.set( tIndex, nzf );
		}

		@Override
		public long addf( final long v0, final long v1, final long v2, final float nx, final float ny, final float nz )
		{
			final int index = v0s.size();
			v0s.add( safeIndex( v0 ) );
			v1s.add( safeIndex( v1 ) );
			v2s.add( safeIndex( v2 ) );
			nxs.add( nx );
			nys.add( ny );
			nzs.add( nz );
			return index;
		}

		private int safeIndex( final long index )
		{
			if ( index > Integer.MAX_VALUE )
			{ throw new IndexOutOfBoundsException( "Index too large: " + index ); }
			return ( int ) index;
		}
	}
}
