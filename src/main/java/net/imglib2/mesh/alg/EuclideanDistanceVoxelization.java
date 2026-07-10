/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package net.imglib2.mesh.alg;

import net.imglib2.*;
import net.imglib2.img.Img;
import net.imglib2.iterator.LocalizingIntervalIterator;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.Triangle;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * <p>
 * This is a voxelizer that produces a binary image with values set to true along
 * the surface of the mesh.
 * </p>
 *
 * @author Andrew McCall (University at Buffalo)
 */

public final class EuclideanDistanceVoxelization {

    private EuclideanDistanceVoxelization()
    {
        // NB: Prevent instantiation of utility class.
    }

    /**
     * @param inputMesh a {@link Mesh}
     * @return an {@link Img} containing a voxelization of {@code mesh}
     * @implNote op names="geom.voxelization", label="Geometric: Voxelization of Mesh", priority="100."
     */
    public static Img<BitType> voxelize(Mesh inputMesh)
    {
        return voxelize(inputMesh, 1.0);
    }
    /**
     * @param inputMesh a {@link Mesh}
     * @param wallThickness an optional double value for the thickness of the voxelized mesh in pixels,
     *                      default is 1
     * @return an {@link Img} containing a voxelization of {@code mesh}
     * @implNote op names="geom.voxelization", label="Geometric: Voxelization of Mesh", priority="100."
     */
    public static Img<BitType> voxelize(Mesh inputMesh, Double wallThickness)
    {
        FinalInterval dims = getDimsFromMesh(inputMesh,wallThickness);
        Img< BitType > outImg = Util.getSuitableImgFactory(dims, new BitType()).create(dims.dimensionsAsLongArray());
        RandomAccessibleInterval<BitType> outRAI = Views.translate(outImg, dims.minAsLongArray());
        voxelize(inputMesh,wallThickness,outRAI);
        return outImg;
    }

    /**
     * @param inputMesh a {@link Mesh}
     * @param out the output image (container)
     * @implNote op names="geom.voxelization", label="Geometric: Voxelization of Mesh", priority="100."
     */
    public static <B extends BooleanType<B>>void voxelize(Mesh inputMesh, RandomAccessibleInterval<B> out)
    {
        voxelize(inputMesh, 1.0, out);
    }
    /**
     * @param inputMesh a {@link Mesh}
     * @param wallThickness an optional double value for the thickness of the voxelized mesh in pixels,
     *                      default is 1
     * @param out the output image (container)
     * @implNote op names="geom.voxelization", label="Geometric: Voxelization of Mesh", priority="100."
     */
    public static <B extends BooleanType<B>>void voxelize(Mesh inputMesh, Double wallThickness, RandomAccessibleInterval<B> out)
    {
        if(wallThickness == null) {
            wallThickness = 1.0;
        }

        RandomAccess<B> ra = out.randomAccess();

        double finalWallThickness = wallThickness;
        inputMesh.triangles().forEach((Triangle t) -> {
            Vector3D[] vector3DTriangle = triangleToVector3DTriangle(t);
            Vector3D normal = getNormalizedNormal(vector3DTriangle);
            //check if triangle is degenerate
            if(normal.getX() == Double.NEGATIVE_INFINITY)
                return;
            Interval triangleBox = boundingBox(vector3DTriangle);
            LocalizingIntervalIterator iterator = new LocalizingIntervalIterator(triangleBox);
            while (iterator.hasNext()) {
                //For each point within triangle bounding box, set to true based on Euclidean distance of point to triangle surface
                iterator.fwd();
                if(Intervals.contains(out, iterator.positionAsPoint())) {
                    //attempted use of org.scijava.ops.image.geom.geom3d.TriangularFacet.distanceToPlane() here in place
                    //of pointToTriangleDist(), but the results were not as clean
                    if (pointToTriangleDist(new Vector3D(iterator.getDoublePosition(0),
                                iterator.getDoublePosition(1),
                                iterator.getDoublePosition(2)),
                            vector3DTriangle, normal) <= finalWallThickness /2) {
                        ra.setPositionAndGet(iterator.positionAsPoint()).set(true);
                    }
                }
            }
        });
    }

    private static FinalInterval getDimsFromMesh(Mesh input, Double wallThickness){

        RealInterval bounds = Meshes.boundingBox(input);
        long[] min = new long[3];
        long[] max = new long[3];
        for (int i = 0; i < 3; i++) {
            min[i] = (long)Math.floor(bounds.realMin(i)-wallThickness/2);
            max[i] = (long)Math.ceil(bounds.realMax(i)+wallThickness/2);
        }
        return new FinalInterval(min, max);
    }


    private static Vector3D[] triangleToVector3DTriangle(Triangle t){
        Vector3D[] o = new Vector3D[3];

        o[0] = new Vector3D(t.v0x(), t.v0y(), t.v0z());
        o[1] = new Vector3D(t.v1x(), t.v1y(), t.v1z());
        o[2] = new Vector3D(t.v2x(), t.v2y(), t.v2z());

        return o;
    }

    private static Vector3D getNormalizedNormal(Vector3D[] t){
        Vector3D ab = t[1].subtract(t[0]);
        Vector3D ac = t[2].subtract(t[0]);

        // Find the normal to the plane: n = ab x ac
        Vector3D n = ab.crossProduct(ac);

        // Normalize normal vector
        try{
            n = n.normalize();
        }
        catch(Exception e){
            return  new Vector3D(Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY);  // Triangle is degenerate
        }
        return n;
    }

    private static Interval boundingBox(Vector3D[] t){
        long [] min = new long[3];
        long [] max = new long[3];

        min[0] = (long) Math.floor(Math.min(t[0].getX(), Math.min(t[1].getX(), t[2].getX())));
        min[1] = (long) Math.floor(Math.min(t[0].getY(), Math.min(t[1].getY(), t[2].getY())));
        min[2] = (long) Math.floor(Math.min(t[0].getZ(), Math.min(t[1].getZ(), t[2].getZ())));

        max[0] = (long) Math.ceil(Math.max(t[0].getX(), Math.max(t[1].getX(), t[2].getX())));
        max[1] = (long) Math.ceil(Math.max(t[0].getY(), Math.max(t[1].getY(), t[2].getY())));
        max[2] = (long) Math.ceil(Math.max(t[0].getZ(), Math.max(t[1].getZ(), t[2].getZ())));
        return new FinalInterval(min, max);
    }

    private static double pointToTriangleDist(Vector3D p, Vector3D[] t, Vector3D n){
        Vector3D tPoint = nearestPointInTriangle3D(p, t, n);
        return p.distance(tPoint);
    }

    private static Vector3D nearestPointInTriangle3D(Vector3D p, Vector3D[] t, Vector3D n) {
		/*
		Need to project point 'p' onto the plane of triangle 't' as first step. This allows
		use of the barycentric coordinate system to locate the nearest point in 't.'
		 */
        Vector3D ab = t[1].subtract(t[0]);
        Vector3D ac = t[2].subtract(t[0]);

        //region Use normal to obtain projection of point p onto plane of triangle
        // Project point p onto the plane spanned by a->b and a->c.
        double dist = p.dotProduct(n) - t[0].dotProduct(n);
        Vector3D projection = p.add(n.scalarMultiply(-dist));
        //endregion

        //Define projection of 'p' onto triangle plane as vector relative to point 'a' on triangle
        Vector3D ap = projection.subtract(t[0]);

		/*
		Vector ap can now be compared to vector sides of triangle 't' using dot products to
		determine where it lies in relation to the triangle, as shown in this image:
		https://i.sstatic.net/tPiEB.png
		Regions from this image will be referenced below. Further discussion here:
		https://stackoverflow.com/questions/2924795/fastest-way-to-compute-point-to-triangle-distance-in-3d
		 */

        //region nearest point in triangle t is corners
        final double abDOTap = ab.dotProduct(ap);
        final double acDOTap = ac.dotProduct(ap);

        //#1 in https://i.sstatic.net/tPiEB.png
        if (abDOTap <= 0d && acDOTap <= 0d) return t[0];

        final Vector3D bc = t[2].subtract(t[1]);
        final Vector3D bp = projection.subtract(t[1]);

        final double baDOTbp = ab.negate().dotProduct(bp);
        final double bcDOTbp = bc.dotProduct(bp);
        //#2 in https://i.sstatic.net/tPiEB.png
        if (baDOTbp <= 0d && bcDOTbp <= 0d) return t[1];

        final Vector3D cp = projection.subtract(t[2]);
        final double cbDOTcp = bc.negate().dotProduct(cp);
        final double caDOTcp = ac.negate().dotProduct(cp);
        //#3 in https://i.sstatic.net/tPiEB.png
        if (cbDOTcp <= 0d && caDOTcp <= 0d) return t[2];
        //endregion

        // Compute barycentric coordinates (v, w) of projection point
        double acDOTac = ac.dotProduct(ac);
        double abDOTac = ab.dotProduct(ac);
        double abDOTab = ab.dotProduct(ab);

        double denom = (acDOTac * abDOTab - abDOTac * abDOTac);
        if (Math.abs(denom) < 1.0e-30) {
            return new Vector3D(Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY); // Triangle is degenerate
        }

        double w = (acDOTac * abDOTap - abDOTac * acDOTap)/denom; //coordinate towards b from a
        double v = (abDOTab * acDOTap - abDOTac * abDOTap)/denom; //coordinate towards c from a

        // Check barycentric coordinates
        if ((v >= 0) && (w >= 0) && (v + w <= 1)) {
            // Projection point is in triangle; #0 in https://i.sstatic.net/tPiEB.png
            return projection;
        }

        //region nearest point in triangle t is on side
        if(w <= 0 && v > w){
            //#4 in https://i.sstatic.net/tPiEB.png
            return t[0].add(ab.scalarMultiply(v));
        }

        if(v <= 0 && w > v){
            //#5 in https://i.sstatic.net/tPiEB.png
            return t[0].add(ac.scalarMultiply(w));
        }

        if(v + w > 1){
            //#6 in https://i.sstatic.net/tPiEB.png
            final double scalarValue = bcDOTbp/bc.getNormSq();
            return t[1].add(bc.scalarMultiply(scalarValue));
        }
        //endregion

        if (v <=0 && w <= 0){ //this should be redundant, but for some reason isn't
            //#1 in https://i.sstatic.net/tPiEB.png
            return t[0];
        }
        return new Vector3D(Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY);
    }
}
