package org.cmdbuild.services.gis;

import org.cmdbuild.model.data.Card;
import org.postgis.Geometry;

public class GeoFeature {

	final Geometry geometry;
	final Long ownerCardId;
	final Long classIdOfOwnerCard;
	final String classNameOfOwnerCard;

	public GeoFeature(final Geometry geometry, final Card ownerCard) {
		super();
		this.geometry = geometry;
		this.ownerCardId = ownerCard.getId();
		this.classIdOfOwnerCard = ownerCard.getClassId();
		this.classNameOfOwnerCard = ownerCard.getClassName();
	}

	public GeoFeature( //
			final Geometry geometry, //
			final Long ownerCardId, //
			final Long classIdOfOwnerCard, //
			final String classNameOfOwnerCard) {
		this.geometry = geometry;
		this.ownerCardId = ownerCardId;
		this.classIdOfOwnerCard = classIdOfOwnerCard;
		this.classNameOfOwnerCard = classNameOfOwnerCard;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public Long getOwnerCardId() {
		return ownerCardId;
	}

	public Long getClassIdOfOwnerCard() {
		return classIdOfOwnerCard;
	}

	public String getClassNameOfOwnerCard() {
		return classNameOfOwnerCard;
	}

}
