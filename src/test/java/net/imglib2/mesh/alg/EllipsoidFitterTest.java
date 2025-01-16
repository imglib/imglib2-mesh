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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.mesh.alg.EllipsoidFitter.Ellipsoid;
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
		final Ellipsoid fit = EllipsoidFitter.fit( mesh );
		
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
