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
