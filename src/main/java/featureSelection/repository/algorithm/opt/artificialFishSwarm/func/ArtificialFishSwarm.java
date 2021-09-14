package featureSelection.repository.algorithm.opt.artificialFishSwarm.func;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import common.utils.RandomUtils;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.artificialFishSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.PositionFactory;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.Fish;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.FitnessAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.FitnessValue;
import org.apache.commons.math3.util.FastMath;

/**
 * Procedure algorithm for Optimization Algorithm of <strong>Artificial Fish Swarm Algorithm</strong> 
 * <code>PLUS</code> Feature Selection.
 * <p>
 * <strong>PS</strong>: This class is only for analysis in constructing such execution, please use the 
 * correspondent {@link ProcedureComponent} tester instead for formal usage.
 * 
 * @author Benjamin_L
 */
public class ArtificialFishSwarm {

	/**
	 * Reduction procedure.
	 * 
	 * @param <Cal>
	 * 		Type of implemented {@link FeatureImportance}.
	 * @param <Sig>
	 * 		Type of Feature(subset) Significance.
	 * @param <CollectionItem>
	 * 		{@link Instance} or EquivalentClass.
	 * @param <FV>
	 * 		Type of implemented {@link FitnessValue}.
	 * @param <PosiValue>
	 * 		Type of {@link Position} value.
	 * @param <Posi>
	 * 		Type of implemented {@link Position}.
	 * @param collectionList
	 * 		A {@link List} of {@link Instance} / <code>EquivalenceClass</code>.
	 * @param calculationClass
	 * 		{@link Class} of implemented {@link FeatureImportance}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Consider equal when the 
	 * 		difference between two sig is less than the given deviation value.
	 * @param params
	 * 		{@link ReductionParameters} for optimization algorithm.
	 * @param attributesLength
	 * 		The number of {@link Instance} attributes.
	 * @param random
	 * 		{@link Random}.
	 * @return An array of {@link Integer} {@link List} as reducts.
	 * @throws IllegalAccessException if exceptions occur when creating {@link FeatureImportance} instance.
	 * @throws InstantiationException if exceptions occur when creating {@link FeatureImportance} instance.
	 */
	@SuppressWarnings("unchecked")
	public static <Cal extends FeatureImportance<Sig>, Sig extends Number, CollectionItem,
					FV extends FitnessValue<? extends Number>, PosiValue,
					Posi extends Position<PosiValue>>
		Collection<Integer>[] reduction(
			Collection<CollectionItem> collectionList, Class<Cal> calculationClass, Sig sigDeviation,
			ReductionParameters params, int attributesLength, Random random
	) throws InstantiationException, IllegalAccessException {
		Cal calculation = calculationClass.newInstance();
		// Initiate generation record :
		GenerationRecord<Posi, Sig> generRecord =
				initGenerationRecord(
						calculation,
						collectionList,
						params, 
						attributesLength
				);
		// Fish swarm
		int fishExitCount;
		while (generRecord.getGeneration() < params.getIteration()) {
			// iter++
			generRecord.nextGeneration();
			// Generate |groupSize| fishes. Loop all fishes and initiate position. 
			Fish<Posi>[] fishGroup =
					params.getFishGroupUpdateAlgorithm()
							.generateFishGroup(
								params.getGroupSize(), 
								attributesLength, 
								params, 
								random
							);
			// Loop and do update.
			int loop = 0;
			UpdateLoop:
			while (true) {
				update(
					calculation,
					sigDeviation,
					collectionList,
					fishGroup, 
					params,
					generRecord, 
					attributesLength, 
					random
				);
				loop++;
				// Loop until all fishes exit or reach max update iteration.
				if (params.getMaxFishUpdateIteration()>0 && 
					params.getMaxFishUpdateIteration()<=loop
				) {
					break;
				}else {
					fishExitCount = 0;
					for (int i=0; i<fishGroup.length; i++) {
						if (params.getMaxFishExit()>0) {
							if (fishGroup[i].isExited())	fishExitCount++;
							if (params.getMaxFishExit()<=fishExitCount)	break;
							else if (i==fishGroup.length-1)	continue UpdateLoop;
						}else {
							if (!fishGroup[i].isExited())	continue UpdateLoop;
						}
					}
					break;
				}
			}
		}
		return fishGroup2Reducts(generRecord);
	}
	
	/**
	 * Initiate fish generation records.
	 * 
	 * @param <Cal>
	 * 		Type of {@link FeatureImportance}
	 * @param <Sig>
	 * 		Type of feature importance.
	 * @param <CollectionItem>
	 * 		{@link Instance} or <code>EquivalenceClass</code>
	 * @param <FV>
	 * 		The value of fitness.
	 * @param <Posi>
	 * 		Implemented {@link Position}.
	 * @param calculation
	 * 		{@link Cal} instance
	 * @param collectionList
	 * 		A {@link Collection} of {@link CollectionItem}.
	 * @param params
	 * 		{@link ReductionParameters} of <code>Artificial Fish Swarm Algorithm</code>.
	 * @param attributesLength
	 * 		The number of condition attributes of {@link Instance}.
	 * @return Initiated {@link GenerationRecord}.
	 */
	@SuppressWarnings("unchecked")
	public static <Cal extends FeatureImportance<Sig>, Sig extends Number, CollectionItem, 
					FV extends FitnessValue<? extends Number>, Posi extends Position<?>> 
			GenerationRecord<Posi, Sig> 
		initGenerationRecord(
				Cal calculation, Collection<CollectionItem> collectionList, 
				ReductionParameters params, int attributesLength
	) {
		// gBest.minR = ones array;  
		Posi position = (Posi) PositionFactory.fullPosition(
								params.getPositionClass(), 
								attributesLength
						);
		// gBest.minL = |C| ; gBest.fitness=0;
		GenerationRecord<Posi, Sig> generRecord = new GenerationRecord<>();
		generRecord.setLeastAttr(attributesLength);
		generRecord.updateBestFeatureSignificance(position, null);
		// Calculate the Dependency(C|D)
		generRecord.setGlobalDependency(
			params.getReductionAlgorithm()
					.dependency(
						calculation, 
						collectionList, 
						position
					).doubleValue()
		);
		return generRecord;
	}
	
	/**
	 * Update fishes' position by searching, swarming, following.
	 * 
	 * @param <CollectionItem>
	 * 		{@link Instance} or <code>EquivalenceClass</code>
	 * @param <Posi>
	 * 		Class type of {@link Position}.
	 * @param <FV>
	 * 		The type of {@link Fitness}.
	 * @param collectionList
	 * 		A {@link Collection} of {@link Instance} / <code>EquivalenceClass</code>
	 * @param fishes
	 * 		An array of {@link Fish} as fish group.
	 * @param generRecord
	 * 		Artificial Fish Swarm Algorithm {@link GenerationRecord}.
	 * @param params
	 * 		{@link ReductionParameters} of Artificial Fish Swarm Algorithm.
	 * @param attributesLength
	 * 		The length of condition attributes.
	 * @param random
	 * 		{@link Random}.
	 */
	@SuppressWarnings("unchecked")
	public static <Cal extends FeatureImportance<Sig>, Sig extends Number, CollectionItem, PosiValue, 
					Posi extends Position<PosiValue>, FV extends FitnessValue<? extends Number>> 
		void update(
			Cal calculation, Sig sigDeviation,
			Collection<CollectionItem> collectionList, Fish<Posi>[] fishes, ReductionParameters params, 
			GenerationRecord<Posi, Sig> generRecord, int attributesLength, Random random
	) {
		// Loop fishes without exit mark.
		int fishExited = 0;
		Fitness<Posi, FV> bestFitness;
		Posi searchPos, swarmPos, followPos;
		for (int i=0; i<fishes.length; i++) {
			if (params.getMaxFishExit()>0 && fishExited>=params.getMaxFishExit()) {
				break;
			}else if (!fishes[i].isExited()) {
				// searchPos = search()
				searchPos = (Posi) search(
								calculation,
								collectionList,
								fishes[i], 
								attributesLength,
								params.getTryNumbers(),
								params.getFitnessAlgorthm(), 
								params.getReductionAlgorithm(),
								random
							);
				// swarmPos = swarm()
				swarmPos = (Posi) params.getFishSwarmAlgorithm()
										.swarm(
											calculation, 
											collectionList,
											fishes[i], 
											fishes, 
											params
										);
				if (swarmPos==null) {
					swarmPos = (Posi) search(
									calculation,
									collectionList,
									fishes[i], 
									attributesLength,
									params.getTryNumbers(),
									params.getFitnessAlgorthm(), 
									params.getReductionAlgorithm(),
									random
								);
				}
				// followPos  = follow()
				followPos = (Posi) params.getFishFollowAlgorithm()
										.follow(
											calculation, 
											collectionList, 
											fishes[i], 
											fishes, 
											params
										);
				if (followPos==null) {
					followPos = (Posi) search(
									calculation,
									collectionList,
									fishes[i], 
									attributesLength,
									params.getTryNumbers(),
									params.getFitnessAlgorthm(), 
									params.getReductionAlgorithm(),
									random
								);
				}
				// currentFish.pos = maxFitness(searchPos, swarmPos, followPos)
				bestFitness = params.getFitnessAlgorthm()
									.findBestFitness(
										params.getFitnessAlgorthm()
												.calculateFitness(
													params.getReductionAlgorithm(),
													searchPos.getAttributes(),
													params.getReductionAlgorithm()
															.dependency(
																calculation, 
																collectionList, 
																searchPos.getAttributes()
													)
												),
										params.getFitnessAlgorthm()
												.calculateFitness(
													params.getReductionAlgorithm(),
													swarmPos.getAttributes(),
													params.getReductionAlgorithm()
															.dependency(
																calculation, 
																collectionList, 
																swarmPos.getAttributes()
													)
												),
										params.getFitnessAlgorthm()
												.calculateFitness(
													params.getReductionAlgorithm(),
													followPos.getAttributes(),
													params.getReductionAlgorithm()
															.dependency(
																calculation, 
																collectionList, 
																followPos.getAttributes()
													)
												)
									);
				fishes[i].setPosition(bestFitness.getPosition());
				// currentFish.len = sum(the number of 1 in currentFish.pos)
				// currentFish.fitness = fitnessCount(currentFish.pos)
				// if (dep(currentFish.posi)==dep(C|D)
				if (Double.compare(
						FastMath.abs(
							generRecord.getGlobalDependency()
							-
							bestFitness.getFitnessValue().getDependency().doubleValue()
						),
						sigDeviation.doubleValue()
					)<=0
				) {
					// currentFish.end = true
					fishes[i].exit();
					if (params.getMaxFishExit()>0)	fishExited++;
					// update global best
					generRecord.updateBestFeatureSignificance(
							bestFitness.getPosition(), 
							(Sig) bestFitness.getFitnessValue().getDependency()
					);
				}
			}
		}
	}
	
	/**
	 * Fish searching behavior, following steps:
	 * <p>1. Determine the <code>tryNumber</code>(i.e. the search time);
	 * <p>2. Extracts current attributes not used: <code>extraAttributes</code>;
	 * <p>3. Calculate current fitness: <code>currentFishFitness</code>;
	 * <p>4. Using a {@link RandomUtils.ShuffleArrayIterator} for attributes random selection,
	 * 		maintain <code>examAttributes</code> to calculate trial incremental fitness.
	 * 		Search until incremental fitness is greater than <code>currentFishFitness</code>
	 * <p>5. Return search result.
	 * 
	 * @param <Cal>
	 * 		The type of {@link FeatureImportance} for feature importance measure.
	 * @param <Sig>
	 * 		Implemented {@link Number} for feature importance measure value.
	 * @param <CollectionItem>
	 * 		{@link Instance} or Equivalence Class.
	 * @param <FV>
	 * 		The type of Fitness.
	 * @param <PosiValue>
	 * 		Class type of {@link Position} value.
	 * @param <Posi>
	 * 		Class extends {@link Position}.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance}.
	 * @param collectionList
	 * 		A {@link Collection} of {@link Instance} / EquivalenceClass.
	 * @param fish
	 * 		{@link Fish}.
	 * @param positionLength
	 * 		The length of position.
	 * @param tryNumbers
	 * 		The try number of searching. <code>-1</code> as <strong>auto</strong>.
	 * @param fitnessAlgorithm
	 * 		Implemented {@link FitnessAlgorithm}.
	 * @param redAlg
	 * 		{@link ReductionAlgorithm} for dependency calculation.
	 * @param random
	 * 		{@link Random} instance.
	 * @return {@link Posi}
	 */
	public static <Cal extends FeatureImportance<Sig>, Sig extends Number, CollectionItem, 
					FV extends FitnessValue<? extends Number>, PosiValue, 
					Posi extends Position<PosiValue>> Posi 
		search(
			Cal calculation,
			Collection<CollectionItem> collectionList, Fish<Posi> fish, int positionLength, int tryNumbers,
			FitnessAlgorithm<Cal, Sig, FV, Posi> fitnessAlgorithm,
			ReductionAlgorithm<Cal, Sig, CollectionItem> redAlg, Random random
	) {
		// i=0, nextPos = POS
		// Rt = Rk, Rp = Rk
		int[] attributes = fish.getPosition().getAttributes();
		// * If user has set the param tryNumbers, follow user's will.
		if (tryNumbers>0)	tryNumbers = Math.min(positionLength-attributes.length, tryNumbers);
		// * If user hasn't set the param tryNumbers. Set it automatically.
		else 				tryNumbers = positionLength-attributes.length;
		if (tryNumbers==0)	return null;

		@SuppressWarnings("unchecked")
		Posi posi = (Posi) fish.getPosition().clone();
		
		// extract attribute candidates.
		int[] extraAttributes = new int[positionLength-attributes.length];
		for (int ext=0, p=1; p<=positionLength && ext<extraAttributes.length; p++)
			if (!posi.containsAttribute(p))	extraAttributes[ext++] = p;
		
		Fitness<Posi, FV> currentFishFitness =
				fitnessAlgorithm.calculateFitness(
						redAlg, 
						fish.getPosition().getAttributes(),
						(Sig) redAlg.dependency(
							calculation,
							collectionList,
							fish.getPosition()
						)
				);
		// Loop and add position value until fitnessCount(nextPos) > currentFish.fitness or i>= tryNumbers
		int[] examAttributes = Arrays.copyOf(attributes, attributes.length+1);
		RandomUtils.ShuffleArrayIterator shuffledAttrIterator = new RandomUtils.ShuffleArrayIterator(extraAttributes, random);
		for (int i=0; i<tryNumbers && shuffledAttrIterator.hasNext(); i++) {
			// Select a feature a[k] in C-Rp randomly. Rt = {Rk U a[k]}. (Rt=nextPos)
			examAttributes[examAttributes.length-1] = shuffledAttrIterator.next();
			// if fitnessCount(nextPos) > currentFish.fitness, break.
			Fitness<Posi, FV> updatedFitness = 
					fitnessAlgorithm.calculateFitness(
							redAlg, 
							examAttributes,
							redAlg.dependency(
								calculation, 
								collectionList, 
								examAttributes
							)
					);
			if (fitnessAlgorithm.compareFitnessValue(
					updatedFitness.getFitnessValue(), 
					currentFishFitness.getFitnessValue()
				)>0
			)	break;
		}
		// If	attribute with greater fitness was found, add into posi as nextPos
		// else	add the last attribute in "extraAttributes" into posi as nextPos
		// then	return nextPos
		if (examAttributes[examAttributes.length-1]==0) {
			if (extraAttributes[extraAttributes.length-1]<=0)
				throw new RuntimeException("invalid attribute : "+extraAttributes[extraAttributes.length-1]);
			posi.addAttributeInPosition(extraAttributes[extraAttributes.length-1]);
		}else {
			if (examAttributes[examAttributes.length-1]<=0)
				throw new RuntimeException("invalid attribute : "+examAttributes[examAttributes.length-1]);
			posi.addAttributeInPosition(examAttributes[examAttributes.length-1]);
		}
		return posi;
	}
	
	/**
	 * Transfer {@link GenerationRecord} into reducts.
	 * 
	 * @param <Posi>
	 * 		Type of implemented {@link Position}.
	 * @param generRecord
	 * 		{@link GenerationRecord}.
	 * @return An array of {@link Integer} {@link List} as reducts.
	 */
	public static <Posi extends Position<?>> Collection<Integer>[] fishGroup2Reducts(
			GenerationRecord<Posi, ?> generRecord
	) {
		@SuppressWarnings("unchecked")
		Collection<Integer>[] reds = new Collection[generRecord.getBestFitnessPosition().size()];
		int i=0;
		for (Posi posi : generRecord.getBestFitnessPosition()) {
			reds[i] = new ArrayList<>(posi.getAttributes().length);
			for (int attr : posi.getAttributes())	reds[i].add(attr);
			i++;
		}
		Arrays.sort(reds, new Comparator<Collection<Integer>>() {
			@Override public int compare(Collection<Integer> o1, Collection<Integer> o2) {
				if (o1==null && o2==null)	return 0;
				else if (o1==null)			return -1;
				else if (o2==null)			return 1;
				else						return (o1.size()-o2.size());
			}
		});
		return reds;
	}

	public static <Posi extends Position<?>> Collection<Fish<Posi>> getFishWithinVisual(
			final Fish<Posi> fish, Fish<Posi>[] fishGroup, ReductionParameters params
	){
		return Arrays.stream(fishGroup).parallel().filter(f->{
					if (f==fish || fish.getPosition().equals(f.getPosition())) {
						return false;
					}else {
						@SuppressWarnings("unchecked")
						double distance = params.getDistanceCount()
												.distance(fish.getPosition(), f.getPosition())
												.doubleValue();
						return Double.compare(distance, params.getVisual().doubleValue())<0;
					}
			}).collect(Collectors.toList());
	}
}