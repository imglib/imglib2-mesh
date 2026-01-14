/*-
 * #%L
 * 3D mesh structures for ImgLib2-related projects.
 * %%
 * Copyright (C) 2016 - 2025 ImgLib2 developers.
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

package net.imglib2.mesh.alg;

import java.util.LinkedHashMap;
import java.util.Map;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.impl.nio.BufferMesh;

/**
 * @author Deborah Schmidt
 * @author Eugene Katrukha
 */
public class RemoveDuplicateVertices
{

	public static BufferMesh calculate( final Mesh mesh, final int precision )
	{
		final Map< String, IndexedVertex > vertices = new LinkedHashMap<>();
		final int[][] triangles = new int[ mesh.triangles().size() ][ 3 ];

		int trianglesCount = 0;
		for ( final net.imglib2.mesh.Triangle triangle : mesh.triangles() )
		{
			final RealPoint p1 = new RealPoint( triangle.v0x(), triangle.v0y(), triangle.v0z() );
			final RealPoint p2 = new RealPoint( triangle.v1x(), triangle.v1y(), triangle.v1z() );
			final RealPoint p3 = new RealPoint( triangle.v2x(), triangle.v2y(), triangle.v2z() );
			
			final int v1 = getVertex( vertices, p1, precision );
			final int v2 = getVertex( vertices, p2, precision );
			final int v3 = getVertex( vertices, p3, precision );
			
			if( v1 != v2 && v1 != v3 && v2 != v3 )
			{
				triangles[ trianglesCount ][ 0 ] = v1;
				triangles[ trianglesCount ][ 1 ] = v2;
				triangles[ trianglesCount ][ 2 ] = v3;
				trianglesCount++;
			}
		}
		final BufferMesh res = new BufferMesh( vertices.size(), triangles.length );
		vertices.values().forEach( vertex -> {
			res.vertices().add( vertex.point.getFloatPosition( 0 ), vertex.point.getFloatPosition( 1 ), vertex.point.getFloatPosition( 2 ) );
		} );

		for ( final int[] triangle : triangles )
		{
			res.triangles().add( triangle[ 0 ], triangle[ 1 ], triangle[ 2 ] );
		}
		return res;
	}

	private static int getVertex( final Map< String, IndexedVertex > vertices, final RealPoint point, final int precision )
	{
		final String hash = getHash( point, precision );
		final IndexedVertex vertex = vertices.get( hash );
		if ( vertex == null )
			return createVertex( vertices, hash, point, precision );
		return vertex.index;
	}

	private static int createVertex( final Map< String, IndexedVertex > vertices, final String hash, final RealPoint point, final int precision )
	{
		final int index = vertices.size();
		final IndexedVertex vertex = new IndexedVertex( point, index, precision );
		vertices.put( hash, vertex );
		return index;
	}

	private static String getHash( final RealPoint point, final int precision )
	{
		final int factor = ( int ) Math.pow( 10, precision );
		return Math.round( point.getFloatPosition( 0 ) * factor ) + "-" + Math.round( point.getFloatPosition( 1 ) * factor ) + "-" + Math.round( point.getFloatPosition( 2 ) * factor );
	}

	private static class IndexedVertex
	{

		int index;

		RealLocalizable point;

		IndexedVertex( final RealLocalizable pos, final int index, final int precision )
		{
			final double[] newpos = new double[ pos.numDimensions() ];
			final double factor = Math.pow( 10, precision );
			for ( int i = 0; i < newpos.length; i++ )
			{
				newpos[ i ] = Math.round( pos.getDoublePosition( i ) * factor ) / factor;
			}
			this.point = new RealPoint( newpos );
			this.index = index;
		}
	}
}
