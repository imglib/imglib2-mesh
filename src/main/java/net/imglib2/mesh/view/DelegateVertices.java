package net.imglib2.mesh.view;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Vertices;

/**
 * A {@link Vertices} backed up by another {@link Vertices}.
 * <p>
 * Inspired by DelegateRealLocalizable and co. from Mastodon.
 */
public interface DelegateVertices extends Vertices
{

	/**
	 * The vertices that backs up this vertices.
	 * 
	 * @return the wrapped vertices.
	 */
	Vertices delegate();

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
	default float xf( final long vIndex )
	{
		return delegate().xf( vIndex );
	}

	@Override
	default float yf( final long vIndex )
	{
		return delegate().yf( vIndex );
	}

	@Override
	default float zf( final long vIndex )
	{
		return delegate().zf( vIndex );
	}

	@Override
	default float nxf( final long vIndex )
	{
		return delegate().nxf( vIndex );
	}

	@Override
	default float nyf( final long vIndex )
	{
		return delegate().nyf( vIndex );
	}

	@Override
	default float nzf( final long vIndex )
	{
		return delegate().nzf( vIndex );
	}

	@Override
	default float uf( final long vIndex )
	{
		return delegate().uf( vIndex );
	}

	@Override
	default float vf( final long vIndex )
	{
		return delegate().vf( vIndex );
	}

	@Override
	default long addf( final float x, final float y, final float z, //
			final float nx, final float ny, final float nz, //
			final float u, final float v )
	{
		return delegate().addf( x, y, z, nx, ny, nz, u, v );
	}

	@Override
	default void setf( final long vIndex, final float x, final float y, final float z, //
			final float nx, final float ny, final float nz, //
			final float u, final float v )
	{
		delegate().setf( vIndex, x, y, z, nx, ny, nz, u, v );
	}

	@Override
	default void setPositionf( final long vIndex, final float x, final float y, final float z )
	{
		delegate().setPositionf( vIndex, x, y, z );
	}

	@Override
	default void setNormalf( final long vIndex, final float nx, final float ny, final float nz )
	{
		delegate().setNormalf( vIndex, nx, ny, nz );
	}

	@Override
	default void setTexturef( final long vIndex, final float u, final float v )
	{
		delegate().setTexturef( vIndex, u, v );
	}

}
