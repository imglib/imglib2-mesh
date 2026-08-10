package net.imglib2.mesh.view;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Triangles;
import net.imglib2.mesh.Vertices;

public class ReadOnlyMesh implements Mesh
{

	public static final Mesh readOnly( final Mesh in )
	{
		return new ReadOnlyMesh( in );
	}

	private ReadOnlyMesh( final Mesh in )
	{
		this.in = in;
		this.vertices = new ReadOnlyVertices();
		this.triangles = new ReadOnlyTriangles();
	}
	
	private final Mesh in;

	private final ReadOnlyVertices vertices;

	private final ReadOnlyTriangles triangles;

	@Override
	public Vertices vertices()
	{
		return vertices;
	}

	@Override
	public Triangles triangles()
	{
		return triangles;
	}

	private class ReadOnlyVertices implements DelegateVertices
	{

		@Override
		public Vertices delegate()
		{
			return in.vertices();
		}

		@Override
		public long addf( final float x, final float y, final float z )
		{
			throw new UnsupportedOperationException( "Read-only mesh: cannot add vertex." );
		}

		@Override
		public void setf( final long vIndex, final float x, final float y, final float z, final float nx, final float ny, final float nz, final float u, final float v )
		{
			throw new UnsupportedOperationException( "Read-only mesh: cannot set vertex attributes." );
		}

		@Override
		public void setPositionf( final long vIndex, final float x, final float y, final float z )
		{
			throw new UnsupportedOperationException( "Read-only mesh: cannot set vertex position." );
		}

		@Override
		public void setNormalf( final long vIndex, final float nx, final float ny, final float nz )
		{
			throw new UnsupportedOperationException( "Read-only mesh: cannot set vertex normal." );
		}

		@Override
		public void setTexturef( final long vIndex, final float u, final float v )
		{
			throw new UnsupportedOperationException( "Read-only mesh: cannot set vertex texture coordinates." );
		}
	}

	private final class ReadOnlyTriangles implements DelegateTriangles
	{

		@Override
		public Triangles delegate()
		{
			return in.triangles();
		}

		@Override
		public long addf( final float v0x, final float v0y, final float v0z, final float v1x, final float v1y, final float v1z, final float v2x, final float v2y, final float v2z, final float nx, final float ny, final float nz )
		{
			throw new UnsupportedOperationException( "Read-only mesh: cannot add triangle." );
		}


	}

}
