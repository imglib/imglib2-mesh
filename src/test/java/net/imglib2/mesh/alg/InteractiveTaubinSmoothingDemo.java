package net.imglib2.mesh.alg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.joml.Matrix4f;

import bvv.core.VolumeViewerPanel;
import bvv.core.util.MatrixMath;
import bvv.vistools.Bvv;
import bvv.vistools.BvvFunctions;
import bvv.vistools.BvvHandle;
import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.alg.TaubinSmoothing.TaubinWeightType;
import net.imglib2.mesh.obj.Mesh;
import net.imglib2.mesh.obj.nio.BufferMesh;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class InteractiveTaubinSmoothingDemo
{

	public static void main( final String[] args ) throws IOException
	{

		// Make a cube
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 100, 100, 100 );
		final FinalInterval cube = Intervals.createMinMax( 25, 25, 25, 75, 75, 75 );
		Views.interval( img, cube ).forEach( p -> p.setOne() );

		final Mesh m1 = Meshes.marchingCubes( img, 0.5 );
		final BufferMesh m2 = Meshes.removeDuplicateVertices( m1, 2 );

		// Add noise.
		final Random ran = new Random( 45l );
		for ( int i = 0; i < m2.vertices().size(); i++ )
		{
			final double x = m2.vertices().x( i );
			final double y = m2.vertices().y( i );
			final double z = m2.vertices().z( i );

			m2.vertices().set( i,
					x + ran.nextDouble() - 0.5,
					y + ran.nextDouble() - 0.5,
					z + ran.nextDouble() - 0.5 );
		}

		// Calculate normals.
		final BufferMesh mesh = new BufferMesh( m2.vertices().isize(), m2.triangles().isize() );
		Meshes.calculateNormals( m2, mesh );

		final List< StupidMesh > meshes = new ArrayList<>();
		final StupidMesh sm = new StupidMesh( mesh );
		sm.setColor( Color.WHITE, 1f );
		sm.setSelectionColor( Color.GREEN, 1f );
		meshes.add( sm );

		final Bvv bvv = BvvFunctions.show(
				img,
				"Cube",
				Bvv.options()
						.maxAllowedStepInVoxels( 0 )
						.renderWidth( 1024 )
						.renderHeight( 1024 )
						.preferredSize( 512, 512 )
						.frameTitle( "Interactive test Taubin smoothing" ) );
		final BvvHandle handle = bvv.getBvvHandle();
		final VolumeViewerPanel viewer = handle.getViewerPanel();
		viewer.setRenderScene( ( gl, data ) -> {
			final Matrix4f pvm = new Matrix4f( data.getPv() );
			final Matrix4f view = MatrixMath.affine( data.getRenderTransformWorldToScreen(), new Matrix4f() );
			final Matrix4f vm = MatrixMath.screen( data.getDCam(), data.getScreenWidth(), data.getScreenHeight(), new Matrix4f() ).mul( view );
			meshes.forEach( s -> s.draw( gl, pvm, vm, true ) );
		} );

		final SpinnerNumberModel spinnerModel = new SpinnerNumberModel( 0.1, 0., 1, 0.01 );
		spinnerModel.addChangeListener( e -> {
			final double passBand = spinnerModel.getNumber().doubleValue();
			final BufferMesh smoothed = ( passBand > 0 )
					? TaubinSmoothing.smooth( mesh, 10, passBand, -passBand, TaubinWeightType.NAIVE )
					: mesh;

			final StupidMesh sm2 = new StupidMesh( smoothed );
			sm2.setColor( Color.WHITE, 1f );
			sm2.setSelectionColor( Color.GREEN, 1f );
			meshes.clear();
			meshes.add( sm2 );
			viewer.requestRepaint();
		} );

		final JSpinner spinner = new JSpinner( spinnerModel );
		final JPanel main = new JPanel( new BorderLayout() );
		main.add( spinner );
		final JFrame frame = new JFrame( "Spatial frequency of smoothing" );
		frame.getContentPane().add( main );
		frame.pack();
		frame.setLocationRelativeTo( viewer );
		frame.setVisible( true );
	}

}
