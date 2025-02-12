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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.alg.hull.ConvexHull;
import net.imglib2.util.Util;

/**
 * Fit an ellipsoid to the convex-Hull of a 3D mesh.
 * <p>
 * Adapted from Yury Petrov's <a href=
 * "https://mathworks.com/matlabcentral/fileexchange/24693-ellipsoid-fit">Ellipsoid
 * Fit MATLAB function</a> and KalebKE <a href=
 * "https://github.com/KalebKE/EllipsoidFit/blob/master/EllipsoidFit/src/ellipsoidFit/FitPoints.java">ellipsoidfit</a>.
 * 
 * @author Jean-Yves Tinevez
 */
public class EllipsoidFitter
{

	/**
	 * The results of fitting an ellipsoid to a mesh or a collection of points.
	 */
	public static final class Ellipsoid
	{
		/** The ellipsoid center. */
		public final RealLocalizable center;

		/** The eigenvector of the smallest axis of the ellipsoid. */
		public final RealLocalizable ev1;

		/** The eigenvector of the middle axis of the ellipsoid. */
		public final RealLocalizable ev2;

		/** The eigenvector of the largest axis of the ellipsoid. */
		public final RealLocalizable ev3;

		/** The radius of the smallest axis of the ellipsoid. */
		public final double r1;

		/** The radius of the middle axis of the ellipsoid. */
		public final double r2;

		/** The radius of the largest axis of the ellipsoid. */
		public final double r3;

		private Ellipsoid( final RealLocalizable center,
				final RealLocalizable ev1,
				final RealLocalizable ev2,
				final RealLocalizable ev3,
				final double r1,
				final double r2,
				final double r3 )
		{
			this.center = center;
			this.ev1 = ev1;
			this.ev2 = ev2;
			this.ev3 = ev3;
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
		}

		@Override
		public String toString()
		{
			final StringBuilder str = new StringBuilder( super.toString() );
			str.append( "\n - center: " + Util.printCoordinates( center ) );
			str.append( String.format( "\n - axis 1: radius = %.2f, vector = %s", r1, ev1 ) );
			str.append( String.format( "\n - axis 2: radius = %.2f, vector = %s", r2, ev2 ) );
			str.append( String.format( "\n - axis 3: radius = %.2f, vector = %s", r3, ev3 ) );
			return str.toString();
		}
	}

	/**
	 * Fit an ellipsoid to the convex-Hull of a 3D mesh.
	 * 
	 * @param mesh
	 *            the mesh to fit.
	 * @return the fit results.
	 */
	public static final Ellipsoid fit( final Mesh mesh )
	{
		final Mesh ch = ConvexHull.calculate( mesh );
		return fitOnConvexHull( ch );
	}

	/**
	 * Fit an ellipsoid to a 3D mesh, assuming it is the convex-Hull.
	 * 
	 * @param mesh
	 *            the convex-Hull of the mesh to fit.
	 * @return the fit results.
	 */
	public static final Ellipsoid fitOnConvexHull( final Mesh mesh )
	{
		return fit( mesh.vertices(), ( int ) mesh.vertices().size() );
	}

	/**
	 * Fit an ellipsoid to a collection of 3D points.
	 * 
	 * @param points
	 *            an iterable over the points to fit.
	 * @param nPoints
	 *            the number of points to include in the fit. The fit will
	 *            consider at most the first nPoints of the iterable, or all the
	 *            points in the iterable, whatever comes first.
	 * @return the fit results.
	 */
	public static Ellipsoid fit( final Iterable< ? extends RealLocalizable > points, final int nPoints )
	{
		final RealVector V = solve( points, nPoints );

		// To algebraix form.
		final RealMatrix A = toAlgebraicForm( V );

		// Find the center of the ellipsoid.
		final RealVector C = findCenter( A );

		// Translate the algebraic form of the ellipsoid to the center.
		final RealMatrix R = translateToCenter( C, A );

		// Ellipsoid eigenvectors and eigenvalues.
		final Ellipsoid fit = getFit( R, C );
		return fit;
	}

	private static Ellipsoid getFit( final RealMatrix R, final RealVector C )
	{
		final RealMatrix subr = R.getSubMatrix( 0, 2, 0, 2 );

		// subr[i][j] = subr[i][j] / -r[3][3]).
		final double divr = -R.getEntry( 3, 3 );
		for ( int i = 0; i < subr.getRowDimension(); i++ )
			for ( int j = 0; j < subr.getRowDimension(); j++ )
				subr.setEntry( i, j, subr.getEntry( i, j ) / divr );

		// Get the eigenvalues and eigenvectors.
		final EigenDecomposition ed = new EigenDecomposition( subr );
		final double[] eigenvalues = ed.getRealEigenvalues();
		final RealVector e1 = ed.getEigenvector( 0 );
		final RealVector e2 = ed.getEigenvector( 1 );
		final RealVector e3 = ed.getEigenvector( 2 );

		// Semi-axis length (radius).
		final RealVector SAL = new ArrayRealVector( eigenvalues.length );
		for ( int i = 0; i < eigenvalues.length; i++ )
			SAL.setEntry( i, Math.sqrt( 1. / eigenvalues[ i ] ) );

		// Put everything in a fit object.
		final RealPoint center = new RealPoint( C.getEntry( 0 ), C.getEntry( 1 ), C.getEntry( 2 ) );
		final RealPoint ev1 = new RealPoint( e1.getEntry( 0 ), e1.getEntry( 1 ), e1.getEntry( 2 ) );
		final RealPoint ev2 = new RealPoint( e2.getEntry( 0 ), e2.getEntry( 1 ), e2.getEntry( 2 ) );
		final RealPoint ev3 = new RealPoint( e3.getEntry( 0 ), e3.getEntry( 1 ), e3.getEntry( 2 ) );
		return new Ellipsoid( center, ev1, ev2, ev3, SAL.getEntry( 0 ), SAL.getEntry( 1 ), SAL.getEntry( 2 ) );
	}

	/**
	 * Translate the algebraic form of the ellipsoid to the center.
	 * 
	 * @param C
	 *            the center of the ellipsoid.
	 * @param A
	 *            the ellipsoid matrix.
	 * @return the center translated form of the algebraic ellipsoid.
	 */
	private static final RealMatrix translateToCenter( final RealVector C, final RealMatrix A )
	{
		final RealMatrix T = MatrixUtils.createRealIdentityMatrix( 4 );
		final RealMatrix centerMatrix = new Array2DRowRealMatrix( 1, 3 );
		centerMatrix.setRowVector( 0, C );
		T.setSubMatrix( centerMatrix.getData(), 3, 0 );
		final RealMatrix R = T.multiply( A ).multiply( T.transpose() );
		return R;
	}

	/**
	 * Find the center of the ellipsoid.
	 * 
	 * @param a
	 *            the algebraic from of the polynomial.
	 * @return a vector containing the center of the ellipsoid.
	 */
	private static final RealVector findCenter( final RealMatrix A )
	{
		final RealMatrix subA = A.getSubMatrix( 0, 2, 0, 2 );

		for ( int q = 0; q < subA.getRowDimension(); q++ )
			for ( int s = 0; s < subA.getColumnDimension(); s++ )
				subA.multiplyEntry( q, s, -1.0 );

		final RealVector subV = A.getRowVector( 3 ).getSubVector( 0, 3 );

		final DecompositionSolver solver = new SingularValueDecomposition( subA ).getSolver();
		final RealMatrix subAi = solver.getInverse();
		return subAi.operate( subV );
	}

	/**
	 * Solve for <code>Ax^2 + By^2 + Cz^2 + 2Dxy + 2Exz + 2Fyz + 2Gx + 2Hy +
	 * 2Iz = 1</code>.
	 * 
	 * @param points
	 *            an iterable over 3D points.
	 * @param nPoints
	 *            the number of points in the iterable.
	 * @return
	 */
	private static final RealVector solve( final Iterable< ? extends RealLocalizable > points, final int nPoints )
	{
		final RealMatrix M0 = new Array2DRowRealMatrix( nPoints, 9 );
		int i = 0;
		for ( final RealLocalizable point : points )
		{
			final double x = point.getDoublePosition( 0 );
			final double y = point.getDoublePosition( 1 );
			final double z = point.getDoublePosition( 2 );

			final double xx = x * x;
			final double yy = y * y;
			final double zz = z * z;

			final double xy = 2. * x * y;
			final double xz = 2. * x * z;
			final double yz = 2. * y * z;

			M0.setEntry( i, 0, xx );
			M0.setEntry( i, 1, yy );
			M0.setEntry( i, 2, zz );
			M0.setEntry( i, 3, xy );
			M0.setEntry( i, 4, xz );
			M0.setEntry( i, 5, yz );
			M0.setEntry( i, 6, 2. * x );
			M0.setEntry( i, 7, 2. * y );
			M0.setEntry( i, 8, 2. * z );

			i++;
			if ( i >= nPoints )
				break;
		}
		final RealMatrix M;
		if ( i == nPoints )
			M = M0;
		else
			M = M0.getSubMatrix( 0, i, 0, 9 );

		final RealMatrix M2 = M.transpose().multiply( M );

		final RealVector O = new ArrayRealVector( nPoints );
		O.mapAddToSelf( 1 );

		final RealVector MO = M.transpose().operate( O );

		final DecompositionSolver solver = new SingularValueDecomposition( M2 ).getSolver();
		final RealMatrix I = solver.getInverse();

		final RealVector V = I.operate( MO );
		return V;
	}

	/**
	 * Reshape the fit result vector in the shape of an algebraic matrix.
	 * 
	 * <pre>
	 * A = 		[ Ax<sup>2</sup> 	2Dxy 	2Exz 	2Gx ] 
	 * 		[ 2Dxy 	By<sup>2</sup> 	2Fyz 	2Hy ] 
	 * 		[ 2Exz 	2Fyz 	Cz<sup>2</sup> 	2Iz ] 
	 * 		[ 2Gx 	2Hy 	2Iz 	-1 ] ]
	 * <pre>
	 * 
	 * @param V the fit result.
	 * @return a new 4x4 real matrix.
	 */
	private static final RealMatrix toAlgebraicForm( final RealVector V )
	{
		final RealMatrix A = new Array2DRowRealMatrix( 4, 4 );

		A.setEntry( 0, 0, V.getEntry( 0 ) );
		A.setEntry( 0, 1, V.getEntry( 3 ) );
		A.setEntry( 0, 2, V.getEntry( 4 ) );
		A.setEntry( 0, 3, V.getEntry( 6 ) );
		A.setEntry( 1, 0, V.getEntry( 3 ) );
		A.setEntry( 1, 1, V.getEntry( 1 ) );
		A.setEntry( 1, 2, V.getEntry( 5 ) );
		A.setEntry( 1, 3, V.getEntry( 7 ) );
		A.setEntry( 2, 0, V.getEntry( 4 ) );
		A.setEntry( 2, 1, V.getEntry( 5 ) );
		A.setEntry( 2, 2, V.getEntry( 2 ) );
		A.setEntry( 2, 3, V.getEntry( 8 ) );
		A.setEntry( 3, 0, V.getEntry( 6 ) );
		A.setEntry( 3, 1, V.getEntry( 7 ) );
		A.setEntry( 3, 2, V.getEntry( 8 ) );
		A.setEntry( 3, 3, -1 );
		return A;
	}
}
