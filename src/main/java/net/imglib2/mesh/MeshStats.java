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
package net.imglib2.mesh;

import net.imglib2.mesh.alg.InertiaTensor;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import net.imglib2.RealPoint;
import net.imglib2.mesh.alg.hull.ConvexHull;
import net.imglib2.mesh.util.MeshUtil;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Static utilities that compute various shape descriptors of a mesh.
 */
public class MeshStats
{

	/**
	 * Computes the volume of the specified mesh.
	 *
	 * @param mesh the input {@link Mesh}
	 * @return the volume in physical units.
	 * @implNote op names='geom.size', label='Geometric (3D): Volume',
	 *           priority='9999.'
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
	 * Computes the volume of the convex hull of the specified mesh.
	 *
	 * @param mesh a {@link Mesh}
	 * @return the volume in physical units.
	 * @implNote op names='geom.sizeConvexHull', label='Geometric (3D): Convex Hull Volume',
	 *           priority='9999.'
	 */
	public static double convexHullVolume(final Mesh mesh) {
		return volume(ConvexHull.calculate(mesh));
	}

	/**
	 * Returns the surface area of a mesh.
	 * <p>
	 * It is computed as the sum of the area of the outwards-facing triangles.
	 * 
	 * @param mesh
	 *            the input mesh.
	 * @return the mesh surface area.
	 * @author Tim-Oliver Buchholz (University of Konstanz)
	 * @implNote op names='geom.boundarySize', label='Geometric (3D): Surface Area',
	 *           priority='10000.'
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
	 * Computes the surface area of the convex hull of the specified mesh.
	 *
	 * @param mesh a {@link Mesh}
	 * @return the surface area in physical units.
	 * @implNote op names='geom.boundarySizeConvexHull', label='Geometric (3D): Convex Hull Surface Area',
	 *           priority='9999.'
	 */
	public static double convexHullSurfaceArea(final Mesh mesh) {
		return surfaceArea(ConvexHull.calculate(mesh));
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
	 * @implNote op names='geom.solidity', label='Geometric (3D): Solidity',
	 *           priority='10000.'
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
	 * @implNote op names='geom.solidity', label='Geometric (3D): Solidity',
	 *           priority='10000.'
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
	 * @implNote op names='geom.convexity', label='Geometric (3D): Convexity',
	 *           priority='10000.'
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
	 * @implNote op names='geom.convexity', label='Geometric (3D): Convexity',
	 *           priority='10000.'
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
	 * @implNote op names='geom.sphericity', label='Geometric (3D): Sphericity',
	 *           priority='10000.'
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
	 * Based on: <a href= "https://doi.org/10.1016/j.patcog.2007.06.029">Ernesto
	 * Bribiesca, An easy measure of compactness for 2D and 3D shapes, Pattern
	 * Recognition, Volume 41, Issue 2, 2008, Pages 543-554, ISSN 0031-3203</a>
	 * <p>
	 * In the paper compactness is defined as <code>area^3/volume^2</code>. For
	 * a sphere this is minimized and results in <code>36*PI</code>. To get
	 * values between (0,1] we use <code>(36*PI)/(area^3/volume^2)</code>.
	 * 
	 * @param mesh
	 *            the input mesh.
	 * @return the mesh compactness value.
	 * @author Tim-Oliver Buchholz (University of Konstanz)
	 * @implNote op names='geom.compactness', label='Geometric (3D): Compactness',
	 *           priority='10000.'
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
	 * @author Jean-Yves Tinevez
	 * @param input
	 *            the input mesh.
	 * @return the centroid of the mesh.
	 * @implNote op names='geom.centroid', priority='10000.'
	 */
	public static RealPoint centroid( final Mesh input )
	{
		// Variable names from moment definition.
		double m100 = 0.;
		double m010 = 0.;
		double m001 = 0.;
		final double[] normals = new double[ 3 ];
		for ( int i = 0; i < input.triangles().size(); i++ )
		{
			final long v0 = input.triangles().vertex0( i );
			final long v1 = input.triangles().vertex1( i );
			final long v2 = input.triangles().vertex2( i );

			final double x1 = input.vertices().x( v0 );
			final double y1 = input.vertices().y( v0 );
			final double z1 = input.vertices().z( v0 );
			final double x2 = input.vertices().x( v1 );
			final double y2 = input.vertices().y( v1 );
			final double z2 = input.vertices().z( v1 );
			final double x3 = input.vertices().x( v2 );
			final double y3 = input.vertices().y( v2 );
			final double z3 = input.vertices().z( v2 );

			// Non-normalized normals.
			final double d21x = x2 - x1;
			final double d21y = y2 - y1;
			final double d21z = z2 - z1;
			final double d31x = x3 - x1;
			final double d31y = y3 - y1;
			final double d31z = z3 - z1;
			MeshUtil.cross( d21x, d21y, d21z, d31x, d31y, d31z, normals );
			final double nx = normals[ 0 ];
			final double ny = normals[ 1 ];
			final double nz = normals[ 2 ];

			final double xx = ( ( x1 + x2 ) * ( x2 + x3 ) + x1 * x1 + x3 * x3 ) / 12.;
			final double yy = ( ( y1 + y2 ) * ( y2 + y3 ) + y1 * y1 + y3 * y3 ) / 12.;
			final double zz = ( ( z1 + z2 ) * ( z2 + z3 ) + z1 * z1 + z3 * z3 ) / 12.;
			
			final double xy = ( ( x1 + x2 + x3 ) * ( y1 + y2 + y3 ) + x1 * y1 + x2 * y2 + x3 * y3 ) / 24.;
			final double xz = ( ( x1 + x2 + x3 ) * ( z1 + z2 + z3 ) + x1 * z1 + x2 * z2 + x3 * z3 ) / 24.;
			final double yz = ( ( y1 + y2 + y3 ) * ( z1 + z2 + z3 ) + y1 * z1 + y2 * z2 + y3 * z3 ) / 24.;

			final double dm100 = ( xx * nx + 2. * xy * ny + 2. * xz * nz ) / 6.;
			final double dm010 = ( 2. * xy * nx + yy * ny + 2. * yz * nz ) / 6.;
			final double dm001 = ( 2. * xz * nx + 2. * yz * ny + zz * nz ) / 6.;

			m100 += dm100;
			m010 += dm010;
			m001 += dm001;
		}

		final double v = volume( input );
		return new RealPoint( m100 / v, m010 / v, m001 / v );
	}

	/**
	 * Describes the elogation of a {@link Mesh}. As elongation is a metric of a
	 * two-dimensional object, this functionality describes elongation as a
	 * {@link RealMatrix}, where each index {@code x,y} is the elongation
	 * considering principal axes {@code x} and {@code y}.
	 *
	 * @param input a {@link Mesh}
	 * @return the elongation of each subset of principal axes of {@code input}
	 * @implNote op names='geom.elongation', label='Geometric (3D): Elongation', priority='10000.'
	 */
	public static RealMatrix elongation(final Mesh input) {
		final RealMatrix it = InertiaTensor.calculate(input);
		final EigenDecomposition ed = new EigenDecomposition(it);

		final double l1 = ed.getRealEigenvalue(0) - ed.getRealEigenvalue(2) + ed
				.getRealEigenvalue(1);
		final double l2 = ed.getRealEigenvalue(0) - ed.getRealEigenvalue(1) + ed
				.getRealEigenvalue(2);
		final double l3 = ed.getRealEigenvalue(2) - ed.getRealEigenvalue(0) + ed
				.getRealEigenvalue(1);

		final double g = 1 / (8 * Math.PI / 15);

		final double a = Math.pow(g * l1 * l1 / Math.sqrt(l2 * l3), 1 / 5d);
		final double b = Math.pow(g * l2 * l2 / Math.sqrt(l1 * l3), 1 / 5d);
		final double c = Math.pow(g * l3 * l3 / Math.sqrt(l1 * l2), 1 / 5d);
		final BlockRealMatrix out = new BlockRealMatrix(3, 3);
		out.setRow(0, new double[] {elongation(a, a), elongation(a, b), elongation(a, c)});
		out.setRow(1, new double[] {elongation(b, a), elongation(b, b), elongation(b, c)});
		out.setRow(2, new double[] {elongation(c, a), elongation(c, b), elongation(c, c)});
		return out;
	}

	private static double elongation(double major, double minor) {
		// Ensure elongation contained in [0, 1]
		if (minor > major) {
			double tmp = minor;
			minor = major;
			major = tmp;
		}
		return 1 - (minor / major);
	}

	/**
	 * Describes the elongation of the two <b>larger</b> principle axes of the
	 * minimum box containing a {@link Mesh}. Provides functionality equivalent
	 * to the {@code "geom.mainElongation"} Op of ImageJ Ops.
	 *
	 * @param input a {@link Mesh}
	 * @return the elongation of a cross-section containing the two <b>larger</b> principle axes
	 * @implNote op names='geom.mainElongation', label='Geometric (3D): Main Elongation', priority='10000.'
	 */
	public static double mainElongation(final Mesh input){
		return elongation(input).getEntry(0, 1);
	}

	/**
	 * Describes the elongation of the two <b>smaller</b> principle axes of the
	 * minimum box containing a {@link Mesh}. Provides functionality equivalent
	 * to the {@code "geom.medianElongation"} Op of ImageJ Ops.
	 *
	 * @param input a {@link Mesh}
	 * @return the elongation of a cross-section containing the two <b>smaller</b> principle axes
	 * @implNote op names='geom.medianElongation', label='Geometric (3D): Median Elongation', priority='10000.'
	 */
	public static double medianElongation(final Mesh input) {
		return elongation(input).getEntry(1, 2);
	}

	/**
	 * Describes the number of vertices on the surface of a {@link Mesh}.
	 *
	 * @param input a {@link Mesh}
	 * @return the number of vertices in {@code input}
	 * @implNote op names='geom.verticesCount', label='Geometric (3D): Surface Vertices Count', priority='10000.'
	 */
	public static long verticesCount(final Mesh input) {
		return input.vertices().size();
	}

	/**
	 * Describes the number of vertices on the surface of the convex hull of {@link Mesh}.
	 *
	 * @param input a {@link Mesh}
	 * @return the number of vertices in the convex hull of {@code input}
	 * @implNote op names='geom.verticesCountConvexHull', label='Geometric (3D): Convex Hull Vertices Count', priority='10000.'
	 */
	public static long convexHullVerticesCount(final Mesh input) {
		return ConvexHull.calculate(input).vertices().size();
	}


	/**
	 * Describes the spareness of a {@link Mesh}. Spareness is defined as the ratio of the {@code Mesh}'s volume to that of a best-fit ellipsoid.
	 * Inspiration drawn from ImageJ.
	 *
	 * @param input the input {@link Mesh}
	 * @return the ellipse variance
	 * @author Tim-Oliver Buchholz (University of Konstanz)
	 * @implNote op names='geom.spareness', label='Geometric (3D): Spareness',
	 *           priority='10000.'
	 */
	public static double spareness(final Mesh input) {
		final RealMatrix it = InertiaTensor.calculate(input);
		final EigenDecomposition ed = new EigenDecomposition(it);

		final double l1 = ed.getRealEigenvalue(0) - ed.getRealEigenvalue(2) + ed
				.getRealEigenvalue(1);
		final double l2 = ed.getRealEigenvalue(0) - ed.getRealEigenvalue(1) + ed
				.getRealEigenvalue(2);
		final double l3 = ed.getRealEigenvalue(2) - ed.getRealEigenvalue(0) + ed
				.getRealEigenvalue(1);

		final double g = 1 / (8 * Math.PI / 15);

		final double a = Math.pow(g * l1 * l1 / Math.sqrt(l2 * l3), 1 / 5d);
		final double b = Math.pow(g * l2 * l2 / Math.sqrt(l1 * l3), 1 / 5d);
		final double c = Math.pow(g * l3 * l3 / Math.sqrt(l1 * l2), 1 / 5d);

		double volumeEllipsoid = (4 / 3d * Math.PI * a * b * c);

		return volume(input) / volumeEllipsoid;
	}

	private MeshStats()
	{}
}
