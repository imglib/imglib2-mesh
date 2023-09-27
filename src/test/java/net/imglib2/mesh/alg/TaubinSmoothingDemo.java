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

import java.io.IOException;
import java.util.Random;

import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.alg.TaubinSmoothing.TaubinWeightType;
import net.imglib2.mesh.io.ply.PLYMeshIO;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class TaubinSmoothingDemo
{

	public static void main( final String[] args ) throws IOException
	{

		// Make a cube
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 100, 100, 100 );
		final FinalInterval cube = Intervals.createMinMax( 25, 25, 25, 75, 75, 75 );
		Views.interval( img, cube ).forEach( p -> p.setOne() );

		final Mesh m1 = Meshes.marchingCubes( img, 0.5 );
		final Mesh mesh = Meshes.removeDuplicateVertices( m1, 2 );

		// Add noise.
		final Random ran = new Random( 45l );
		for ( int i = 0; i < mesh.vertices().sizel(); i++ )
		{
			final double x = mesh.vertices().x( i );
			final double y = mesh.vertices().y( i );
			final double z = mesh.vertices().z( i );

			mesh.vertices().set( i,
					x + ran.nextDouble() - 0.5,
					y + ran.nextDouble() - 0.5,
					z + ran.nextDouble() - 0.5 );
		}

		PLYMeshIO.save( mesh, "samples/BeforeSmooth.ply" );
		PLYMeshIO.save( TaubinSmoothing.smooth( mesh ), "samples/SmoothedBit.ply" );
		PLYMeshIO.save( TaubinSmoothing.smooth( mesh, 10, 0.5, -0.53, TaubinWeightType.NAIVE ),
				"samples/SmoothedMore.ply" );
	}

}
