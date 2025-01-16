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
package net.imglib2.mesh.util;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Triangles;
import net.imglib2.mesh.Vertices;

public class MeshUtil
{

	/**
	 * Dot product between vectors V1 and V2.
	 *
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @return
	 */
	public static final double dotProduct( final double x1, final double y1, final double z1, final double x2,
			final double y2, final double z2 )
	{
		return x1 * x2 + y1 * y2 + z1 * z2;
	}

	/**
	 * Cross product between vectors V1 and V2. Stores the results in the
	 * specified <code>double</code> array.
	 *
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @param out
	 *            a <code>double</code> array of size at least 3 to store the
	 *            coordinates of resulting vector.
	 */
	public static final void cross( final double x1, final double y1, final double z1, final double x2, final double y2,
			final double z2, final double[] out )
	{
		out[ 0 ] = y1 * z2 - z1 * y2;
		out[ 1 ] = -x1 * z2 + z1 * x2;
		out[ 2 ] = x1 * y2 - y1 * x2;
	}

	/**
	 * Builds a unique id for an edge between the two specified vertices. Such
	 * an edge is undirected; the edge v1-&gt;v2 and v2-&gt;v1 have the the same
	 * id.
	 *
	 * @param v1
	 *            the id of the first vertex.
	 * @param v2
	 *            the id of the second vertex.
	 * @return the edge id, as a <code>long</code>.
	 */
	public static final long edgeID( final int v1, final int v2 )
	{
		return ( v1 < v2 ) ? concat( v1, v2 ) : concat( v2, v1 );
	}

	private static final long concat( final int i1, final int i2 )
	{
		final long l = ( ( ( long ) i1 ) << 32 ) | ( i2 & 0xffffffffL );
		return l;
	}

	/**
	 * Returns the id of the first vertex of the specified edge.
	 *
	 * @param l
	 *            the edge id.
	 * @return the first vertex id, as an <code>int</code>.
	 */
	public static final int edgeV1( final long l )
	{
		final int i1 = ( int ) ( l >> 32 );
		return i1;
	}

	/**
	 * Returns the id of the second vertex of the specified edge.
	 *
	 * @param l
	 *            the edge id.
	 * @return the second vertex id, as an <code>int</code>.
	 */
	public static final int edgeV2( final long l )
	{
		final int i2 = ( int ) l;
		return i2;
	}

	public static final String edgeStr( final long l )
	{
		return String.format( "E: (%d -> %d)", edgeV1( l ), edgeV2( l ) );
	}

	/**
	 * Rounds a position to a multiple of small number, plus reminder. This is
	 * used typically to ensure a ray will not cross a vertex in a mesh, causing
	 * a singularity.
	 * 
	 * @param v
	 *            the value to round.
	 * @param eps
	 *            the scale to round to.
	 * @param mod
	 *            the number of time eps to round. For instance if you put 2,
	 *            everything will be a multiple of 2*eps.
	 * @param rem
	 *            the remainder to add to the rounded value. For instance if you
	 *            put a remainder of 1 and a mod of, all values will be rounded
	 *            to 2 times eps + eps.
	 * @return the rounded value.
	 */
	public static final double mround( final double v, final double eps, final int mod, final int rem )
	{
		final long y = Math.round( v / ( mod * eps ) );
		final double z = ( y * mod + rem ) * eps;
		return z;
	}

	/**
	 * Return the minimal Z value of a given triangle, rounded to twice the
	 * specified scale.
	 * 
	 * @param vertices
	 *            the vertices structure.
	 * @param v0
	 *            the triangle first vertex.
	 * @param v1
	 *            the triangle second vertex.
	 * @param v2
	 *            the triangle third vertex. Order does not matter.
	 * @param eps
	 *            the rounding scale.
	 * @return the minimal Z value of the triangle, rounded to twice the
	 *         specified scale.
	 */
	public static final double minZ( final Vertices vertices, final long v0, final long v1, final long v2,
			final double eps )
	{
		final double z0 = mround( vertices.z( v0 ), eps, 2, 0 );
		final double z1 = mround( vertices.z( v1 ), eps, 2, 0 );
		final double z2 = mround( vertices.z( v2 ), eps, 2, 0 );
		return Math.min( z0, Math.min( z1, z2 ) );
	}

	/**
	 * Return the maximal Z value of a given triangle, rounded to twice the
	 * specified scale.
	 * 
	 * @param vertices
	 *            the vertices structure.
	 * @param v0
	 *            the triangle first vertex.
	 * @param v1
	 *            the triangle second vertex.
	 * @param v2
	 *            the triangle third vertex. Order does not matter.
	 * @param eps
	 *            the rounding scale.
	 * @return the maximal Z value of the triangle, rounded to twice the
	 *         specified scale.
	 */
	public static final double maxZ( final Vertices vertices, final long v0, final long v1, final long v2,
			final double eps )
	{
		final double z0 = mround( vertices.z( v0 ), eps, 2, 0 );
		final double z1 = mround( vertices.z( v1 ), eps, 2, 0 );
		final double z2 = mround( vertices.z( v2 ), eps, 2, 0 );
		return Math.max( z0, Math.max( z1, z2 ) );
	}

	/**
	 * Produces a string representation of a triangle.
	 * 
	 * @param mesh
	 *            the mesh.
	 * @param id
	 *            the if of the triangle in the mesh.
	 * @return a string representation.
	 */
	public static String triangleStr( final Mesh mesh, final long id )
	{
		final Triangles triangles = mesh.triangles();
		final long vertex0 = triangles.vertex0( id );
		final long vertex1 = triangles.vertex1( id );
		final long vertex2 = triangles.vertex2( id );

		final Vertices vertices = mesh.vertices();
		final double x0 = vertices.x( vertex0 );
		final double y0 = vertices.y( vertex0 );
		final double z0 = vertices.z( vertex0 );
		final double x1 = vertices.x( vertex1 );
		final double y1 = vertices.y( vertex1 );
		final double z1 = vertices.z( vertex1 );
		final double x2 = vertices.x( vertex2 );
		final double y2 = vertices.y( vertex2 );
		final double z2 = vertices.z( vertex2 );

		return String.format( "triangle %d: (%.2f, %.2f, %.2f) -> (%.2f, %.2f, %.2f) -> (%.2f, %.2f, %.2f) N = (%.2f, %.2f, %.2f)",
				id,
				x0, y0, z0,
				x1, y1, z1,
				x2, y2, z2,
				triangles.nx( id ), triangles.ny( id ), triangles.nz( id ) );
	}
}

