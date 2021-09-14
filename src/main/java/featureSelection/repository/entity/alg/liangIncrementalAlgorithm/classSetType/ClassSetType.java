package featureSelection.repository.entity.alg.liangIncrementalAlgorithm.classSetType;

public enum ClassSetType {
	/**
	 * Class set contains mixed(previous + new) universe instances.
	 */
	MIXED(0),
	/**
	 * Class set contains previous universe instances only.
	 */
	PREVIOUS(1),
	/**
	 * Class set contains new universe instances only.
	 */
	NEW(2);

	private int code;
	ClassSetType(int code) {
		this.code = code;
	}

	/**
	 * Get the code value of current enum.
	 * 
	 * @return the code
	 */
	public int getCode() {
		return code;
	}
}