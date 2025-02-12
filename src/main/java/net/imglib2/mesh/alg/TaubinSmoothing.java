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

import java.util.Arrays;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.Triangles;
import net.imglib2.mesh.Vertices;
import net.imglib2.mesh.impl.nio.BufferMesh;

/**
 * Taubin's mesh smoothing algorithm.
 * <p>
 * Adapted by the Javascript code of mykolalysenko, MIT license.
 * https://github.com/mikolalysenko/taubin-smooth/blob/master/smooth.js
 *
 * @author Jean-Yves Tinevez
 * @see <a href="https://doi.org/10.1109/ICCV.1995.466848">Taubin, G. “Curve and
 *      Surface Smoothing without Shrinkage.” In Proceedings of IEEE
 *      International Conference on Computer Vision, 852–57, 1995.</a>
 *
 */
public class TaubinSmoothing
{

	/**
	 * Smooth the specified mesh using the Taubin smoothing algorithm, with
	 * default parameters.
	 *
	 * @param mesh
	 *            the mesh to smooth.
	 * @return a new smoothed mesh.
	 */
	public static final BufferMesh smooth( final Mesh mesh )
	{
		return smooth( mesh, 10, 0.1 );
	}

	/**
	 * Smooth the specified mesh using the Taubin smoothing algorithm, using
	 * cotangent weights.
	 *
	 * @param mesh
	 *            the mesh to smooth.
	 * @param iters
	 *            the number of iterations to apply. Typical values: 10.
	 * @param passBand
	 *            the spatial frequency to use for smoothing. For instance 0.1.
	 * @return a new smoothed mesh.
	 */
	public static final BufferMesh smooth( final Mesh mesh, final int iters, final double passBand )
	{
		final double A = -1.;
		final double B = passBand;
		final double C = 2.;

		final double discr = Math.sqrt( B * B - 4. * A * C );
		final double r0 = ( -B + discr ) / ( 2. * A * C );
		final double r1 = ( -B - discr ) / ( 2. * A * C );

		final double lambda = Math.max( r0, r1 );
		final double mu = Math.min( r0, r1 );

		return smooth( mesh, iters, lambda, mu, TaubinWeightType.COTANGENT );
	}

	/**
	 * Smooth the specified mesh using the Taubin smoothing algorithm.
	 *
	 * @param mesh
	 *            the mesh to smooth.
	 * @param iters
	 *            the number of iterations to apply. Typical values: 10.
	 * @param lambda
	 *            the value of the lambda step in Taubin smoothing. Positive,
	 *            typical values are around 0.5;
	 * @param mu
	 *            the value of the mu step in Taubin smoothing. Negative,
	 *            typical values are around -0.5.
	 * @param weightType
	 *            the type of weights to use when summing contribution from
	 *            neighbor edges.
	 * @return a new smoothed mesh.
	 */
	public static final BufferMesh smooth( final Mesh mesh, final int iters, final double lambda, final double mu,
			final TaubinWeightType weightType )
	{
		final int nvs = mesh.vertices().size();
		final int nts = mesh.triangles().size();
		final BufferMesh meshA = new BufferMesh( nvs, nts );
		Meshes.copy( mesh, meshA );
		final BufferMesh meshB = new BufferMesh( nvs, nts );
		Meshes.copy( mesh, meshB );

		final double[] trace = new double[ nvs ];

		switch ( weightType )
		{
		case COTANGENT:
			for ( int i = 0; i < iters; ++i )
			{
				smoothStepCotangent( mesh.triangles(), meshA, meshB, trace, lambda );
				smoothStepCotangent( mesh.triangles(), meshB, meshA, trace, mu );
			}
			break;
		case NAIVE:
			for ( int i = 0; i < iters; ++i )
			{
				smoothStepNaive( mesh.triangles(), meshA, meshB, trace, lambda );
				smoothStepNaive( mesh.triangles(), meshB, meshA, trace, mu );
			}
			break;
		default:
			throw new IllegalArgumentException( "Unhandled weight type: " + weightType );
		}

		final BufferMesh out = new BufferMesh( meshA.vertices().size(), meshA.triangles().size() );
		Meshes.calculateNormals( meshA, out );
		return out;
	}

	private static void smoothStepCotangent( final Triangles triangles, final BufferMesh source, final BufferMesh target,
			final double[] trace, final double weigth )
	{

		final int nvs = source.vertices().size();
		final int nts = source.triangles().size();

		// Zero target.
		for ( int i = 0; i < nvs; i++ )
			target.vertices().set( i, 0., 0., 0. );

		// Zero trace.
		Arrays.fill( trace, 0. );

		for ( int i = 0; i < nts; ++i )
		{

			final int ia = source.triangles().vertex0( i );
			final double ax = source.vertices().x( ia );
			final double ay = source.vertices().y( ia );
			final double az = source.vertices().z( ia );

			final int ib = source.triangles().vertex1( i );
			final double bx = source.vertices().x( ib );
			final double by = source.vertices().y( ib );
			final double bz = source.vertices().z( ib );

			final int ic = source.triangles().vertex2( i );
			final double cx = source.vertices().x( ic );
			final double cy = source.vertices().y( ic );
			final double cz = source.vertices().z( ic );

			final double abx = ax - bx;
			final double aby = ay - by;
			final double abz = az - bz;

			final double bcx = bx - cx;
			final double bcy = by - cy;
			final double bcz = bz - cz;

			final double cax = cx - ax;
			final double cay = cy - ay;
			final double caz = cz - az;

			final double area = 0.5 * hypot( aby * caz - abz * cay, abz * cax - abx * caz, abx * cay - aby * cax );
			if ( area < 1e-8 )
				continue;

			final double w = -0.5 / area;
			final double wa = w * ( abx * cax + aby * cay + abz * caz );
			final double wb = w * ( bcx * abx + bcy * aby + bcz * abz );
			final double wc = w * ( cax * bcx + cay * bcy + caz * bcz );

			trace[ ia ] += wb + wc;
			trace[ ib ] += wc + wa;
			trace[ ic ] += wa + wb;

			accumulate( target.vertices(), ib, source.vertices(), ic, wa );
			accumulate( target.vertices(), ic, source.vertices(), ib, wa );
			accumulate( target.vertices(), ic, source.vertices(), ia, wb );
			accumulate( target.vertices(), ia, source.vertices(), ic, wb );
			accumulate( target.vertices(), ia, source.vertices(), ib, wc );
			accumulate( target.vertices(), ib, source.vertices(), ia, wc );
		}

		for ( int i = 0; i < nvs; i++ )
		{
			final double ox = target.vertices().x( i );
			final double oy = target.vertices().y( i );
			final double oz = target.vertices().z( i );
			final double ix = source.vertices().x( i );
			final double iy = source.vertices().y( i );
			final double iz = source.vertices().z( i );
			final double tr = trace[ i ];

			target.vertices().set( i,
					ix + weigth * ( ox / tr - ix ),
					iy + weigth * ( oy / tr - iy ),
					iz + weigth * ( oz / tr - iz ) );
		}
	}

	private static void smoothStepNaive( final Triangles triangles, final BufferMesh source, final BufferMesh target,
			final double[] trace, final double weigth )
	{

		final int nvs = source.vertices().size();
		final int nts = source.triangles().size();

		// Zero target.
		for ( int i = 0; i < nvs; i++ )
			target.vertices().set( i, 0., 0., 0. );

		// Zero trace.
		Arrays.fill( trace, 0. );

		for ( int i = 0; i < nts; ++i )
		{

			final int ia = source.triangles().vertex0( i );
			final int ib = source.triangles().vertex1( i );
			final int ic = source.triangles().vertex2( i );

			final double w = 0.5;
			final double wa = w;
			final double wb = w;
			final double wc = w;

			trace[ ia ] += wb + wc;
			trace[ ib ] += wc + wa;
			trace[ ic ] += wa + wb;

			accumulate( target.vertices(), ib, source.vertices(), ic, wa );
			accumulate( target.vertices(), ic, source.vertices(), ib, wa );
			accumulate( target.vertices(), ic, source.vertices(), ia, wb );
			accumulate( target.vertices(), ia, source.vertices(), ic, wb );
			accumulate( target.vertices(), ia, source.vertices(), ib, wc );
			accumulate( target.vertices(), ib, source.vertices(), ia, wc );
		}

		for ( int i = 0; i < nvs; i++ )
		{
			final double ox = target.vertices().x( i );
			final double oy = target.vertices().y( i );
			final double oz = target.vertices().z( i );
			final double ix = source.vertices().x( i );
			final double iy = source.vertices().y( i );
			final double iz = source.vertices().z( i );
			final double tr = trace[ i ];

			target.vertices().set( i,
					ix + weigth * ( ox / tr - ix ),
					iy + weigth * ( oy / tr - iy ),
					iz + weigth * ( oz / tr - iz ) );
		}
	}

	private final static void accumulate( final Vertices out, final int ok, final Vertices in, final int ik,
			final double w )
	{
		final double xo = out.x( ok );
		final double yo = out.y( ok );
		final double zo = out.z( ok );
		final double xi = in.x( ik );
		final double yi = in.y( ik );
		final double zi = in.z( ik );
		out.set( ok,
				xo + xi * w,
				yo + yi * w,
				zo + zi * w );
	}

	private static final double hypot( final double x, final double y, final double z )
	{
		return Math.sqrt( x * x + y * y + z * z );
	}

	/**
	 * The weight to use when accumlating contribution for neighborhood edges in
	 * Taubin smoothing.
	 */
	public enum TaubinWeightType
	{

		/**
		 * Use cotangent of the 2 triangles around an edge when adding the
		 * contribution of one edge.
		 */
		COTANGENT,
		/**
		 * Use identical weight for all edges.
		 */
		NAIVE;

	}

}
