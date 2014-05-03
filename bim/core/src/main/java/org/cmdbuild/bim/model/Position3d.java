package org.cmdbuild.bim.model;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

public interface Position3d {

	Vector3d getOrigin();

	Matrix3d getVersorsMatrix();

	boolean isValid();

	final Position3d DEFAULT_POSITION = new Position3d() {

		@Override
		public Vector3d getOrigin() {
			return new Vector3d(0.0, 0.0, 0.0);
		}

		@Override
		public Matrix3d getVersorsMatrix() {
			final Matrix3d M = new Matrix3d();
			M.setIdentity();
			return M;
		}

		@Override
		public boolean isValid() {
			return getVersorsMatrix().determinant() == 1;
		}

		@Override
		public String toString() {
			return getOrigin().toString();
		}

		@Override
		public void convertToAbsolutePosition(final Vector3d point) {
			// nothing to do
		}

	};

	void convertToAbsolutePosition(Vector3d point);

}
