package net.imglib2.mesh;

import net.imglib2.mesh.impl.nio.BufferMesh;

/**
 * Utility class providing example meshes for testing purposes.
 */
public class MeshExamples
{

	public static BufferMesh createTetrahedron()
	{
		final BufferMesh mesh = new BufferMesh( 4, 4 );

		mesh.vertices().add( 0., 0., 0. ); // V0
		mesh.vertices().add( 1., 0., 0. ); // V1
		mesh.vertices().add( 0., 2., 0. ); // V2
		mesh.vertices().add( 0., 0., 3. ); // V3

		mesh.triangles().add( 0, 2, 1 );
		mesh.triangles().add( 0, 1, 3 );
		mesh.triangles().add( 0, 3, 2 );
		mesh.triangles().add( 1, 2, 3 );

		return mesh;
	}
}
