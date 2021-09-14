package featureSelection.repository.entity.alg.rec.classSet.type;

import lombok.Getter;

public enum ClassSetType {
	/**
	 * 1-REC
	 */
	POSITIVE(1, "POS"), 
	/**
	 * -1-REC
	 */
	NEGATIVE(-1, "NEG"), 
	/**
	 * 0-REC
	 */
	BOUNDARY(0, "BND");
	
	@Getter private int code;
	@Getter private String abbreviation;
	
	ClassSetType(int code, String abbreviation) {
		this.code = code;
		this.abbreviation = abbreviation;
	}

	public boolean isPositive() {	return POSITIVE.equals(this);	}
	public boolean isNegative() {	return NEGATIVE.equals(this);	}
	public boolean isBoundary()	{	return BOUNDARY.equals(this);	}
}