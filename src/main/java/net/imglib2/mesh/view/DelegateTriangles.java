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
package net.imglib2.mesh.view;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Triangles;

/**
 * A {@link Triangles} backed up by another {@link Triangles}.
 */
public interface DelegateTriangles extends Triangles
{

	Triangles delegate();

	@Override
	default long sizel()
	{
		return delegate().sizel();
	}

	@Override
	default Mesh mesh()
	{
		return delegate().mesh();
	}

	@Override
	default long vertex0( final long tIndex )
	{
		return delegate().vertex0( tIndex );
	}

	@Override
	default long vertex1( final long tIndex )
	{
		return delegate().vertex1( tIndex );
	}

	@Override
	default long vertex2( final long tIndex )
	{
		return delegate().vertex2( tIndex );
	}

	@Override
	default float nxf( final long tIndex )
	{
		return delegate().nxf( tIndex );
	}

	@Override
	default float nyf( final long tIndex )
	{
		return delegate().nyf( tIndex );
	}

	@Override
	default float nzf( final long tIndex )
	{
		return delegate().nzf( tIndex );
	}

	@Override
	default long addf( final long v0, final long v1, final long v2, final float nx, final float ny, final float nz )
	{
		return delegate().addf( v0, v1, v2, nx, ny, nz );
	}
}
