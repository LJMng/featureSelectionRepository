package featureSelection.repository.algorithm.opt.improvedHarmonySearch.func;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.improvedHarmonySearch.GenerationRecord;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.HarmonyFactory;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Procedure algorithm for Optimization Algorithm of <strong>Improved Harmony Search
 * Algorithm</strong> <code>PLUS</code> Attribute Reduction.
 * <p>
 * <strong>PS</strong>: This class is only for analysis in constructing such execution, please
 * use the correspondent {@link ProcedureComponent} tester instead for formal usage.
 * 
 * @author Benjamin_L
 */
public class ImprovedHarmonySearch {
	/**
	 * Reduction procedures using Improved Harmony Search optimization.
	 * 
	 * @param <CollectionItem>
	 * 		{@link Instance} or <code>EquivalenceClass</code>
	 * @param <Hrmny>
	 * 		{@link Harmony} implementation
	 * @param <FValue>
	 * 		{@link FitnessValue} type.
	 * @param <FI>
	 * 		Type of implemented {@link FeatureImportance}.
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param calculationClass
	 * 		{@link Class} of {@link FI}
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Consider equal when the 
	 * 		difference between two sig is less than the given deviation value.
	 * @param collectionList
	 * 		A {@link Collection} of {@link Instance} or EquivalentClass.
	 * @param params
	 * 		{@link ReductionParameters} for Improved Harmony Search.
	 * @param random
	 * 		{@link Random}.
	 * @return An array of integer values sets.
	 * @throws IllegalAccessException if exceptions occur when creating {@link FeatureImportance} instance.
	 * @throws InstantiationException if exceptions occur when creating {@link FeatureImportance} instance.
	 */
	@SuppressWarnings("unchecked")
	public static <FI extends FeatureImportance<Sig>, Sig extends Number, CollectionItem,
					Hrmny extends Harmony<?>, FValue extends FitnessValue<Sig>> Collection<Integer>[]
			reduction(
				Class<? extends FI> calculationClass, Sig sigDeviation,
				Collection<CollectionItem> collectionList, ReductionParameters<Sig, Hrmny, FValue> params,
				Random random
	) throws InstantiationException, IllegalAccessException{
		FI calculation = calculationClass.newInstance();
		// Initiate harmony group in the size of <code>grouSize</code>. For each vector,
		//	 initiate |C| bits with binary values 0 or 1.
		Harmony<?>[] harmonyMemoryGroup = params.getHarmonyInitializationAlg()
												.init(params, random);
		// gBest = hGroup[0];  oldX= hGroup[0] ; gBest.fitness=0; iter = 0
		Harmony<?> gBest = null;
		Fitness<Sig, FValue> bestFitness = null;
		// For each in hGroup.
		Fitness<Sig, FValue> fitness;
		for (int h=0; h<harmonyMemoryGroup.length; h++) {
			// Calculate vector's fitness.
			fitness = params.getRedAlg()
							.fitness(
								calculation, 
//								params.getFitnessClass(),
								harmonyMemoryGroup[h], 
								collectionList,
								params.getAttributes()
							);
			// Inspect.
			harmonyMemoryGroup[h] = params.getRedAlg()
										.inspection(
											calculation,
											sigDeviation,
											harmonyMemoryGroup[h], 
											params.getHarmonyMemorySize(), 
											collectionList, 
											params.getAttributes()
										);
			// if Xi > gBest.fitness, oldX = Xi;
			if (bestFitness==null || fitness.compareToFitness(bestFitness)>0) {
				bestFitness = fitness;
				gBest = HarmonyFactory.copyHarmony(harmonyMemoryGroup[h]);
			}
		}
		// Loop and generate new Harmony Memories.
		double par, bw, upsAndDown;
		GenerationRecord<FValue> generRecord = new GenerationRecord<>();
		generRecord.updateBestFitness(bestFitness, gBest);
		while (generRecord.getGeneration() < params.getIteration() && 
				params.getRedAlg().compareToMaxFitness(bestFitness, params) < 0
		) {
			// iter++;
			generRecord.nextGeneration();
			// for j=1 to |C|.
			params.getParAlg().preProcess();
			params.getBwAlg().preProcess();
			for (int j=0; j<params.getAttributes().length; j++) {
				// PAR = updatePitchAdjustingRate(maxPAR, minPAR, times, iter);
				par = params.getParAlg().getPitchAdjustmentRate();
				// bw = updateBandwidth(maxBw, minBw, times, iter);
				bw = params.getBwAlg().getBandwidth();
				// Loop harmony memory group to update each attribute[j].
				for (int h=0; h<harmonyMemoryGroup.length; h++) {
					// if (rand()<=HMCR). Determin whether to adjust attribute by global best harmony or not.
					if (random.nextDouble() <= params.getHarmonyMemoryConsiderationRate()) {
						// newX[j]=oldX[j]
						if (gBest.containsAttribute(j))	harmonyMemoryGroup[h].addAttribute(j);
						else							harmonyMemoryGroup[h].removeAttribute(j);
						// if(rand()<= PAR), 判断是否进行随机扰动
						if (random.nextDouble() <= par) {
							upsAndDown = bw;
							if (random.nextBoolean())	upsAndDown = 1 - bw;
							// temp = +temp or -temp
							if (upsAndDown > 0.5) {
								if (!harmonyMemoryGroup[h].containsAttribute(j))
									harmonyMemoryGroup[h].addAttribute(j);
							}else {
								if (harmonyMemoryGroup[h].containsAttribute(j))
									harmonyMemoryGroup[h].removeAttribute(j);
							}
						}
					// else 此情况下，新向量随机产生
					}else {
						// newX[j] = lowerPVB + rand()*(upperPVB - lowerPVB)
						double temp = params.getHarmonyInitializationAlg()
											.getMinPossibleValueBoundOfHarmonyBit()
											.doubleValue() + 
										random.nextDouble() * 
										(params.getHarmonyInitializationAlg().getMaxPossibleValueBoundOfHarmonyBit().doubleValue() - 
										 params.getHarmonyInitializationAlg().getMinPossibleValueBoundOfHarmonyBit().doubleValue());
						if (temp > 0.5)	harmonyMemoryGroup[h].addAttribute(j);
						else			harmonyMemoryGroup[h].removeAttribute(j);
					}
				}
				// if( fitnessCount(newX) >= fitnessCount(oldX) ). 更新用于产生新X的向量. 更新全局最优值
				//		oldX = newX;
				for (int h=0; h<harmonyMemoryGroup.length; h++) {
					fitness = params.getRedAlg()
									.fitness(
										calculation, 
//										params.getFitnessClass(),
										harmonyMemoryGroup[h], 
										collectionList,
										params.getAttributes()
									);
					if (fitness.compareToFitness(bestFitness)>0) {
						bestFitness = fitness;
						gBest = HarmonyFactory.copyHarmony(harmonyMemoryGroup[h]);
					}
				}
			}
			// if (conNumber>=conTimes). 若连续conNum次最优值相同，认为已经收敛
			if (generRecord.getConvergence() >= params.getConvergence()) {
				// break. 返回多个解(含最优解)
				break;
			// else if (lastBest == gBest.fitness). 若连续最优值相等
			}else if (Double.compare(
						generRecord.getBestFitness().getValue().doubleValue(), 
						bestFitness.getFitnessValue().getValue().doubleValue()
					)==0
			) {
				// conNumber++;
				generRecord.countConvergence();
				if (gBest.getAttributes().length!=0)	generRecord.addBestFitness(gBest);
			}else {
				// conNumber = 0
				generRecord.resetConvergence();
				// lastBest = maxFitness
				generRecord.updateBestFitness(bestFitness, gBest);
			}
		}
		return getReds(generRecord, params.getAttributes());
	}
	
	/**
	 * Transfer harmony memory group array into attribute list array.
	 * 
	 * @param generRecord
	 * 		{@link GenerationRecord} instance.
	 * @param attributes
	 * 		Attributes of Instance. (starts from 1)
	 * @return An array of attribute lists. (Attribute starts from 1)
	 */
	@SuppressWarnings("rawtypes")
	public static Collection<Integer>[] getReds(GenerationRecord<?> generRecord, int[] attributes){
		@SuppressWarnings("unchecked")
		List<Integer>[] reds = new List[generRecord.getBestHarmonies().size()];
		int i=0;
		for (Harmony<?> hrmny : generRecord.getBestHarmonies()) {
			reds[i] = new ArrayList<>(hrmny.getAttributes().length);
			for (int index: hrmny.getAttributes())	reds[i].add(attributes[index]);
			i++;
		}
		Arrays.sort(reds, new Comparator<List>(){
			@Override public int compare(List o1, List o2) {	return o1.size()-o2.size();	}
		});
		return reds;
	}

}