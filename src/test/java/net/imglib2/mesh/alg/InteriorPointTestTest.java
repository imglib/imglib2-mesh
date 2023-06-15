package net.imglib2.mesh.alg;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import net.imglib2.FinalInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.obj.Mesh;
import net.imglib2.mesh.obj.nio.BufferMesh;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class InteriorPointTestTest
{

	@Test
	public void test()
	{
		final int minX = 10;
		final int minY = 10;
		final int minZ = 10;
		final int maxX = 90;
		final int maxY = 90;
		final int maxZ = 90;
		final int length = 100;

		// Make a cube mesh from an image.
		final int intensity = 100;
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( length, length, length );
		final FinalInterval cube = FinalInterval.createMinMax( minX, minY, minZ, maxX, maxY, maxZ );
		Views.interval( img, cube ).forEach( p -> p.set( ( byte ) intensity ) );

		// Build a mesh from the source image..
		final Mesh m = Meshes.marchingCubes( img, intensity / 2. );
		final Mesh m2 = Meshes.removeDuplicateVertices( m, 2 );
		final BufferMesh mesh = new BufferMesh( m2.vertices().isize(), m2.triangles().isize() );
		Meshes.calculateNormals( m2, mesh );

		// The algo to test.
		final double scale = 1.;
		// Scale is 1: this is the pixel length on which the mesh is created in
		// our case.
		final InteriorPointTest test = new InteriorPointTest( mesh, scale );

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

			final boolean expected = Intervals.contains( cube, p );
			final boolean actual = test.isInside( p );

			System.out.println( String.format( "%s inside? Should be %s, and is %s.",
					Util.printCoordinates( p ), expected, actual ) ); // DEBUG

			assertEquals( "Point at position " + Util.printCoordinates( p ) + " was not properly located in or outside the mesh.", expected, actual );
		}
	}
}
