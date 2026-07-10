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

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fill.FloodFill;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.mesh.Mesh;
import net.imglib2.type.logic.BitType;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests {@link EuclideanDistanceVoxelization}.
 *
 * @author Kyle Harrington
 * @author Curtis Rueden
 * @author Andrew McCall
 */
public class VoxelizationTest
{

	@Test
	public void voxelization3D() {
		final int radius = 50; // a balance between speed and accuracy
		RandomAccessibleInterval<BitType> sphere = generateSphere(radius);
		final Mesh mesh = MarchingCubesBooleanType.calculate(sphere);
		Img<BitType> voxelization = ArrayImgs.bits(sphere.dimensionsAsLongArray());

		// The mesh is good by now, let's check the voxelization
		EuclideanDistanceVoxelization.voxelize(mesh, voxelization);

		// Flood fill (ops implementation starts from borders)
		//RandomAccessibleInterval< BitType > filledVoxelization = ops.run(DefaultFillHoles.class, voxelization);
		Img< BitType > filledVoxelization = voxelization.copy();
		FloodFill.fill(
			filledVoxelization, // source
			filledVoxelization, // target
			new Point( radius, radius ,radius ), // seed
			new BitType( true ), // fillLabel
			new DiamondShape( 1 ) );

		// Comparison
		long diff = compareImages(sphere, filledVoxelization);

		final double area = 4 * Math.PI * radius * radius;
		final double ratio = diff / area;
		final String statSuffix = String.format(" (diff=%d, area=%f, ratio=%f).", diff, area, ratio);
		assertTrue("Voxelization does not match image perfectly" + statSuffix, ratio <= Double.MIN_VALUE);
	}

	/**
	 * Creates a 3D binary image of a sphere.
	 *
	 * @param r The radius of the sphere.
	 * @return A RandomAccessibleInterval representing the sphere.
	 */
	private RandomAccessibleInterval<BitType> generateSphere(int r) {
		long[] dims = new long[] {2*r, 2*r, 2*r}; // Dimensions of the bounding box of the sphere
		Img<BitType> sphereImg = ArrayImgs.bits(dims);

		Cursor<BitType> cursor = sphereImg.localizingCursor();

		// Center of the sphere
		int cx = r;
		int cy = r;
		int cz = r;

		while (cursor.hasNext()) {
			cursor.fwd();
			int x = cursor.getIntPosition(0) - cx;
			int y = cursor.getIntPosition(1) - cy;
			int z = cursor.getIntPosition(2) - cz;

			if (x * x + y * y + z * z <= r * r) {
				cursor.get().set(true);
			}
		}

		return sphereImg;
	}

	private long compareImages( RandomAccessibleInterval<BitType> img1, RandomAccessibleInterval<BitType> img2 )
	{
		final long[] diff = { 0 };
		LoopBuilder.setImages( img1, img2 ).forEachPixel( ( a, b ) ->
		{
			if ( !a.valueEquals( b ) ) diff[ 0 ]++;
		});
		return diff[ 0 ];
	}
}
