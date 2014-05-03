package org.cmdbuild.bim.model;

import java.util.List;

import javax.vecmath.Vector3d;

public interface SpaceGeometry {

	Vector3d getCentroid();

	double getXDim();

	double getYDim();

	double getZDim();

	void setXDim(double xdim);

	void setYDim(double ydim);

	void setZDim(double zdim);
	
	 List<Vector3d> getVertexList();

}
