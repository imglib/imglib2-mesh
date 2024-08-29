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
package net.imglib2.mesh.alg.zslicer;

import gnu.trove.list.array.TDoubleArrayList;

public final class Contour
{

	private final TDoubleArrayList x;

	private final TDoubleArrayList y;

	/** Normals. */
	private final TDoubleArrayList nx;

	/** Normals. */
	private final TDoubleArrayList ny;

	private final boolean isInterior;

	public Contour( final boolean isInterior )
	{
		this( new TDoubleArrayList(), new TDoubleArrayList(), new TDoubleArrayList(), new TDoubleArrayList(),
				isInterior );
	}

	public Contour( final TDoubleArrayList x, final TDoubleArrayList y, final TDoubleArrayList nx,
			final TDoubleArrayList ny, final boolean isInterior )
	{
		this.x = x;
		this.y = y;
		this.nx = nx;
		this.ny = ny;
		this.isInterior = isInterior;
	}

	void add( final double x, final double y, final double nx, final double ny )
	{
		this.x.add( x );
		this.y.add( y );
		this.nx.add( nx );
		this.ny.add( ny );
	}

	public boolean isInterior()
	{
		return isInterior;
	}

	public double centerX()
	{
		double sum = 0.;
		for ( int i = 0; i < x.size(); i++ )
			sum += x.getQuick( i );
		return sum / x.size();
	}

	public double centerY()
	{
		double sum = 0.;
		for ( int i = 0; i < y.size(); i++ )
			sum += y.getQuick( i );
		return sum / y.size();
	}

	public int size()
	{
		return x.size();
	}

	public double x( final int i )
	{
		return x.getQuick( i );
	}

	public double y( final int i )
	{
		return y.getQuick( i );
	}

	public double nx( final int i )
	{
		return nx.getQuick( i );
	}

	public double ny( final int i )
	{
		return ny.getQuick( i );
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder( super.toString() );
		str.append( String.format( "\n%d vertices, is interior: %s", x.size(), isInterior ) );
		return str.toString();
	}

	public double area()
	{
		return Math.abs( signedArea( x, y ) );
	}

	private static final double signedArea( final TDoubleArrayList x, final TDoubleArrayList y )
	{
		final int n = x.size();
		double a = 0.0;
		for ( int i = 0; i < n - 1; i++ )
			a += x.getQuick( i ) * y.getQuick( i + 1 ) - x.getQuick( i + 1 ) * y.getQuick( i );

		return ( a + x.getQuick( n - 1 ) * y.getQuick( 0 ) - x.getQuick( 0 ) * y.getQuick( n - 1 ) ) / 2.0;
	}
}
