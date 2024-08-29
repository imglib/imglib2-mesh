/*-
 * #%L
 * 3D mesh structures for ImgLib2-related projects.
 * %%
 * Copyright (C) 2016 - 2024 ImgLib2 developers.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gnu.trove.list.array.TDoubleArrayList;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.alg.zslicer.RamerDouglasPeucker;
import net.imglib2.mesh.alg.zslicer.Slice;
import net.imglib2.mesh.alg.zslicer.ZSlicer;
import net.imglib2.mesh.impl.nio.BufferMesh;

public class MeshCursor< T > implements Cursor< T >
{

	private final double[] cal;

	private final int minX;

	private final int maxX;

	private final int minY;

	private final int maxY;

	private final int minZ;

	private final int maxZ;

	private final RandomAccess< T > ra;

	private final Mesh mesh;

	private boolean hasNext;

	private int iy;

	private int iz;

	private int ix;

	/**
	 * List of resolved X positions where we enter / exit the mesh. Set by the
	 * ray casting algorithm.
	 */
	private final TDoubleArrayList intersectionXs = new TDoubleArrayList();

	private Slice slice;

	private final Map< Integer, Slice > sliceMap;

	public MeshCursor( final RandomAccess< T > ra, final Mesh mesh, final double[] cal )
	{
		this( ra, mesh, cal, Meshes.boundingBox( mesh ) );
	}

	public MeshCursor( final RandomAccess< T > ra, final Mesh mesh, final double[] cal, final RealInterval boundingBox )
	{
		this.ra = ra;
		this.mesh = mesh;
		this.cal = cal;
		this.minX = ( int ) Math.floor( boundingBox.realMin( 0 ) / cal[ 0 ] );
		this.maxX = ( int ) Math.ceil( boundingBox.realMax( 0 ) / cal[ 0 ] );
		this.minY = ( int ) Math.floor( boundingBox.realMin( 1 ) / cal[ 1 ] );
		this.maxY = ( int ) Math.ceil( boundingBox.realMax( 1 ) / cal[ 1 ] );
		this.minZ = ( int ) Math.floor( boundingBox.realMin( 2 ) / cal[ 2 ] );
		this.maxZ = ( int ) Math.ceil( boundingBox.realMax( 2 ) / cal[ 2 ] );
		this.sliceMap = buildSliceMap( mesh, boundingBox, cal[ 0 ], cal[ 2 ] );
		reset();
	}

	@Override
	public void reset()
	{
		this.ix = maxX; // To force a new ray cast when we call fwd()
		this.iy = minY - 1; // Then we will move to minY.
		this.iz = minZ;
		// Get the next z slice.
		this.hasNext = false;
		while ( true )
		{
			iz++;
			if ( iz > maxZ )
				return; // Finished!
			slice = sliceMap.get( iz );
			if ( slice != null )
				break;
		}
		this.hasNext = true;
		preFetch();
	}

	@Override
	public void fwd()
	{
		ra.setPosition( ix, 0 );
		ra.setPosition( iy, 1 );
		ra.setPosition( iz, 2 );
		preFetch();
	}

	private void preFetch()
	{
		hasNext = false;
		while ( true )
		{
			// Find next position.
			ix++;
			if ( ix > maxX )
			{
				ix = minX;
				while ( true )
				{
					// Next Y line, we will need to ray cast again.
					ix = minX;
					iy++;
					if ( iy > maxY )
					{
						iy = minY;

						// Get the next z slice.
						while ( true )
						{
							iz++;
							if ( iz > maxZ )
								return; // Finished!
							slice = sliceMap.get( iz );
							if ( slice != null )
								break;
						}
					}

					// New ray cast.
					final double y = iy * cal[ 1 ];
					slice.xRayCast( y, intersectionXs, cal[ 1 ] );

					// No intersection?
					if ( !intersectionXs.isEmpty() )
						break;

					// No intersection on this line, move to the next.
				}
			}
			// We have found the next position.

			// Is it inside?
			final double x = ix * cal[ 0 ];

			// Special case: only one intersection.
			if ( intersectionXs.size() == 1 )
			{
				if ( x == intersectionXs.getQuick( 0 ) )
				{
					hasNext = true;
					return;
				}
				else
				{
					continue;
				}
			}

			final int i = intersectionXs.binarySearch( x );
			if ( i >= 0 )
			{
				// Fall on an intersection exactly.
				hasNext = true;
				return;
			}
			final int ip = -( i + 1 );
			// Odd or even?
			if ( ip % 2 != 0 )
			{
				// Odd. We are inside.
				hasNext = true;
				return;
			}

			// Not inside, move to the next point.
		}
	}

	@Override
	public boolean hasNext()
	{
		return hasNext;
	}

	@Override
	public void jumpFwd( final long steps )
	{
		for ( int i = 0; i < steps; i++ )
			fwd();
	}

	@Override
	public T next()
	{
		fwd();
		return get();
	}

	@Override
	public long getLongPosition( final int d )
	{
		return ra.getLongPosition( d );
	}

	@Override
	public Cursor< T > copyCursor()
	{
		final BufferMesh dest = new BufferMesh( mesh.vertices().size(), mesh.triangles().size() );
		Meshes.copy( mesh, dest );
		return new MeshCursor<>( ra.copy(), dest, cal.clone() );
	}

	@Override
	public Cursor< T > copy()
	{
		return copyCursor();
	}

	@Override
	public int numDimensions()
	{
		return 3;
	}

	@Override
	public T get()
	{
		return ra.get();
	}

	/**
	 * Computes the intersections of the specified mesh with the multiple
	 * Z-slice <b>at integer coordinates</b> corresponding to 1-pixel spacing in
	 * Z. This is why we need to have the <code>calibration</code> array.
	 *
	 * @param mesh
	 *            the mesh to reslice.
	 * @param boundingBox
	 *            its bounding box.
	 * @param calibration
	 *            the pixel size array, needed to compute the 1-pixel spacing.
	 * @return a map from slice position (integer, pixel coordinates) to slices.
	 */
	private static final Map< Integer, Slice > buildSliceMap( final Mesh mesh, final RealInterval boundingBox,
			final double xyScale, final double zScale )
	{
		final int minZ = ( int ) Math.ceil( boundingBox.realMin( 2 ) / zScale );
		final int maxZ = ( int ) Math.floor( boundingBox.realMax( 2 ) / zScale );
		/*
		 * Compute the Z integers, in pixel coordinates, of the mesh
		 * intersection. These coordinates are absolute value (relative to mesh
		 * center).
		 */
		final int[] zSlices = new int[ maxZ - minZ + 1 ];
		/*
		 * Compute equivalent Z positions in mesh units, of these intersections.
		 */
		final double[] zPos = new double[ zSlices.length ];
		for ( int i = 0; i < zSlices.length; i++ )
		{
			zSlices[ i ] = ( minZ + i );// pixel coords, absolute value
			zPos[ i ] = zSlices[ i ] * zScale;
		}

		// Compute the slices.
		final List< Slice > slices = ZSlicer.slices( mesh, zPos, zScale );

		// Simplify below 1/4th of a pixel.
		final double epsilon = xyScale * 0.25;
		final List< Slice > simplifiedSlices = slices.stream().map( s -> RamerDouglasPeucker.simplify( s, epsilon ) )
				.collect( Collectors.toList() );

		// Store in a map of Z slice -> slice.
		final Map< Integer, Slice > sliceMap = new HashMap<>();
		for ( int i = 0; i < zSlices.length; i++ )
			sliceMap.put( Integer.valueOf( zSlices[ i ] ), simplifiedSlices.get( i ) );

		return sliceMap;
	}
}
