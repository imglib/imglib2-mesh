/*-
 * #%L
 * 3D mesh structures for ImgLib2-related projects.
 * %%
 * Copyright (C) 2016 - 2023 ImgLib2 developers.
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

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.function.Predicate;

import org.junit.Test;

import net.imglib2.FinalInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.Vertex;
import net.imglib2.mesh.impl.nio.BufferMesh;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Localizables;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class InteriorTest
{

	@Test
	public void testEdgeCase()
	{
		/*
		 * Test the situation where the ray casting algorithm will just cross an
		 * edge between two triangles with opposite normal. It should make the
		 * test fail for the current version of the algorithm.
		 */

		final int minX = 8;
		final int minY = 8;
		final int minZ = 8;
		final int maxX = 13;
		final int maxY = 13;
		final int maxZ = 13;
		final FinalInterval outCube = FinalInterval.createMinMax( minX, minY, minZ, maxX, maxY, maxZ );

		final int length = 20;
		final Img< BitType > img = ArrayImgs.bits( length, length, length );
		Views.interval( img, outCube ).forEach( p -> p.set( true ) );
		final BufferMesh m2 = makeMesh( img );

		// Move one edge "inside" the cube.
		final RealPoint v1 = new RealPoint( 11, 13, 10 );
		final RealPoint v2 = new RealPoint( 11, 13, 11 );
		for ( final Vertex v : m2.vertices() )
		{
			if ( Localizables.equals( v1, v ) || Localizables.equals( v2, v ) )
			{
				final long id = v.index();
				m2.vertices().setPosition( id,
						v.getDoublePosition( 0 ),
						v.getDoublePosition( 1 ) - 1,
						v.getDoublePosition( 2 ) );
			}
		}

		final BufferMesh mesh = new BufferMesh( m2.vertices().size(), m2.triangles().size() );
		Meshes.calculateNormals( m2, mesh );

		/*
		 * Create one point inside the cube, such that the ray-tracing algo with
		 * pass exactly on the aforementioned edge.
		 */
		
		final RealPoint p = new RealPoint( 9.5, 12, 10.5 );

		final boolean expected = true;
		final boolean actual = new Interior( mesh, 1. ).isInside( p );
		assertEquals( "Point at position " + Util.printCoordinates( p ) + " was not properly located inside the mesh.", expected, actual );

	}

	/*
	 * Disabled for now:
	 * 
	 * 
	 * The hollow cube test does not pass, despite of the assertion of inside
	 * being correct. When we extrude the inside hollow cube, the mesh
	 * interpolates triangles and yield triangles that have a 45º angle. Because
	 * we validate the interior point test against a predicate testing whether a
	 * point is inside the intervals that were used to create the image on which
	 * we created the mesh, there are small spaces near the inside limit where a
	 * point can be correctly inside the mesh and also inside the extruded cube,
	 * making this test fail.
	 */
//	@Test
	public void testHollowCube()
	{
		final int minX = 10;
		final int minY = 10;
		final int minZ = 10;
		final int maxX = 90;
		final int maxY = 90;
		final int maxZ = 90;
		final FinalInterval outCube = FinalInterval.createMinMax( minX, minY, minZ, maxX, maxY, maxZ );

		final int minX2 = 40;
		final int minY2 = 40;
		final int minZ2 = 40;
		final int maxX2 = 60;
		final int maxY2 = 60;
		final int maxZ2 = 60;
		final FinalInterval inCube = FinalInterval.createMinMax( minX2, minY2, minZ2, maxX2, maxY2, maxZ2 );


		// Make a hollow cube mesh from an image.
		final int length = 100;
		final Img< BitType > img = ArrayImgs.bits( length, length, length );
		Views.interval( img, outCube ).forEach( p -> p.set( true ) );
		Views.interval( img, inCube ).forEach( p -> p.set( false ) );
		final BufferMesh mesh = makeMesh( img );

		final Predicate< RealPoint > actualInside = p -> {
			if ( Intervals.contains( inCube, p ) )
				return false;
			if ( Intervals.contains( outCube, p ) )
				return true;
			return false;
		};

		makeTest( mesh, actualInside, length );
	}

	@Test
	public void testCube()
	{
		final int minX = 10;
		final int minY = 10;
		final int minZ = 10;
		final int maxX = 90;
		final int maxY = 90;
		final int maxZ = 90;
		final int length = 100;

		// Make a cube mesh from an image.
		final Img< BitType > img = ArrayImgs.bits( length, length, length );
		final FinalInterval cube = FinalInterval.createMinMax( minX, minY, minZ, maxX, maxY, maxZ );
		Views.interval( img, cube ).forEach( p -> p.set( true ) );
		final BufferMesh mesh = makeMesh( img );
		
		final Predicate< RealPoint > actualInside = p -> Intervals.contains( cube, p );

		makeTest( mesh, actualInside, length );
	}

	private void makeTest( final BufferMesh mesh, final Predicate< RealPoint > actualInside, final int length )
	{
		final double scale = 1.;
		// Scale is 1: this is the pixel length on which the mesh is created in
		// our case.
		final Interior test = new Interior( mesh, scale );

		final int nPoints = 100;
		final Random ran = new Random( 50l );
		final RealPoint p = new RealPoint( 3 );
		for ( int i = 0; i < nPoints; i++ )
		{
			final double x = ran.nextDouble() * length;
			final double y = ran.nextDouble() * length;
			final double z = ran.nextDouble() * length;
			p.setPosition( x, 0 );
			p.setPosition( y, 1 );
			p.setPosition( z, 2 );

			final boolean expected = actualInside.test( p );
			final boolean actual = test.isInside( p );
			assertEquals( "Point at position " + Util.printCoordinates( p ) + " was not properly located in or outside the mesh.", expected, actual );
		}
	}

	private BufferMesh makeMesh( final Img< BitType > img )
	{
		// Build a mesh from the source image..
		final Mesh m = Meshes.marchingCubes( img );
		final Mesh m2 = Meshes.removeDuplicateVertices( m, 2 );
		final BufferMesh mesh = new BufferMesh( m2.vertices().size(), m2.triangles().size() );
		Meshes.calculateNormals( m2, mesh );
		return mesh;
	}

	public static void main( final String[] args )
	{
		new InteriorTest().testEdgeCase();
	}
}
