package org.cmdbuild.bim.model.implementation;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import org.cmdbuild.bim.model.Position3d;

public class IfcPosition3d implements Position3d {
	
	private final Vector3d origin;
	private final Matrix3d M;
	
	public IfcPosition3d(Vector3d origin, Vector3d e1, Vector3d e3){
		this.origin = origin;
		Vector3d e2 = new Vector3d();
		e2.cross(e3, e1);
		M = new Matrix3d(e1.x, e2.x, e3.x, e1.y, e2.y, e3.y, e1.z, e2.z, e3.z);
	}

	public IfcPosition3d(Vector3d origin) {
		this.origin = origin; 
		M = new Matrix3d();
		M.setIdentity();
	}

	@Override
	public Vector3d getOrigin() {
		return origin;
	}

	@Override
	public Matrix3d getVersorsMatrix() {
		return M;
	}
	
	public String toString(){
		return "\n" + origin + "\n[\n" + M + "]";
	}

	@Override
	public boolean isValid() {
		return M.determinant() == 1;
	}

	@Override
	public void convertToAbsolutePosition(Vector3d point) {
		M.transform(point);
		point.add(origin);
		
	}

}
