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
package net.imglib2.mesh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.imglib2.Cursor;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.mesh.alg.EllipsoidFitter;
import net.imglib2.mesh.alg.EuclideanDistanceVoxelization;
import net.imglib2.mesh.alg.Interior;
import net.imglib2.mesh.alg.TaubinSmoothing;
import net.imglib2.mesh.alg.hull.ConvexHull;
import net.imglib2.mesh.alg.zslicer.Slice;
import net.imglib2.mesh.alg.zslicer.ZSlicer;
import net.imglib2.mesh.impl.naive.NaiveDoubleMesh;
import net.imglib2.mesh.impl.nio.BufferMesh;
import net.imglib2.mesh.io.ply.PLYMeshIO;
import net.imglib2.mesh.io.stl.STLMeshIO;
import net.imglib2.type.logic.BitType;

/**
 * Usage examples for ImgLib2 Mesh, linked from the README.
 * <p>
 * They live here as tests so that they stay compilable and correct as the API
 * evolves.
 * </p>
 */
public class ExamplesTest
{

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	/** Builds a mesh from a binary image and measures it. */
	@Test
	public void meshFromImage()
	{
		final Img< BitType > mask = sphereMask( 32, 10 );

		// Extract the surface. For grayscale images, use
		// Meshes.marchingCubes(image, isoLevel) instead.
		final Mesh raw = Meshes.marchingCubes( mask );

		// Merge the duplicate vertices that marching cubes produces between
		// adjacent cubes. This discards normals, so recompute them afterward;
		// some algorithms (e.g. Interior) rely on them.
		final BufferMesh deduped = Meshes.removeDuplicateVertices( raw, 2 );
		final BufferMesh mesh = new BufferMesh( deduped.vertices().size(), deduped.triangles().size() );
		Meshes.calculateNormals( deduped, mesh );

		final double volume = MeshStats.volume( mesh );
		final double surfaceArea = MeshStats.surfaceArea( mesh );
		final double sphericity = MeshStats.sphericity( mesh );
		final double solidity = MeshStats.solidity( mesh );

		assertTrue( mesh.vertices().size() < raw.vertices().size() );
		// Note: Marching cubes on a binary mask places the surface about half a
		// voxel inside the object boundary, so volume is underestimated.
		assertEquals( 4. / 3 * Math.PI * 1000, volume, 700 );
		assertEquals( 4 * Math.PI * 100, surfaceArea, 100 );
		assertTrue( sphericity > 0.8 && sphericity <= 1 );
		assertTrue( solidity > 0.9 && solidity <= 1 );
	}

	/** Builds a mesh by hand, one vertex and triangle at a time. */
	@Test
	public void meshByHand()
	{
		final NaiveDoubleMesh mesh = new NaiveDoubleMesh();
		final long v0 = mesh.vertices().add( 0, 0, 0 );
		final long v1 = mesh.vertices().add( 1, 0, 0 );
		final long v2 = mesh.vertices().add( 0, 1, 0 );
		mesh.triangles().add( v0, v1, v2 );

		assertEquals( 3, mesh.vertices().size() );
		assertEquals( 1, mesh.triangles().size() );
	}

	/** Reads a PLY file, smooths the mesh, and writes it as STL. */
	@Test
	public void readProcessWrite() throws IOException
	{
		final String input = new File( tmp.getRoot(), "input.ply" ).getPath();
		final String output = new File( tmp.getRoot(), "output.stl" ).getPath();
		PLYMeshIO.save( sphereMesh(), input );

		final BufferMesh mesh = PLYMeshIO.open( input );
		final BufferMesh smooth = TaubinSmoothing.smooth( mesh );
		STLMeshIO.save( smooth, output );

		assertEquals( mesh.triangles().size(), STLMeshIO.open( output ).triangles().size() );
	}

	/** A tour of other common operations. */
	@Test
	public void otherOperations()
	{
		final Mesh mesh = sphereMesh();

		// Split into connected components.
		int components = 0;
		for ( final BufferMesh part : Meshes.connectedComponents( mesh ) )
			components++;
		assertEquals( 1, components );

		// Convex hull and best-fit ellipsoid.
		final Mesh hull = ConvexHull.calculate( mesh );
		final EllipsoidFitter.Ellipsoid ellipsoid = EllipsoidFitter.fit( mesh );
		assertTrue( hull.vertices().size() <= mesh.vertices().size() );
		assertEquals( 10, ellipsoid.r1, 1 );

		// Is a point inside the mesh?
		final Interior interior = new Interior( mesh, 1.0 );
		assertTrue( interior.isInside( new RealPoint( 15.5, 15.5, 15.5 ) ) );
		assertFalse( interior.isInside( new RealPoint( 1, 1, 1 ) ) );

		// Cross-sections at given Z positions.
		final List< Slice > slices = ZSlicer.slices( mesh, new double[] { 12, 16, 20 }, 1.0 );
		assertEquals( 3, slices.size() );

		// Rasterize back into a binary image.
		final Img< BitType > voxels = EuclideanDistanceVoxelization.voxelize( mesh );
		assertEquals( 3, voxels.numDimensions() );
	}

	/** Creates a {@code size}^3 binary image containing a centered sphere. */
	private static Img< BitType > sphereMask( final int size, final double radius )
	{
		final Img< BitType > mask = ArrayImgs.bits( size, size, size );
		final double c = ( size - 1 ) / 2.;
		final Cursor< BitType > cursor = mask.localizingCursor();
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			final double dx = cursor.getDoublePosition( 0 ) - c;
			final double dy = cursor.getDoublePosition( 1 ) - c;
			final double dz = cursor.getDoublePosition( 2 ) - c;
			cursor.get().set( dx * dx + dy * dy + dz * dz <= radius * radius );
		}
		return mask;
	}

	private static BufferMesh sphereMesh()
	{
		final BufferMesh deduped = Meshes.removeDuplicateVertices( Meshes.marchingCubes( sphereMask( 32, 10 ) ), 2 );
		final BufferMesh mesh = new BufferMesh( deduped.vertices().size(), deduped.triangles().size() );
		Meshes.calculateNormals( deduped, mesh );
		return mesh;
	}
}
