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
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.mesh.Mesh;
import net.imglib2.type.logic.BitType;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests {@link Voxelization}.
 *
 * @author Kyle Harrington
 * @author Curtis Rueden
 */
public class VoxelizationTest
{

	@Test
	public void voxelization3D() {
		final int radius = 50; // a balance between speed and accuracy
		final int w, h, d;
		w = h = d = 2 * radius;
		RandomAccessibleInterval<BitType> sphere = generateSphere(radius);
		final Mesh mesh = MarchingCubesBooleanType.calculate(sphere);

		// The mesh is good by now, let's check the voxelization
		Img< BitType > voxelization = Voxelization.voxelize(mesh, w, h, d);

		// Flood fill (ops implementation starts from borders)
		//RandomAccessibleInterval< BitType > filledVoxelization = ops.run(DefaultFillHoles.class, voxelization);
		Img< BitType > filledVoxelization = voxelization.copy();
		FloodFill.fill(
			filledVoxelization, // source
			filledVoxelization, // target
			new Point( radius, radius ,radius ), // seed
			new BitType( true ), // fillLabel
			new RectangleShape( 1, false ) );

		// Comparison
		long diff = compareImages(sphere, filledVoxelization);

		// radius |  samples | deviations | area (4πr²) | dev-to-area
		// -------|----------|------------|-------------|------------
		//     10 |     4166 |       1494 |      1256.6 | 1.188922489
		//     20 |    33398 |       6690 |      5026.4 | 1.330972465
		//     30 |   113078 |      15406 |     11309.4 | 1.362229650
		//     40 |   267758 |      27850 |     20105.6 | 1.385186217
		//     50 |   523302 |      43710 |     31415.0 | 1.391373548
		//     60 |   904086 |      63102 |     45237.6 | 1.394901586
		//     70 |  1436382 |      86178 |     61573.4 | 1.399597878
		//     80 |  2143638 |     112690 |     80422.4 | 1.401226524
		//     90 |  3053614 |     143070 |    101784.6 | 1.405615388
		//    100 |  4187854 |     176886 |    125660.0 | 1.407655579
		//    110 |  5574718 |     213954 |    152048.6 | 1.407142190
		//    120 |  7236574 |     255058 |    180950.4 | 1.409546483
		//    130 |  9201622 |     299470 |    212365.4 | 1.410163803
		//    140 | 11492078 |     347286 |    246293.6 | 1.410048820
		//    150 | 14137634 |     399078 |    282735.0 | 1.411491326
		final double area = 4 * Math.PI * radius * radius;
		final double ratio = diff / area;
		final String statSuffix = String.format(" (diff=%d, area=%f, ratio=%f).", diff, area, ratio);
		assertTrue("Voxelization differs from the original image too much" + statSuffix, ratio < 1.412);
		assertTrue("Voxelization matches the original image suspiciously well" + statSuffix, ratio > 1.18);
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
