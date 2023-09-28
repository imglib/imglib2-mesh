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

package net.imglib2.mesh.io.stl;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.scijava.util.FileUtils;

import com.google.common.base.Strings;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Triangle;
import net.imglib2.mesh.impl.naive.NaiveFloatMesh;

/**
 * Routines for reading and writing STL files.
 * 
 * @author Richard Domander (Royal Veterinary College, London)
 * @author Curtis Rueden
 */
public final class STLMeshIO
{

	private STLMeshIO()
	{
		// NB: Prevent instantiation of utility class.
	}

	public static final int HEADER_BYTES = 80;

	public static final String HEADER = Strings.padEnd( "Binary STL created with ImageJ", HEADER_BYTES, '.' );

	public static final int COUNT_BYTES = 4;

	public static final int FACET_START = HEADER_BYTES + COUNT_BYTES;

	public static final int FACET_BYTES = 50;

	public static final void read( final Mesh mesh, final byte[] data )
	{
		if ( data.length < FACET_START )
			return;

		final ByteBuffer buffer = ByteBuffer.wrap( data ).order(
				ByteOrder.LITTLE_ENDIAN );
		final int facetCount = buffer.getInt( HEADER_BYTES );
		final int expectedSize = HEADER_BYTES + COUNT_BYTES + facetCount *
				FACET_BYTES;
		if ( expectedSize != buffer.capacity() )
			return;

		buffer.position( FACET_START );
		for ( int offset = FACET_START; offset < buffer.capacity(); offset +=
				FACET_BYTES )
		{
			readFacet( mesh, buffer );
		}
	}

	/** Writes the facets into a byte[] that can then be saved into a file */
	public static final byte[] write( final Mesh mesh )
	{
		final long facetCount = mesh == null ? 0 : mesh.triangles().sizel();
		final long longBytes = HEADER_BYTES + COUNT_BYTES + facetCount * FACET_BYTES;
		if ( longBytes > Integer.MAX_VALUE )
		{ throw new IllegalArgumentException( "Too many triangles: " + facetCount ); }
		final int bytes = ( int ) longBytes;
		final ByteBuffer buffer = ByteBuffer.allocate( bytes ).order(
				ByteOrder.LITTLE_ENDIAN );

		buffer.put( HEADER.getBytes() );
		buffer.putInt( ( int ) facetCount );

		if ( mesh == null )
			return buffer.array();

		mesh.triangles().forEach( f -> writeFacet( buffer, f ) );

		return buffer.array();
	}

	public static final void read( final Mesh mesh, final File stlFile ) throws IOException
	{
		if ( stlFile == null )
			return;

		final byte[] data = Files.readAllBytes( //
				Paths.get( stlFile.getAbsolutePath() ) );

		read( mesh, data );
	}

	public static final Mesh open( final String source ) throws IOException
	{
		final Mesh mesh = new NaiveFloatMesh();
		read( mesh, new File( source ) );
		return mesh;
	}

	public static final void save( final Mesh data, final String destination ) throws IOException
	{
		final byte[] bytes = write( data );
		FileUtils.writeFile( new File( destination ), bytes );
	}

	// -- Helper methods --

	private final static void writeFacet( final ByteBuffer buffer, final Triangle facet )
	{
		// TODO Blend vertices
		writeVector( buffer, facet.nxf(), facet.nyf(), facet.nzf() );
		writeVector( buffer, facet.v0xf(), facet.v0yf(), facet.v0zf() );
		writeVector( buffer, facet.v1xf(), facet.v1yf(), facet.v1zf() );
		writeVector( buffer, facet.v2xf(), facet.v2yf(), facet.v2zf() );
		buffer.putShort( ( short ) 0 ); // Attribute byte count
	}

	private static void writeVector( final ByteBuffer buffer, final float x, final float y, final float z )
	{
		buffer.putFloat( x );
		buffer.putFloat( y );
		buffer.putFloat( z );
	}

	private static void readFacet( final Mesh mesh, final ByteBuffer buffer )
	{
		final float nx = buffer.getFloat();
		final float ny = buffer.getFloat();
		final float nz = buffer.getFloat();
		final float v0x = buffer.getFloat();
		final float v0y = buffer.getFloat();
		final float v0z = buffer.getFloat();
		final float v1x = buffer.getFloat();
		final float v1y = buffer.getFloat();
		final float v1z = buffer.getFloat();
		final float v2x = buffer.getFloat();
		final float v2y = buffer.getFloat();
		final float v2z = buffer.getFloat();
		@SuppressWarnings( "unused" )
		final short attributeByteCount = buffer.getShort();// sorry TODO

		mesh.triangles().addf( //
				v0x, v0y, v0z, //
				v1x, v1y, v1z, //
				v2x, v2y, v2z, //
				nx, ny, nz );
	}
}
