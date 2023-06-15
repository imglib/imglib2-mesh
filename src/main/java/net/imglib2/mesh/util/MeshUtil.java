package net.imglib2.mesh.util;

import net.imglib2.RealInterval;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.obj.Vertices;
import net.imglib2.util.Intervals;

public class MeshUtil
{

	/**
	 * Convenience utility to map the <code>float[]</code> array returned by
	 * {@link Meshes#boundingBox(net.imglib2.mesh.obj.Mesh)} to an ImgLib2
	 * {@link RealInterval}.
	 * 
	 * @param boundingBox
	 *            a <code>float[]</code> array with 6 elements.
	 * @return a new 3D real interval.
	 */
	public static final RealInterval toRealInterval( final float[] boundingBox )
	{
		return Intervals.createMinMaxReal( boundingBox[ 0 ], boundingBox[ 1 ], boundingBox[ 2 ], boundingBox[ 3 ], boundingBox[ 4 ], boundingBox[ 5 ] );
	}

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
}

