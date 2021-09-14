package featureSelection.tester.statistics.info.iteration.optimization;

import featureSelection.tester.statistics.info.iteration.IterationBasicInfo;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.OptEntityBasicInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Info. of iteration for Optimization based Feature Selections.
 * 
 * @author Benjamin_L
 *
 * @param <FitnessValue>
 * 		Type of fitness value of a specific Optimization algorithm.
 */
@Getter
@EqualsAndHashCode(callSuper=false)
public class BasicIterationInfo4Optimization<FitnessValue> 
	extends IterationBasicInfo
{
	private Integer convergence;
	
	/**
	 * {@link OptEntityBasicInfo} instance.
	 */
	private OptEntityBasicInfo<FitnessValue>[] optimizationEntityBasicInfo;
	
	public BasicIterationInfo4Optimization(int iteration, int recordNumber, Class<?> recordClass) {
		super(iteration, recordNumber, recordClass);
	}

	public BasicIterationInfo4Optimization<FitnessValue> setConvergence(int convergence){
		this.convergence = convergence;
		return this;
	}
	public BasicIterationInfo4Optimization<FitnessValue> setOptimizationEntityBasicInfo(
			OptEntityBasicInfo<FitnessValue>[] optimizationEntityBasicInfo
	){
		this.optimizationEntityBasicInfo = optimizationEntityBasicInfo;
		return this;
	}
}