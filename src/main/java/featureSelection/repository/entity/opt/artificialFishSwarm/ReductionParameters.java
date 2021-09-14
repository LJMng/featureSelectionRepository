package featureSelection.repository.entity.opt.artificialFishSwarm;

import featureSelection.basic.model.optimization.OptimizationAlgorithm;
import featureSelection.basic.model.optimization.OptimizationParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.distance.DistanceAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.FishCenterCalculationAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.action.FishFollowAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.action.FishGroupUpdateAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.action.FishSwarmAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.FitnessAlgorithm;
import lombok.Data;

/**
 * {@link OptimizationParameters} for ArtificialFishSwarm.
 * <p><strong>Notice</strong>:
 * <p><code>tryNumbers=-1</code> if <code>auto</code> in the searching step of FSA.
 * <p><code>maxFishExit=-1</code> if not setting any exit threshold. (i.e. all fishes exit)
 * 
 * @author Benjamin_L
 */
@Data
public class ReductionParameters 
	implements OptimizationParameters
{
	/**
	 * The number of fish in group.
	 * <p>
	 * 种群大小
	 */
	private int groupSize;
	/**
	 * Visual of a fish: <code>int</code>-number, <code>double</code>-proportion.
	 * <p>
	 * 鱼视野
	 */
	private Number visual;
	/**
	 * Crowd factor.
	 * <p>
	 * 拥挤因子
	 */
	private double cFactor;
	/**
	 * Try numbers in a searching action.
	 * <p>
	 * Search中的连续尝试次数
	 */
	private int tryNumbers;
	/**
	 * The maximum iteration.
	 * <p>
	 * 最大叠代次数
	 */
	private int iteration;
	/**
	 * Maximum number of fitting fish. When reaching the number, algorithm exits.
	 * <p>
	 * 最大鱼退出个数
	 */
	private int maxFishExit;
	/**
	 * Maximum iteration for a fish to update itself by performing actions before an exit.
	 * <p>
	 * -1 for no limit.
	 */
	private int maxFishUpdateIteration;
	
	/**
	 * The distance calculation for 2 positions(in this case, {@link Position})
	 */
	@SuppressWarnings("rawtypes")
	private DistanceAlgorithm distanceCount;
	/**
	 * Fitness calculation/measuring algorithm.
	 */
	@SuppressWarnings("rawtypes")
	private FitnessAlgorithm fitnessAlgorthm;		// 计算fitness的方法
	
	/**
	 * Fish <code>update</code> action algorithm.
	 */
	@SuppressWarnings("rawtypes")
	private FishGroupUpdateAlgorithm fishGroupUpdateAlgorithm;
	/**
	 * Fish <code>swarm</code> action algorithm.
	 */
	@SuppressWarnings("rawtypes")
	private FishSwarmAlgorithm fishSwarmAlgorithm;
	/**
	 * Fish <code>follow</code> action algorithm.
	 */
	@SuppressWarnings("rawtypes")
	private FishFollowAlgorithm fishFollowAlgorithm;
	/**
	 * Fish center calculation algorithm.
	 */
	@SuppressWarnings("rawtypes")
	private FishCenterCalculationAlgorithm fishCenterCalculationAlgorithm;
	
	private Class<? extends Position<?>> positionClass;

	/**
	 * Feature selection implemented {@link ReductionAlgorithm}.
	 */
	@SuppressWarnings("rawtypes")
	private ReductionAlgorithm reductionAlgorithm;

	@Override
	public OptimizationAlgorithm getOptimizationAlgorithm() {
		return reductionAlgorithm;
	}


	@Override
	public ReductionParameters clone() throws CloneNotSupportedException {
		ReductionParameters clone = new ReductionParameters();
		clone.setGroupSize(groupSize);
		clone.setVisual(visual);
		clone.setCFactor(cFactor);
		clone.setTryNumbers(tryNumbers);
		clone.setIteration(iteration);
		clone.setMaxFishExit(maxFishExit);
		
		clone.setDistanceCount(distanceCount);
		clone.setFitnessAlgorthm(fitnessAlgorthm);
		
		clone.setFishGroupUpdateAlgorithm(fishGroupUpdateAlgorithm);
		clone.setFishSwarmAlgorithm(fishSwarmAlgorithm);
		clone.setFishFollowAlgorithm(fishFollowAlgorithm);
		clone.setFishCenterCalculationAlgorithm(fishCenterCalculationAlgorithm);
		
		clone.setPositionClass(positionClass);
		clone.setReductionAlgorithm(reductionAlgorithm);
		
		return clone;
	}
}