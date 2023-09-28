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

package net.imglib2.mesh.io.xyz;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;

/**
 * Routines for reading XYZ files.
 * 
 * @author Curtis Rueden
 */
public final class XYZPointsIO
{

	private XYZPointsIO()
	{
		// NB: Prevent instantiation of utility class.
	}

	public static final List< RealLocalizable > open( final String source ) throws IOException
	{
		final ArrayList< RealLocalizable > points = new ArrayList<>();

		try (final BufferedReader br = new BufferedReader( new FileReader( source ) ))
		{
			String line;
			while ( ( line = br.readLine() ) != null )
			{
				if ( line.isEmpty() || line.startsWith( "#" ) )
					continue;
				final String[] parts = line.trim().split( "[\\s,]+" );
				if ( parts.length < 3 )
				{
					//log.warn( "Invalid line: " + line );
					continue;
				}
				final double x, y, z;
				try
				{
					x = Double.parseDouble( parts[ 0 ] );
					y = Double.parseDouble( parts[ 1 ] );
					z = Double.parseDouble( parts[ 2 ] );
				}
				catch ( final NumberFormatException e )
				{
					//log.warn( "Invalid line: " + line );
					continue;
				}
				points.add( new RealPoint( x, y, z ) );
			}
		}

		return points;
	}
}
