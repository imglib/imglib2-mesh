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

package net.imglib2.mesh;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.mesh.alg.MarchingCubesBooleanType;
import net.imglib2.mesh.alg.MarchingCubesRealType;
import net.imglib2.mesh.alg.MeshConnectedComponents;
import net.imglib2.mesh.alg.RemoveDuplicateVertices;
import net.imglib2.mesh.alg.SimplifyMesh;
import net.imglib2.mesh.impl.naive.NaiveDoubleMesh;
import net.imglib2.mesh.impl.nio.BufferMesh;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

/**
 * Utility methods for working with {@link Mesh} objects.
 *
 * @author Curtis Rueden
 * @author Kyle Harrington
 * @author Eugene Katrukha
 */
public class Meshes
{

	/**
	 * Finds the center of a mesh using vertices.
	 *
	 * @return a RealPoint representing the mesh's center
	 */
	public static RealPoint center( final Mesh m )
	{
		final RealPoint p = new RealPoint( 0, 0, 0 );
		for ( final Vertex v : m.vertices() )
			p.move( v );

		for ( int d = 0; d < 3; d++ )
			p.setPosition( p.getDoublePosition( d ) / m.vertices().sizel(), d );

		return p;
	}

	/**
	 * Computes and returns an <b>oriented</b> bounding box {@link RealInterval}
	 *
	 * @param mesh
	 * @return the output {@link Mesh}
	 * @implNote op names="geom.boundingBox"
	 */
	public static RealInterval boundingBox( final net.imglib2.mesh.Mesh mesh )
	{
		final double[] boundingBox = new double[] { Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
		for ( final Vertex v : mesh.vertices() )
		{
			final double x = v.x(), y = v.y(), z = v.z();
			if ( x < boundingBox[ 0 ] )
				boundingBox[ 0 ] = x;
			if ( y < boundingBox[ 1 ] )
				boundingBox[ 1 ] = y;
			if ( z < boundingBox[ 2 ] )
				boundingBox[ 2 ] = z;
			if ( x > boundingBox[ 3 ] )
				boundingBox[ 3 ] = x;
			if ( y > boundingBox[ 4 ] )
				boundingBox[ 4 ] = y;
			if ( z > boundingBox[ 5 ] )
				boundingBox[ 5 ] = z;
		}
		return Intervals.createMinMaxReal( boundingBox[ 0 ], boundingBox[ 1 ], boundingBox[ 2 ], boundingBox[ 3 ], boundingBox[ 4 ], boundingBox[ 5 ] );
	}

	/**
	 * Computes and returns an <b>oriented</b> bounding box {@link Mesh}
	 *
	 * @param input
	 * @return the output {@link Mesh}
	 * @implNote op names="geom.boundingBox"
	 */
	public static Mesh boundingBoxMesh(final Mesh input) {
		RealInterval interval = boundingBox(input);
		Mesh m = new NaiveDoubleMesh();
		// BOTTOM VERTICES
		long bbl = m.vertices().add(interval.realMin(0), interval.realMin(1), interval.realMin(2));
		long bbr = m.vertices().add(interval.realMax(0), interval.realMin(1), interval.realMin(2));
		long bfl = m.vertices().add(interval.realMin(0), interval.realMax(1), interval.realMin(2));
		long bfr = m.vertices().add(interval.realMax(0), interval.realMax(1), interval.realMin(2));
		// TOP VERTICES
		long tbl = m.vertices().add(interval.realMin(0), interval.realMin(1), interval.realMax(2));
		long tbr = m.vertices().add(interval.realMax(0), interval.realMin(1), interval.realMax(2));
		long tfl = m.vertices().add(interval.realMin(0), interval.realMax(1), interval.realMax(2));
		long tfr = m.vertices().add(interval.realMax(0), interval.realMax(1), interval.realMax(2));

		// BOTTOM TRIANGLES
		m.triangles().add(bbl, bfr, bbr);
		m.triangles().add(bbl, bfl, bfr);
		// FRONT TRIANGLES
		m.triangles().add(tfl, bfr, bfl);
		m.triangles().add(tfl, tfr, bfr);
		// TOP TRIANGLES
		m.triangles().add(tbl, tfr, tfl);
		m.triangles().add(tbl, tbr, tfr);
		// BACK TRIANGLES
		m.triangles().add(tbl, bbl, bbr);
		m.triangles().add(tbl, tbr, tfr);
		// LEFT TRIANGLES
		m.triangles().add(tfl, bfl, bbl);
		m.triangles().add(tfl, bbl, tbr);
		// RIGHT TRIANGLES
		m.triangles().add(tfr, tbr, bbr);
		m.triangles().add(tfr, bbr, tfr);

		return m;
	}

	/**
	 * Copies a mesh into another mesh.
	 *
	 * @param src
	 *            Source mesh, from which data will be copied.
	 * @param dest
	 *            Destination mesh, into which source will be copied. (container)
	 * @implNote op names="engine.copy, copy.mesh"
	 */
	public static void copy( final net.imglib2.mesh.Mesh src, final net.imglib2.mesh.Mesh dest )
	{
		final Map< Long, Long > vIndexMap = new HashMap<>();
		// Copy the vertices, keeping track when indices change.
		for ( final Vertex v : src.vertices() )
		{
			final long srcIndex = v.index();
			final long destIndex = dest.vertices().add( //
					v.x(), v.y(), v.z(), //
					v.nx(), v.ny(), v.nz(), //
					v.u(), v.v() );
			if ( srcIndex != destIndex )
			{
				/*
				 * NB: If the destination vertex index matches the source, we
				 * skip recording the entry, to save space in the map. Later, we
				 * leave indexes unchanged which are absent from the map.
				 * 
				 * This scenario is actually quite common, because vertices are
				 * often numbered in natural order, with the first vertex having
				 * index 0, the second having index 1, etc., although it is not
				 * guaranteed.
				 */
				vIndexMap.put( srcIndex, destIndex );
			}
		}
		// Copy the triangles, taking care to use destination indices.
		for ( final Triangle tri : src.triangles() )
		{
			final long v0src = tri.vertex0();
			final long v1src = tri.vertex1();
			final long v2src = tri.vertex2();
			final long v0 = vIndexMap.getOrDefault( v0src, v0src );
			final long v1 = vIndexMap.getOrDefault( v1src, v1src );
			final long v2 = vIndexMap.getOrDefault( v2src, v2src );

			dest.triangles().add( v0, v1, v2, tri.nx(), tri.ny(), tri.nz() );
		}
	}

	/**
	 * Calculates the normals for a mesh. Creates a new mesh with the calculated
	 * normals. Assumes CCW winding order.
	 *
	 * @param src
	 *            Source mesh, used for vertex and triangle info
	 * @param dest
	 *            Destination mesh, will be populated with src's info plus the
	 *            calculated normals
	 */
	public static void calculateNormals( final net.imglib2.mesh.Mesh src, final net.imglib2.mesh.Mesh dest )
	{

		// Store the triangle normals
		final HashMap< Long, float[] > triNormals = new HashMap<>();
		// Store per vertex normals
		final HashMap< Long, float[] > vNormals = new HashMap<>();
		//vertex cumulative normal
		float[] cumNormal;
		
		for ( final Triangle tri : src.triangles() )
		{
			final int v0 = ( int ) tri.vertex0();
			final int v1 = ( int ) tri.vertex1();
			final int v2 = ( int ) tri.vertex2();

			final float v0x = src.vertices().xf( v0 );
			final float v0y = src.vertices().yf( v0 );
			final float v0z = src.vertices().zf( v0 );
			final float v1x = src.vertices().xf( v1 );
			final float v1y = src.vertices().yf( v1 );
			final float v1z = src.vertices().zf( v1 );
			final float v2x = src.vertices().xf( v2 );
			final float v2y = src.vertices().yf( v2 );
			final float v2z = src.vertices().zf( v2 );

			final float v10x = v1x - v0x;
			final float v10y = v1y - v0y;
			final float v10z = v1z - v0z;

			final float v20x = v2x - v0x;
			final float v20y = v2y - v0y;
			final float v20z = v2z - v0z;

			final float nx = v10y * v20z - v10z * v20y;
			final float ny = v10z * v20x - v10x * v20z;
			final float nz = v10x * v20y - v10y * v20x;
			
			//cumulative, triangle area weighted normals per vertex.
			for ( final long idx : new long[] { tri.vertex0(), tri.vertex1(), tri.vertex2() } )
			{
				cumNormal = vNormals.getOrDefault( idx, new float[] { 0, 0, 0 } );
				cumNormal[ 0 ] += nx;
				cumNormal[ 1 ] += ny;
				cumNormal[ 2 ] += nz;
				vNormals.put( idx, cumNormal );
			}
			
			final float nmag = ( float ) Math.sqrt( Math.pow( nx, 2 ) + Math.pow( ny, 2 ) + Math.pow( nz, 2 ) );
			triNormals.put( tri.index(), new float[] { nx / nmag, ny / nmag, nz / nmag } );
		}

		// Now populate dest
		final Map< Long, Long > vIndexMap = new HashMap<>();
		float[] vNormal;
		double vNormalMag;
		// Copy the vertices, keeping track when indices change.
		for ( final Vertex v : src.vertices() )
		{
			final long srcIndex = v.index();
			vNormal = vNormals.get( v.index() );
			vNormalMag = Math.sqrt( Math.pow( vNormal[ 0 ], 2 ) + Math.pow( vNormal[ 1 ], 2 ) + Math.pow( vNormal[ 2 ], 2 ) );
			final long destIndex = dest.vertices().add( //
					v.x(), v.y(), v.z(), //
					vNormal[ 0 ] / vNormalMag, vNormal[ 1 ] / vNormalMag, vNormal[ 2 ] / vNormalMag, //
					v.u(), v.v() );
			if ( srcIndex != destIndex )
			{
				/*
				 * NB: If the destination vertex index matches the source, we
				 * skip recording the entry, to save space in the map. Later, we
				 * leave indexes unchanged which are absent from the map.
				 * 
				 * This scenario is actually quite common, because vertices are
				 * often numbered in natural order, with the first vertex having
				 * index 0, the second having index 1, etc., although it is not
				 * guaranteed.
				 */
				vIndexMap.put( srcIndex, destIndex );
			}
		}
		// Copy the triangles, taking care to use destination indices.
		//triangle normalized normal
		float[] triNormal;
		for ( final Triangle tri : src.triangles() )
		{
			final long v0src = tri.vertex0();
			final long v1src = tri.vertex1();
			final long v2src = tri.vertex2();
			final long v0 = vIndexMap.getOrDefault( v0src, v0src );
			final long v1 = vIndexMap.getOrDefault( v1src, v1src );
			final long v2 = vIndexMap.getOrDefault( v2src, v2src );
			triNormal = triNormals.get( tri.index() );
			dest.triangles().add( v0, v1, v2, triNormal[ 0 ], triNormal[ 1 ], triNormal[ 2 ] );
		}
	}

	/**
	 * Simplifies a given mesh. Normals and uv coordinates will be ignored and
	 * not added to the output mesh.
	 *
	 * @param mesh
	 *            Source mesh
	 * @param target_percent
	 *            the amount in percent to attempt to achieve. For example:
	 *            0.25f would result in creating a mesh with 25% of triangles
	 *            contained in the original.
	 * @param agressiveness
	 *            sharpness to increase the threshold. 5..8 are good numbers.
	 *            more iterations yield higher quality. Minimum 4 and maximum 20
	 *            are recommended.
	 * @return the simplified mesh The result will not include normals or uv
	 *         coordinates.
	 */
	public static Mesh simplify( final Mesh mesh, final float target_percent, final float agressiveness )
	{
		return new SimplifyMesh( mesh ).simplify( target_percent, agressiveness );
	}

	/**
	 * Creates a new mesh from a given mesh without any duplicate vertices.
	 * Normals and uv coordinates will be ignored and not added to the output
	 * mesh.
	 *
	 * @param mesh
	 *            Source mesh
	 * @param precision
	 *            decimal digits to take into account when comparing mesh
	 *            vertices
	 * @return new mesh without duplicate vertices. The result will not include
	 *         normals or uv coordinates.
	 */
	public static BufferMesh removeDuplicateVertices( final Mesh mesh, final int precision )
	{
		return RemoveDuplicateVertices.calculate( mesh, precision );
	}

	/**
	 * Creates mesh e.g. from IterableRegion by using the marching cubes
	 * algorithm.
	 *
	 * @param source
	 *            The binary input image for the marching cubes algorithm.
	 * @return The result mesh of the marching cubes algorithm.
	 */
	public static < T extends BooleanType< T > > Mesh marchingCubes( final RandomAccessibleInterval< T > source )
	{
		return MarchingCubesBooleanType.calculate( source );
	}

	/**
	 * Creates mesh e.g. from IterableRegion by using the marching cubes
	 * algorithm.
	 *
	 * @param source
	 *            The input image for the marching cubes algorithm.
	 * @param isoLevel
	 *            The threshold to distinguish between foreground and background
	 *            values.
	 * @return The result mesh of the marching cubes algorithm.
	 */
	public static < T extends RealType< T > > Mesh marchingCubes( final RandomAccessibleInterval< T > source,
			final double isoLevel )
	{
		return MarchingCubesRealType.calculate( source, isoLevel );
	}

	/**
	 * Returns the number of connected components in the specified mesh.
	 * 
	 * @param mesh
	 *            the mesh.
	 * @return the number of connected components.
	 */
	public static int nConnectedComponents( final Mesh mesh )
	{
		return MeshConnectedComponents.n( mesh );
	}

	/**
	 * Returns an iterable over the connected components of the specified mesh.
	 * 
	 * @param mesh
	 *            the mesh.
	 * @return an iterable.
	 */
	public static Iterable< BufferMesh > connectedComponents( final Mesh mesh )
	{
		return MeshConnectedComponents.iterable( mesh );
	}

	/**
	 * Scales the vertices position by the amount specified by the array.
	 * <p>
	 * Note that the position are simply multiplied by the specified scale, so
	 * they are scaled with respect to (0,0,0) which makes this method useful
	 * mainly to have a mesh generated by marching-cube algorithm have
	 * coordinates in physical units.
	 * 
	 * @param mesh
	 *            the mesh to scale.
	 * @param scale
	 *            a <code>double</code> array of at least 3 elements.
	 */
	public static void scale( final Mesh mesh, final double[] scale )
	{
		final Vertices vertices = mesh.vertices();
		final long nVertices = vertices.sizel();
		for ( long i = 0; i < nVertices; i++ )
		{
			final double x = vertices.x( i );
			final double y = vertices.y( i );
			final double z = vertices.z( i );
			vertices.set( i, x * scale[ 0 ], y * scale[ 1 ], z * scale[ 2 ] );
		}
	}

	/**
	 * Translates the vertices position by the amount specified by the array.
	 * <p>
	 * The positions are equal to
	 * <code>new_pos[d] = old_pos[d] + translate[d]</code>.
	 * 
	 * @param mesh
	 *            the mesh to translate.
	 * @param translate
	 *            a <code>double</code> array of at least 3 elements.
	 */
	public static void translate( final Mesh mesh, final double[] translate )
	{
		final Vertices vertices = mesh.vertices();
		final long nVertices = vertices.sizel();
		for ( long i = 0; i < nVertices; i++ )
		{
			final double x = vertices.x( i );
			final double y = vertices.y( i );
			final double z = vertices.z( i );
			vertices.set( i, x + translate[ 0 ], y + translate[ 1 ], z + translate[ 2 ] );
		}
	}

	/**
	 * Translates the vertices position by the amount specified by the array,
	 * then scales the coordinates.
	 * <p>
	 * The positions are equal to
	 * <code>new_pos[d] = (old_pos[d] + translate[d]) * scale[d]</code>.
	 * 
	 * @param mesh
	 *            the mesh to translate.
	 * @param translate
	 *            a <code>double</code> array of at least 3 elements.
	 * @param scale
	 *            a <code>double</code> array of at least 3 elements.
	 */
	public static void translateScale( final Mesh mesh, final double[] translate, final double[] scale )
	{
		final Vertices vertices = mesh.vertices();
		final long nv = vertices.size();
		for ( long i = 0; i < nv; i++ )
		{
			final double x = ( translate[ 0 ] + vertices.x( i ) ) * scale[ 0 ];
			final double y = ( translate[ 1 ] + vertices.y( i ) ) * scale[ 1 ];
			final double z = ( translate[ 2 ] + vertices.z( i ) ) * scale[ 2 ];
			vertices.set( i, x, y, z );
		}
	}

	public static BufferMesh merge( final Iterable< Mesh > meshes )
	{
		int nVertices = 0;
		int nTriangles = 0;
		for ( final Mesh mesh : meshes )
		{
			nVertices += mesh.vertices().size();
			nTriangles += mesh.triangles().size();
		}

		final BufferMesh out = new BufferMesh( nVertices, nTriangles );
		final BufferMesh.Vertices vOut = out.vertices();
		final BufferMesh.Triangles tOut = out.triangles();

		for ( final Mesh mesh : meshes )
		{
			// Add vertices.
			final Vertices vIn = mesh.vertices();
			final int[] inToOutMap = new int[ vIn.size() ];
			for ( int i = 0; i < vIn.size(); i++ )
			{
				final float xf = vIn.xf( i );
				final float yf = vIn.yf( i );
				final float zf = vIn.zf( i );
				final float nxf = vIn.nxf( i );
				final float nyf = vIn.nyf( i );
				final float nzf = vIn.nzf( i );
				final float uf = vIn.uf( i );
				final float vf = vIn.vf( i );
				final int vo = ( int ) vOut.addf( xf, yf, zf, nxf, nyf, nzf, uf, vf );
				inToOutMap[ i ] = vo;
			}

			// Add triangles.
			final Triangles tIn = mesh.triangles();
			for ( int i = 0; i < tIn.size(); i++ )
			{
				final int v0In = tIn.vertex0( i );
				final int v1In = tIn.vertex1( i );
				final int v2In = tIn.vertex2( i );
				final float nxf = tIn.nxf( i );
				final float nyf = tIn.nyf( i );
				final float nzf = tIn.nzf( i );

				final int v0Out = inToOutMap[ v0In ];
				final int v1Out = inToOutMap[ v1In ];
				final int v2Out = inToOutMap[ v2In ];

				tOut.addf( v0Out, v1Out, v2Out, nxf, nyf, nzf );
			}
		}
		return out;
	}

	private Meshes()
	{}
}
