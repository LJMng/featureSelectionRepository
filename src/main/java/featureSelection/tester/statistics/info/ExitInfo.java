package featureSelection.tester.statistics.info;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExitInfo {
	private int iteration;
	private int convergence;
	private double fitness;
	
	private String exitMark;
	
	public final static String EXIT_MARK_ITERATION = "MaxIter.";
	public final static String EXIT_MARK_CONVERGENCE = "MaxConv.";
	public final static String EXIT_MARK_REACH_MAX_FITNESS = "MaxFitness";
}