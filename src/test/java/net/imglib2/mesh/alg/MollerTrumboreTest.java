/*-
 * #%L
 * 3D mesh structures for ImgLib2-related projects.
 * %%
 * Copyright (C) 2016 - 2026 ImgLib2 developers.
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.imglib2.mesh.impl.naive.NaiveDoubleMesh;

public class MollerTrumboreTest
{

	@Test
	public void testIntersection()
	{
		// A triangle in the plane x = 5.
		final NaiveDoubleMesh mesh = new NaiveDoubleMesh();
		final long v0 = mesh.vertices().add( 5, 0, 0 );
		final long v1 = mesh.vertices().add( 5, 10, 0 );
		final long v2 = mesh.vertices().add( 5, 0, 10 );
		mesh.triangles().add( v0, v1, v2 );
		final MollerTrumbore mt = new MollerTrumbore( mesh );

		// An oblique ray, so that each coordinate of the hit is distinct.
		final double[] intersection = new double[ 3 ];
		assertTrue( mt.rayIntersectsTriangle( 0, 0, 1, 2, 1, 0.1, 0.2, intersection ) );
		assertArrayEquals( new double[] { 5, 1.5, 3 }, intersection, 1e-12 );

		// The same ray, pointing away from the triangle.
		assertFalse( mt.rayIntersectsTriangle( 0, 0, 1, 2, -1, -0.1, -0.2, intersection ) );
	}
}
