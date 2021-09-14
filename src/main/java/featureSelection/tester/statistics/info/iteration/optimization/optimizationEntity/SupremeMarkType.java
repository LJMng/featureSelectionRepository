package featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity;

/**
 * Supreme mark/title for an entity in a specific Optimization.
 * 
 * @author Benjamin_L
 */
public enum SupremeMarkType {
	/**
	 * Supreme as the global best.
	 */
	GLOBAL_BEST, 
	/**
	 * Supreme as the local best.
	 */
	LOCAL_BEST,
	/**
	 * Not supreme.
	 */
	NONE;

	public static boolean isGlobalBest(SupremeMarkType type) {
		return GLOBAL_BEST.equals(type);
	}
	
	public static boolean isLocalBest(SupremeMarkType type) {
		return LOCAL_BEST.equals(type);
	}
}