package net.imglib2.mesh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import net.imglib2.RealPoint;
import net.imglib2.mesh.alg.InertiaTensor;
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

	/**
	 * Returns the compactness of the mesh.
	 * 
	 * Based on: <a href=
	 * "http://www.sciencedirect.com/science/article/pii/S003132030700324X">Ernesto
	 * Bribiesca, An easy measure of compactness for 2D and 3D shapes, Pattern
	 * Recognition, Volume 41, Issue 2, 2008, Pages 543-554, ISSN 0031-3203,
	 * https://doi.org/10.1016/j.patcog.2007.06.029.</a>
	 * <p>
	 * In the paper compactness is defined as <code>area^3/volume^2</code>. For
	 * a sphere this is minimized and results in <code>36*PI</code>. To get
	 * values between (0,1] we use <code>(36*PI)/(area^3/volume^2)</code>.
	 * 
	 * @param mesh
	 *            the input mesh.
	 * @return the mesh compactness value.
	 * @author Tim-Oliver Buchholz (University of Konstanz)
	 */
	public static double compactness( final Mesh mesh )
	{
		final double sa = surfaceArea( mesh );
		final double s3 = sa * sa * sa;
		final double v = volume( mesh );
		final double v2 = v * v;
		final double c = s3 / v2;
		final double compactness = ( 36.0 * Math.PI ) / c;
		return compactness;
	}

	/**
	 * Returns the centroid of the mesh, that is the center of gravity of its
	 * volume.
	 * <p>
	 * (As a side note, {@link Meshes#center(Mesh)} returns the center of
	 * gravity of the mesh's surface.)
	 *
	 * @author Tim-Oliver Buchholz (University of Konstanz)
	 * @param input
	 *            the input mesh.
	 * @return the centroid of the mesh.
	 * @see <a href="http://wwwf.imperial.ac.uk/~rn/centroid.pdf">Calculating
	 *      the volume and centroid of a polyhedron in 3d</a>.
	 */
	public static RealPoint centroid( final Mesh input )
	{
		double cX = 0.;
		double cY = 0.;
		double cZ = 0.;
		for ( int i = 0; i < input.triangles().size(); i++ )
		{
			final long v0 = input.triangles().vertex0( i );
			final long v1 = input.triangles().vertex1( i );
			final long v2 = input.triangles().vertex2( i );

			final double nx = input.triangles().nx( i );
			final double ny = input.triangles().ny( i );
			final double nz = input.triangles().nz( i );

			final double v0x = input.vertices().x( v0 );
			final double v0y = input.vertices().y( v0 );
			final double v0z = input.vertices().z( v0 );
			final double v1x = input.vertices().x( v1 );
			final double v1y = input.vertices().y( v1 );
			final double v1z = input.vertices().z( v1 );
			final double v2x = input.vertices().x( v2 );
			final double v2y = input.vertices().y( v2 );
			final double v2z = input.vertices().z( v2 );

			cX += ( 1 / 24. ) * nx * ( Math.pow( ( v0x + v1x ), 2 )
					+ Math.pow( ( v1x + v2x ), 2 )
					+ Math.pow( ( v2x + v0x ), 2 ) );
			cY += ( 1 / 24. ) * ny * ( Math.pow( ( v0y + v1y ), 2 )
					+ Math.pow( ( v1y + v2y ), 2 )
					+ Math.pow( ( v2y + v0y ), 2 ) );
			cZ += ( 1 / 24. ) * nz * ( Math.pow( ( v0z + v1z ), 2 )
					+ Math.pow( ( v1z + v2z ), 2 )
					+ Math.pow( ( v2z + v0z ), 2 ) );
		}

		final double v = volume( input );
		final double d = 1 / ( 2 * v );
		cX *= d;
		cY *= d;
		cZ *= d;

		return new RealPoint( -cX, -cY, -cZ );
	}

	/**
	 * Returns the sparseness of an object.
	 * <p>
	 * The sparseness is calculated as the ratio between the volume of the mesh
	 * and the volume of the ellipsoid fitted to the mesh inertia tensor. Values
	 * close to 1 indicate a compact object, and lower values indicate a sparser
	 * one.
	 * 
	 * @param input
	 *            the input mesh.
	 * @return the mesh sparseness value.
	 * @author Tim-Oliver Buchholz (University of Konstanz)
	 */
	public static double sparseness( final Mesh input )
	{
		/*
		 * This code from Timo is probably based on Thomas Boudier 3D Suite
		 * plugin, itself inspired from BoneJ:
		 * https://forum.image.sc/t/3d-shape-plugin/24880/11
		 */
		final RealMatrix it = InertiaTensor.calculate( input );
		final EigenDecomposition ed = new EigenDecomposition( it );

		final double l1 = ed.getRealEigenvalue( 0 ) - ed.getRealEigenvalue( 2 ) + ed.getRealEigenvalue( 1 );
		final double l2 = ed.getRealEigenvalue( 0 ) - ed.getRealEigenvalue( 1 ) + ed.getRealEigenvalue( 2 );
		final double l3 = ed.getRealEigenvalue( 2 ) - ed.getRealEigenvalue( 0 ) + ed.getRealEigenvalue( 1 );

		final double g = 1. / ( 8. * Math.PI / 15. );

		final double a = Math.pow( g * l1 * l1 / Math.sqrt( l2 * l3 ), 1 / 5. );
		final double b = Math.pow( g * l2 * l2 / Math.sqrt( l1 * l3 ), 1 / 5. );
		final double c = Math.pow( g * l3 * l3 / Math.sqrt( l1 * l2 ), 1 / 5. );

		final double volumeEllipsoid = ( 4. / 3. * Math.PI * a * b * c );
		final double volume = volume( input );

		final double sparseness = volume / volumeEllipsoid;
		return sparseness;
	}
}
