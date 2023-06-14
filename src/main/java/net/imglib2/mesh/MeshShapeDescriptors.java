package net.imglib2.mesh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import net.imglib2.mesh.alg.hull.ConvexHull;
import net.imglib2.mesh.obj.Mesh;
import net.imglib2.mesh.obj.Triangle;
import net.imglib2.mesh.obj.Triangles;
import net.imglib2.mesh.obj.Vertices;

/**
 * Static utilities that compute various shape descriptors of a mesh.
 */
public class MeshShapeDescriptors
{

	/**
	 * Computes the volume of the specified mesh.
	 *
	 * @return the volume in physical units.
	 */
	public static double volume( final Mesh mesh )
	{
		final Vertices vertices = mesh.vertices();
		final Triangles triangles = mesh.triangles();
		final long nTriangles = triangles.size();
		double sum = 0.;
		for ( long t = 0; t < nTriangles; t++ )
		{
			final long v1 = triangles.vertex0( t );
			final long v2 = triangles.vertex1( t );
			final long v3 = triangles.vertex2( t );

			final double x1 = vertices.x( v1 );
			final double y1 = vertices.y( v1 );
			final double z1 = vertices.z( v1 );
			final double x2 = vertices.x( v2 );
			final double y2 = vertices.y( v2 );
			final double z2 = vertices.z( v2 );
			final double x3 = vertices.x( v3 );
			final double y3 = vertices.y( v3 );
			final double z3 = vertices.z( v3 );

			final double v321 = x3 * y2 * z1;
			final double v231 = x2 * y3 * z1;
			final double v312 = x3 * y1 * z2;
			final double v132 = x1 * y3 * z2;
			final double v213 = x2 * y1 * z3;
			final double v123 = x1 * y2 * z3;

			sum += ( 1. / 6. ) * ( -v321 + v231 + v312 - v132 - v213 + v123 );
		}
		return Math.abs( sum );
	}

	/**
	 * Returns the surface area of a mesh.
	 * <p>
	 * It is computed as the sum of the are of the outwards-facing triangles.
	 * 
	 * @param mesh
	 *            the input mesh.
	 * @return the mesh surface area.
	 * @author Tim-Oliver Buchholz (University of Konstanz)
	 */
	public static double surfaceArea( final Mesh mesh )
	{
		double total = 0;
		for ( final Triangle tri : mesh.triangles() )
		{
			final Vector3D v0 = new Vector3D( tri.v0x(), tri.v0y(), tri.v0z() );
			final Vector3D v1 = new Vector3D( tri.v1x(), tri.v1y(), tri.v1z() );
			final Vector3D v2 = new Vector3D( tri.v2x(), tri.v2y(), tri.v2z() );

			final Vector3D cross = v0.subtract( v1 ).crossProduct( v2.subtract( v0 ) );
			final double norm = cross.getNorm();
			if ( norm > 0 )
				total += norm * 0.5;
		}
		return total;
	}

	/**
	 * Returns the solidity of a mesh.
	 * <p>
	 * The solidity is computed as the ratio between the volume of the mesh and
	 * the volume of its convex hull.
	 * 
	 * @param mesh
	 *            the input mesh.
	 * @return the mesh solidity value.
	 */
	public static double solidity( final Mesh mesh )
	{
		final Mesh ch = ConvexHull.calculate( mesh );
		return solidity( mesh, ch );
	}

	/**
	 * Returns the solidity of a mesh.
	 * <p>
	 * The solidity is computed as the ratio between the volume of the mesh and
	 * the volume of its convex hull.
	 * 
	 * @param mesh
	 *            the input mesh.
	 * @param convexHull
	 *            the convex hull of the input, if it has been pre-calculated.
	 * @return the mesh solidity value.
	 */
	public static double solidity( final Mesh mesh, final Mesh convexHull )
	{
		final double volume = volume( mesh );
		final double volumeCH = volume( convexHull );
		final double solidity = volume / volumeCH;
		return solidity;
	}

	/**
	 * Returns the convexity of a mesh.
	 * <p>
	 * The convexity is computed as the ratio between the surface area of the
	 * mesh and the surface area of its convex hull.
	 * 
	 * @param mesh
	 *            the input mesh.
	 * @return the mesh convexity value.
	 */
	public static double convexity( final Mesh mesh )
	{
		final Mesh ch = ConvexHull.calculate( mesh );
		return convexity( mesh, ch );
	}

	/**
	 * Returns the convexity of a mesh.
	 * <p>
	 * The convexity is computed as the ratio between the surface area of the
	 * convex hull and the surface area of the mesh.
	 * 
	 * @param mesh
	 *            the input mesh.
	 * @param convexHull
	 *            the convex hull of the input, if it has been pre-calculated.
	 * @return the mesh convexity value.
	 */
	public static double convexity( final Mesh mesh, final Mesh convexHull )
	{
		final double ra = surfaceArea( mesh );
		final double raCH = surfaceArea( convexHull );
		final double convexity = raCH / ra;
		return convexity;
	}

	/**
	 * Returns the sphericity of a mesh.
	 * <p>
	 * The sphericity is computed as the ratio between the surface area of a
	 * sphere with the same volume that of the input, and the the surface area
	 * of the input mesh.
	 * 
	 * @param mesh
	 *            the input mesh.
	 * @return the mesh sphericity value.
	 */
	public static double sphericity( final Mesh mesh )
	{
		final double volume = volume( mesh );
		final double sa = surfaceArea( mesh );
		final double sphereArea = Math.pow( Math.PI, 1. / 3. )
				* Math.pow( 6. * volume, 2. / 3. );
		final double sphericity = sphereArea / sa;
		return sphericity;
	}
}
