package net.imglib2.mesh.alg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.mesh.alg.EllipsoidFitter.EllipsoidFit;
import net.imglib2.mesh.impl.nio.BufferMesh;
import net.imglib2.mesh.util.Icosahedron;

public class EllipsoidFitterTest
{

	private static final double TOLERANCE = 10e-6;

	@Test
	public void testFitSphere()
	{
		final RealLocalizable center = new RealPoint( 1., 2., 3. );
		final double radius = 5.;
		final BufferMesh mesh = Icosahedron.sphere( center, radius );
		final EllipsoidFit fit = EllipsoidFitter.fit( mesh );
		
		for ( int d = 0; d < 3; d++ )
		{
			assertEquals( "Incorrect center in the fitted ellipse.",
					center.getDoublePosition( d ), fit.center.getDoublePosition( d ), TOLERANCE );

		}
		assertEquals( "Incorrect semi-axis length in the fitted ellipse.", radius, fit.r1, TOLERANCE );
		assertEquals( "Incorrect semi-axis length in the fitted ellipse.", radius, fit.r2, TOLERANCE );
		assertEquals( "Incorrect semi-axis length in the fitted ellipse.", radius, fit.r3, TOLERANCE );
	}

}
