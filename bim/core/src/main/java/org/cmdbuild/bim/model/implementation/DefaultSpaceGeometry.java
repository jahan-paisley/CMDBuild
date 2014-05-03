package org.cmdbuild.bim.model.implementation;

import java.util.List;

import javax.vecmath.Vector3d;

import org.cmdbuild.bim.model.SpaceGeometry;

import com.google.common.collect.Lists;

public class DefaultSpaceGeometry implements SpaceGeometry {
	
	@Deprecated
	private Vector3d centroid;
	
	private final double height;
	
	@Deprecated
	private List<Double> dimensions = Lists.newArrayList();
	private final List<Vector3d> vertexList = Lists.newArrayList();
	
	@Deprecated
	public DefaultSpaceGeometry(Vector3d centroid, double xdim, double ydim, double zdim) {
		vertexList.add(new Vector3d(centroid.x - xdim / 2, centroid.y - ydim / 2, centroid.z));
		vertexList.add(new Vector3d(centroid.x + xdim / 2, centroid.y - ydim / 2, centroid.z));
		vertexList.add(new Vector3d(centroid.x + xdim / 2, centroid.y + ydim / 2, centroid.z));
		vertexList.add(new Vector3d(centroid.x - xdim / 2, centroid.y + ydim / 2, centroid.z));
		vertexList.add(new Vector3d(centroid.x - xdim / 2, centroid.y - ydim / 2, centroid.z + zdim));
		vertexList.add(new Vector3d(centroid.x + xdim / 2, centroid.y - ydim / 2, centroid.z + zdim));
		vertexList.add(new Vector3d(centroid.x + xdim / 2, centroid.y + ydim / 2, centroid.z + zdim));
		vertexList.add(new Vector3d(centroid.x - xdim / 2, centroid.y + ydim / 2, centroid.z + zdim));
		height = zdim;
	}

	public DefaultSpaceGeometry(List<Vector3d> polylineProfile, double dz) {
		for (Vector3d vector3d : polylineProfile) {
			vertexList.add(vector3d);
		}
		height = dz;
	}
	
	
	public DefaultSpaceGeometry() {
		centroid = new Vector3d(0, 0, 0);
		dimensions.add(new Double(0));
		dimensions.add(new Double(0));
		dimensions.add(new Double(0));
		height = 0;
	}

	public String toString() {
		return "Centroid: " + centroid + " Dimensions: " + dimensions;
	}
	
	@Deprecated
	@Override
	public Vector3d getCentroid() {
		return centroid;
	}
	
	@Deprecated
	@Override
	public double getXDim() {
		return dimensions.get(0);
	}
	
	@Deprecated
	@Override
	public double getYDim() {
		return dimensions.get(1);
	}

	@Override
	public double getZDim() {
		return height;
	}

	@Override
	public void setXDim(double xdim) {
		dimensions.set(0, xdim);
	}
	
	@Deprecated
	@Override
	public void setYDim(double ydim) {
		dimensions.set(1, ydim);

	}

	@Deprecated
	@Override
	public void setZDim(double zdim) {
		dimensions.set(1, zdim);

	}

	@Override
	public List<Vector3d> getVertexList() {
		return vertexList;
	}
}
