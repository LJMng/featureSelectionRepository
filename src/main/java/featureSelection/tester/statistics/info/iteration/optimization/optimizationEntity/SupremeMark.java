package featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity;

import lombok.Getter;

/**
 * Supreme mark/title for an entity in a specific Optimization.
 * 
 * @author Benjamin_L
 */
public class SupremeMark {
	@Getter private SupremeMarkType supremeMarkType;
	@Getter private long rank;
	
	private static long globalBestCounter = 0L;
	public static void resetGlobalBestCounter() {	globalBestCounter = 0L;	}
	
	private SupremeMark(SupremeMarkType supremeMarkType, long rank) {
		this.supremeMarkType = supremeMarkType;
		this.rank = rank;
	}
	
	public boolean differ(SupremeMark sm) {
		if (sm==this)																return false;
		if (sm.rank!=rank)															return true;
		if (sm.supremeMarkType!=null && supremeMarkType==null)						return true;
		if (sm.supremeMarkType==null && supremeMarkType!=null)						return true;
		if (sm.supremeMarkType!=null && !sm.supremeMarkType.equals(supremeMarkType))return true;
		return false;
	}
	
	public static SupremeMark nextGlobalBest() {
		return new SupremeMark(SupremeMarkType.GLOBAL_BEST, ++globalBestCounter);
	}
	
	public static SupremeMark nextLocalBest() {
		return new SupremeMark(SupremeMarkType.LOCAL_BEST, 1);
	}
	
	public static SupremeMark nextNone() {
		return new SupremeMark(SupremeMarkType.NONE, 0);
	}

	
	@Override
	public String toString() {
		if (supremeMarkType==null) {
			return "Type=null";
		}else {
			return "Type="+supremeMarkType + " (rank="+rank+")";
		}
	}
}