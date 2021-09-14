package featureSelection.repository.entity.opt.genetic;

import featureSelection.basic.model.optimization.OptimizationAlgorithm;
import featureSelection.basic.model.optimization.OptimizationParameters;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;

import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.opt.genetic.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.ChromosomeCrossAlgorithm;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.initlization.ChromosomeInitialization;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.initlization.ChromosomeInitializationParameters;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.mutation.ChromosomeMutation;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.mutation.ChromosomeMutationParameters;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import lombok.Data;

/**
 * {@link OptimizationParameters} for <strong>Genetic Algorithm</strong>. With following fields:
 * <ul>
 * 	<li><code>population</code></li>
 * 	<li><code>chromosomeLength</code></li>
 * 	<li><code>chromosomeSwitchNum</code></li>
 * 	<li><code>reserveNum</code></li>
 * 	<li><code>iterateNum</code></li>
 * 	<li><code>convergenceLimit</code></li>
 * 	<li><code>maxFitness</code></li>
 * 	<li><code>maxDistinctBestFitness</code></li>
 * 	<li><code>singleSolutionOnly</code></li>
 * 	<li><code>chromosomeInitAlgorithm</code></li>
 * 	<li><code>mutationAlgorithm</code></li>
 * 	<li><code>crossAlgorithm</code></li>
 * 	<li><code>reductionAlgorithm</code></li>
 * </ul>
 * 
 * @param <Cal>
 * 		Implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <Item>
 * 		Type of dataset items. {@link Instance}, {@link EquivalenceClass}, etc..
 * @param <Chr>
 * 		Implemented {@link Chromosome}.
 * @param <FValue>
 * 		Implemented {@link FitnessValue}.
 * 
 * @author Benjamin_L
 */
@Data
public class ReductionParameters<Cal extends FeatureImportance<Sig>,
								Sig extends Number, 
								Item, 
								Chr extends Chromosome<?>,
								FValue extends FitnessValue<?>>
	implements OptimizationParameters
{
	/**
	 * The number of Chromosome
	 * <p>
	 * 种群大小
	 */
	private int population;
	/**
	 * The length of a {@link Chromosome}'s gene.
	 * <p>
	 * 染色体长度/属性个数
	 */
	private int chromosomeLength;
	/**
	 * The number of switch {@link Chromosome} gene.
	 * <p>
	 * 染色体交换位数
	 */
	private int chromosomeSwitchNum;
	/**
	 * Reserve number of {@link Chromosome} at each iteration.
	 * <p>
	 * 原种群保留数量
	 */
	private int reserveNum;
	/**
	 * The maximum iteration number.
	 * <p>
	 * 迭代次数n
	 */
	private int iterateNum;
	/**
	 * The max convergence times.
	 * <p>
	 * 认为收敛的次数
	 */
	private int convergenceLimit;
	/**
	 * The max fitness value. <code>null</code> if auto-setting.
	 * <p>
	 * fitness 最大值
	 */
	private FValue maxFitness;
	/**
	 * The max number of distinct fitnesses/feature subset. <code>null</code> as no limitation.
	 * <p>
	  * 最大best fitness个数(不重复feature subset)
	 */
	private Integer maxDistinctBestFitness;
	/**
	 * If get multiple solutions, return one only.
	 */
	private Boolean singleSolutionOnly;
	
	/**
	 * Chromosome initiate algorithm instance.
	 * <p>
	 * 染色体初始化算法
	 */
	private ChromosomeInitialization<Chr, ? extends ChromosomeInitializationParameters<Chr>> chromosomeInitAlgorithm;
	/**
	 * Chromosome mutation algorithm instance.
	 * <p>
	 * 染色体变异算法
	 */
	private ChromosomeMutation<Chr, ? extends ChromosomeMutationParameters<Chr>> mutationAlgorithm;
	/**
	 * Chromosome cross-over algorithm instance.
	 * <p>
	 * 染色体交叉算法
	 */
	private ChromosomeCrossAlgorithm<Chr, FValue> crossAlgorithm;
	/**
	 * Implemented {@link ReductionAlgorithm}.
	 */
	private ReductionAlgorithm<Cal, Sig, Item, Chr, FValue> reductionAlgorithm;
	
	public void setChromosomeSwitchNum(int chromosomeSwitchNum) {
		if (chromosomeSwitchNum<=0)	chromosomeSwitchNum = 1;
		this.chromosomeSwitchNum = chromosomeSwitchNum;
	}

	@Override
	public OptimizationAlgorithm getOptimizationAlgorithm() {
		return reductionAlgorithm;
	}


	@Override
	public ReductionParameters<Cal, Sig, Item, Chr, FValue> clone() throws CloneNotSupportedException {
		ReductionParameters<Cal, Sig, Item, Chr, FValue> clone = new ReductionParameters<Cal, Sig, Item, Chr, FValue>();
		clone.setPopulation(population);
		clone.setChromosomeLength(chromosomeLength);
		clone.setChromosomeSwitchNum(chromosomeSwitchNum);
		clone.setReserveNum(reserveNum);
		clone.setIterateNum(iterateNum);
		clone.setConvergenceLimit(convergenceLimit);
		clone.setMaxFitness(maxFitness);
		clone.setMaxDistinctBestFitness(maxDistinctBestFitness);
		clone.setSingleSolutionOnly(singleSolutionOnly);
		
		clone.setChromosomeInitAlgorithm(chromosomeInitAlgorithm);
		clone.setMutationAlgorithm(mutationAlgorithm);
		clone.setCrossAlgorithm(crossAlgorithm);
		clone.setReductionAlgorithm(reductionAlgorithm);
		
		return clone;
	}
}