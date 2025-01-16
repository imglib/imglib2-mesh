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

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Triangles;
import net.imglib2.mesh.Vertices;

/**
 * Möller–Trumbore intersection algorithm.
 * <p>
 * This algorithm can efficiently tells whether a ray intersects with a triangle
 * in a mesh. Adapted from Wikipedia.
 *
 * @see <a href="https://en.wikipedia.org/wiki/M%C3%B6ller%E2%80%93Trumbore_intersection_algorithm">Möller–Trumbore intersection algorithm on Wikipedia</a>.
 * @author Jean-Yves Tinevez
 *
 */
public class MollerTrumbore
{

	private static final double EPSILON = 0.0000001;

	private final Vertices vertices;

	private final Triangles triangles;

	private final double[] tmp;

	public MollerTrumbore( final Mesh mesh )
	{
		this.vertices = mesh.vertices();
		this.triangles = mesh.triangles();
		this.tmp = new double[ 3 ];
	}

	/**
	 * Returns true if the specified ray intersects with the specified triangle
	 * in the mesh, and if yes, store the position of intersection in an array.
	 * The intersection is calculated for ray that originates from the point.
	 * Only one direction is considered.
	 * 
	 * @param id
	 *            the triangle to test.
	 * @param ox
	 *            the X position of the ray origin.
	 * @param oy
	 *            the Y position of the ray origin.
	 * @param oz
	 *            the Z position of the ray origin.
	 * @param rx
	 *            the X coordinate of the ray vector.
	 * @param ry
	 *            the Y coordinate of the ray vector.
	 * @param rz
	 *            the Z coordinate of the ray vector.
	 * @param intersection
	 *            a <code>double[]</code> array of at least 3 elements to store
	 *            the intersection position. Is not modified if the ray does not
	 *            intersect the triangle.
	 * @return <code>true</code> if the ray intersects the triangle.
	 */
	public boolean rayIntersectsTriangle( final long id, final double ox, final double oy, final double oz,
			final double rx, final double ry, final double rz, final double[] intersection )
	{
		final long vertex0 = triangles.vertex0( id );
		final long vertex1 = triangles.vertex1( id );
		final long vertex2 = triangles.vertex2( id );

		// Coords.
		final double x0 = vertices.x( vertex0 );
		final double y0 = vertices.y( vertex0 );
		final double z0 = vertices.z( vertex0 );
		final double x1 = vertices.x( vertex1 );
		final double y1 = vertices.y( vertex1 );
		final double z1 = vertices.z( vertex1 );
		final double x2 = vertices.x( vertex2 );
		final double y2 = vertices.y( vertex2 );
		final double z2 = vertices.z( vertex2 );

		// Edge 1
		final double e1x = x1 - x0;
		final double e1y = y1 - y0;
		final double e1z = z1 - z0;
		// Edge 2
		final double e2x = x2 - x0;
		final double e2y = y2 - y0;
		final double e2z = z2 - z0;

		cross( rx, ry, rz, e2x, e2y, e2z, tmp );
		final double hx = tmp[ 0 ];
		final double hy = tmp[ 1 ];
		final double hz = tmp[ 2 ];
		final double a = dot( e1x, e1y, e1z, hx, hy, hz );
		if ( a > -EPSILON && a < EPSILON )
			return false; // This ray is parallel to this triangle.

		final double sx = ox - x0;
		final double sy = oy - y0;
		final double sz = oz - z0;
		final double f = 1. / a;
		final double u = f * dot( sx, sy, sz, hx, hy, hz );

		if ( u < 0. || u > 1. )
			return false;

		cross( sx, sy, sz, e1x, e1y, e1z, tmp );
		final double qx = tmp[ 0 ];
		final double qy = tmp[ 1 ];
		final double qz = tmp[ 2 ];

		final double v = f * dot( rx, ry, rz, qx, qy, qz );

		if ( v < 0. || u + v > 1. )
			return false;

		final double t = f * dot( e2x, e2y, e2z, qx, qy, qz );
		// We have an infinite line intersection.
		if ( t < EPSILON )
			return false;

		intersection[ 0 ] = ox + t * rx;
		intersection[ 1 ] = oy + t * ry;
		intersection[ 2 ] = oy + t * rz;
		return true;
	}

	private double dot( final double x1, final double y1, final double z1, final double x2, final double y2,
			final double z2 )
	{
		return x1 * x2 + y1 * y2 + z1 * z2;
	}

	private void cross( final double x1, final double y1, final double z1, final double x2, final double y2,
			final double z2, final double[] out )
	{
		out[ 0 ] = y1 * z2 - z1 * y2;
		out[ 1 ] = -x1 * z2 + z1 * x2;
		out[ 2 ] = x1 * y2 - y1 * x2;
	}
}
