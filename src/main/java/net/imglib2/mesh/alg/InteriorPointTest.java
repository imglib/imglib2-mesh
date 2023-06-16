package net.imglib2.mesh.alg;

import static net.imglib2.mesh.util.MeshUtil.maxZ;
import static net.imglib2.mesh.util.MeshUtil.minZ;
import static net.imglib2.mesh.util.MeshUtil.mround;

import java.util.Arrays;

import gnu.trove.list.array.TDoubleArrayList;
import net.imglib2.RealPoint;
import net.imglib2.mesh.obj.Mesh;
import net.imglib2.mesh.obj.Triangles;
import net.imglib2.mesh.obj.Vertices;
import net.imglib2.mesh.util.SortArray;
import net.imglib2.mesh.util.SortBy;

public class InteriorPointTest
{

	/** Fraction of the specified scale to shift mesh vertices position. */
	private static final double EPS = 4e-4;

	/** Precision limit for the Moller-Trumbore algorithm. */
	private static final double EPSILON = 0.0000001;

	/** The X coordinate of the ray vector to cast. We go along positive X. */
	private static final double RX = 1.;

	/** The Y coordinate of the ray vector to cast. We go along positive X. */
	private static final double RY = 0.;

	/** The Z coordinate of the ray vector to cast. We go along positive X. */
	private static final double RZ = 0.;

	/**
	 * The array of the minimal Z position of all the triangles in the mesh,
	 * sorted.
	 */
	private final double[] minZs;

	/**
	 * The indices that map the {@link #minZs} value to the triangle index.
	 */
	private final int[] indexMin;

	/**
	 * The array of the maximal Z position of all the triangles in the mesh,
	 * unsorted.
	 */
	private final double[] maxZs;

	/**
	 * Tmp holder for Z min/max calculations.
	 */
	private final int[] indices;

	private final Mesh mesh;

	/** Tmp holder used in the Moller-Trumbore algorithm. */
	private final double[] tmp = new double[ 3 ];

	/** Holder to store X coords of intersection. */
	private final TDoubleArrayList xIntersect = new TDoubleArrayList();

	/**
	 * Tmp holder to store triangle intersection coordinates in Moller-Trumbore
	 * algorithm.
	 */
	private final double[] intersection;

	public InteriorPointTest( final Mesh mesh, final double scale )
	{
		this.mesh = mesh;

		/*
		 * Slice plane Z positions to odd multiples of eps.
		 */
		final double eps = EPS * scale;

		// Collect minZ & maxZ of vertices.
		final Triangles triangles = mesh.triangles();
		final Vertices vertices = mesh.vertices();

		this.minZs = new double[ ( int ) triangles.size() ];
		this.maxZs = new double[ ( int ) triangles.size() ];
		for ( int t = 0; t < triangles.size(); t++ )
		{
			final long v0 = triangles.vertex0( t );
			final long v1 = triangles.vertex1( t );
			final long v2 = triangles.vertex2( t );

			final double minZ = minZ( vertices, v0, v1, v2, eps );
			minZs[ t ] = minZ;
			final double maxZ = maxZ( vertices, v0, v1, v2, eps );
			maxZs[ t ] = maxZ;
		}
		this.indexMin = SortArray.quicksort( minZs );
		this.indices = new int[ indexMin.length ];
		this.intersection = new double[ 3 ];
	}

	public boolean isInside( final RealPoint p )
	{
		final double ox = p.getDoublePosition( 0 );
		final double oy = p.getDoublePosition( 1 );
		final double oz = mround( p.getDoublePosition( 2 ), EPS, 2, 1 );

		// All triangles with minZ < oz
		int k1 = Arrays.binarySearch( minZs, oz );
		if ( k1 < 0 )
			k1 = -( k1 + 1 );

		// Write these indices into an array (so that we do not change
		// minZs).
		System.arraycopy( indexMin, 0, indices, 0, k1 );

		/*
		 * All the triangles with id indices[0], ... indices[jmin-1] (jmin is
		 * not included, either equal or larger than zr) have a minZ below zr.
		 *
		 * Now: search among those triangles what ones have a maxZ larger than
		 * zr. We will sort indexMin[0 -> jmin] by maxZ.
		 */

		// Sort indices by the maxZ of the triangle they point to.
		SortBy.sortBy( indices, maxZs, 0, k1 - 1 );

		// Search the closest z.
		int k2 = SortBy.binarySearch( indices, maxZs, 0, k1, oz );
		if ( k2 < 0 )
			k2 = -( k2 + 1 );

		/*
		 * All the triangles with id indices[k2], ... indices[k1-1] have a minZ
		 * below z and a maxZ above z. Only them have to be considered for the
		 * test.
		 */

		final int start = k2;
		final int end = k1;
		xIntersect.resetQuick();
		for ( int i = start; i < end; i++ )
		{
			final int id = indices[ i ];
			final boolean intersects = rayIntersectsTriangle( id, ox, oy, oz, RX, RY, RZ, intersection );
			if ( intersects )
				xIntersect.add( intersection[ 0 ] );
		}

		// Sort intersection coords.
		xIntersect.sort();
		int nCross = 0;

		// Only consider intersections that change position.
		double previous = Double.NaN;
		for ( int i = 0; i < xIntersect.size(); i++ )
		{
			final double v = xIntersect.get( i );
			if ( v != previous )
				nCross++;

			previous = v;
		}

		// We are inside if we crossed an odd number of non-duplicate
		// intersections.
		return ( nCross % 2 ) != 0;
	}

	/**
	 * A version of the Moller-Trumbore algorithm, using the shifted position
	 * trick to avoid singularities.
	 * <p>
	 * Returns true if the specified ray intersects with the specified triangle
	 * in the mesh. Only one direction is considered.
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
	 * @return <code>true</code> if the ray intersects the triangle.
	 */
	private final boolean rayIntersectsTriangle( final long id, final double ox, final double oy, final double oz,
			final double rx, final double ry, final double rz, final double[] intersection )
	{
		final long vertex0 = mesh.triangles().vertex0( id );
		final long vertex1 = mesh.triangles().vertex1( id );
		final long vertex2 = mesh.triangles().vertex2( id );

		// Coords.
		final double x0 = mround( mesh.vertices().x( vertex0 ), EPS, 2, 0 );
		final double y0 = mround( mesh.vertices().y( vertex0 ), EPS, 2, 0 );
		final double z0 = mround( mesh.vertices().z( vertex0 ), EPS, 2, 0 );
		final double x1 = mround( mesh.vertices().x( vertex1 ), EPS, 2, 0 );
		final double y1 = mround( mesh.vertices().y( vertex1 ), EPS, 2, 0 );
		final double z1 = mround( mesh.vertices().z( vertex1 ), EPS, 2, 0 );
		final double x2 = mround( mesh.vertices().x( vertex2 ), EPS, 2, 0 );
		final double y2 = mround( mesh.vertices().y( vertex2 ), EPS, 2, 0 );
		final double z2 = mround( mesh.vertices().z( vertex2 ), EPS, 2, 0 );

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
