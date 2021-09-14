package featureSelection.tester.utils;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.ArrayUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.model.optimization.AttributeEncoding;
import featureSelection.basic.model.optimization.OptimizationAlgorithm;
import featureSelection.basic.model.optimization.OptimizationParameters;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.Procedure;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportGenerated;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.basic.support.calculation.featureImportance.DependencyCalculation;
import featureSelection.basic.support.calculation.featureImportance.InConsistencyCalculation;
import featureSelection.basic.support.calculation.featureImportance.PositiveRegionCalculation;
import featureSelection.basic.support.calculation.featureImportance.entropy.CombinationConditionEntropyCalculation;
import featureSelection.basic.support.calculation.featureImportance.entropy.LiangConditionEntropyCalculation;
import featureSelection.basic.support.calculation.featureImportance.entropy.ShannonConditionEnpropyCalculation;
import featureSelection.basic.support.calculation.featureImportance.entropy.mutualInformation.MutualInformationEntropyCalculation;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.iteration.heuristic.BasicIterationInfo4Heuristic;
import featureSelection.tester.statistics.info.iteration.optimization.BasicIterationInfo4Optimization;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.OptEntityBasicInfo;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.SupremeMarkType;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;

import com.alibaba.fastjson.JSONObject;

/**
 * Utilities for {@link ProcedureContainer} and {@link ProcedureComponent}.
 *
 * @author Benjamin_L
 */
@UtilityClass
public class ProcedureUtils {

	/**
	 * Check if "exit" mark in {@link ProcedureParameters}.
	 *
	 * @param procedureParameters
	 *      {@link ProcedureParameters} instance that contains Procedure parameters.
	 * @return <code>true</code> if the "exit" mark is in the {@link ProcedureParameters} and marked
	 *      as true.
	 * @see ParameterConstants#PARAMETER_PROCEDURE_EXIT_MARK
	 */
	public static boolean procedureExitMark(ProcedureParameters procedureParameters) {
		Boolean exitMark = procedureParameters.get(ParameterConstants.PARAMETER_PROCEDURE_EXIT_MARK);
		return exitMark != null && exitMark;
	}

	/**
	 * Get the correspondent abbreviation of names.
	 */
	public static class ShortName {

		/**
		 * Extract {@link FeatureImportance} info.(class/instance) from
		 * {@link ProcedureParameters} who marks {@link FeatureImportance} class as
		 * {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS} and instance
		 * as {@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}.
		 *
		 * @see ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS
		 * @see ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE
		 *
		 * @param parameters
		 *      {@link ProcedureParameters} instance that contains Procedure parameters.
		 * @return "UNKNOWN" if failed to extract info. / The name of implemented
		 *      {@link FeatureImportance}.
		 */
		public static String calculation(ProcedureParameters parameters) {
			Class<? extends Calculation<?>> calculationClass =
					parameters.get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS);
			Calculation<?> calculationInstance =
					parameters.get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE);

			if (calculationClass != null) {
				try {
					return calculationClass.getField("CALCULATION_NAME")
							.get(calculationClass)
							.toString();
				} catch (IllegalArgumentException | IllegalAccessException |
						NoSuchFieldException | SecurityException e
				) {
					return calculation(calculationClass);
				}
			} else if (calculationInstance != null) {
				try {
					return calculationInstance.getClass()
							.getField("CALCULATION_NAME")
							.get(calculationInstance.getClass())
							.toString();
				} catch (IllegalArgumentException | IllegalAccessException |
						NoSuchFieldException | SecurityException e
				) {
					return calculation((Class<? extends Calculation<?>>) calculationInstance.getClass());
				}
			} else {
				return "UNKNOWN";
			}
		}

		/**
		 * Get the name of {@link FeatureImportance}: (checked in order)
		 * <ul>
		 * 	<li>{@link InConsistencyCalculation}</li>
		 * 	<li>{@link ShannonConditionEnpropyCalculation}</li>
		 * 	<li>{@link LiangConditionEntropyCalculation}</li>
		 * 	<li>{@link CombinationConditionEntropyCalculation}</li>
		 * 	<li>{@link MutualInformationEntropyCalculation}</li>
		 * 	<li>{@link PositiveRegionCalculation}</li>
		 * 	<li>{@link DependencyCalculation}</li>
		 * 	<li>etc.</li>
		 * </ul>
		 *
		 * @param calculationClass
		 *      Implemented {@link FeatureImportance} class.
		 * @return <code>CALCULATION_NAME</code> of the {@link FeatureImportance} interface.
		 */
		public static String calculation(Class<? extends Calculation<?>> calculationClass) {
			if (InConsistencyCalculation.class.isAssignableFrom(calculationClass)) {
				return InConsistencyCalculation.CALCULATION_NAME;
			} else if (ShannonConditionEnpropyCalculation.class.isAssignableFrom(calculationClass)) {
				return ShannonConditionEnpropyCalculation.CALCULATION_NAME;
			} else if (LiangConditionEntropyCalculation.class.isAssignableFrom(calculationClass)) {
				return LiangConditionEntropyCalculation.CALCULATION_NAME;
			} else if (CombinationConditionEntropyCalculation.class.isAssignableFrom(calculationClass)) {
				return CombinationConditionEntropyCalculation.CALCULATION_NAME;
			} else if (MutualInformationEntropyCalculation.class.isAssignableFrom(calculationClass)) {
				return MutualInformationEntropyCalculation.CALCULATION_NAME;
			} else if (PositiveRegionCalculation.class.isAssignableFrom(calculationClass)) {
				return PositiveRegionCalculation.CALCULATION_NAME;
			} else if (DependencyCalculation.class.isAssignableFrom(calculationClass)) {
				return DependencyCalculation.CALCULATION_NAME;
			} else {
				return "UNKNOWN";
			}
		}

		/**
		 * Check if set obtaining core in {@link ProcedureParameters}.
		 *
		 * @see ParameterConstants#PARAMETER_QR_EXEC_CORE
		 *
		 * @param parameters
		 *      {@link ProcedureParameters} instance.
		 * @return "Core" if set. / "NoCore" if not.
		 */
		public static String byCore(ProcedureParameters parameters) {
			Boolean byCore = parameters.get(ParameterConstants.PARAMETER_QR_EXEC_CORE);
			if (byCore != null) return byCore ? "Core" : "NoCore";
			return "NoCore";
		}

		/**
		 * Extract Optimization Algorithm info. from {@link OptimizationAlgorithm#shortName()}
		 * in {@link OptimizationParameters} which is marked as {@link ParameterConstants#
		 * PARAMETER_OPTIMIZATION_PARAMETERS}.
		 *
		 * @param parameters {@link ProcedureParameters} instance that contains Procedure parameters.
		 * @return "UNKNOWN" if failed to extract info./the short name of Optimization
		 * Algorithm.
		 * @see OptimizationAlgorithm#shortName()
		 * @see ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS
		 */
		public static String optimizationAlgorithm(ProcedureParameters parameters) {
			OptimizationParameters optParams =
					parameters.get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);

			if (optParams != null) {
				if (optParams.getOptimizationAlgorithm() != null) {
					return optParams.getOptimizationAlgorithm().shortName();
				}
			}
			return "UNKNOWN";
		}
	}

	/**
	 * Utilities for time counting in {@link TimeCountedProcedureComponent}.
	 */
	public static class Time {
		public static final String SUM_TIME = "ProcedureUtils.SUM";

		/**
		 * Sum the total time of the given {@link ProcedureComponent} and all its
		 * sub-containers(i.e. {@link ProcedureComponent#getSubProcedureContainers()})'s
		 * components too.
		 *
		 * @see TimeCounted
		 *
		 * @param component
		 *      A {@link ProcedureComponent} with at least one component implemented
		 *      {@link TimeCounted}.
		 * @return Sum time of {@link ProcedureComponent}s.
		 */
		public static long sumProcedureComponentTimes(ProcedureComponent<?> component) {
			long time = 0L;
			if (component instanceof TimeCounted) {
				time += ((TimeCounted) component).getTime();
			}

			if (component.getSubProcedureContainers() != null &&
					!component.getSubProcedureContainers().isEmpty()
			) {
				for (ProcedureContainer<?> container :
						component.getSubProcedureContainers().values()
				) {
					for (ProcedureComponent<?> com: container.getComponents()) {
						time += sumProcedureComponentTimes(com);
					}
				}
			}
			return time;
		}

		/**
		 * Sum the total time of the given {@link ProcedureComponent} and all its
		 * sub-containers(i.e. {@link ProcedureComponent#getSubProcedureContainers()})'s
		 * components too grouped by <strong>Tag</strong>.
		 *
		 * @see #sumProcedureComponentsTimesByTags(ProcedureContainer, Map)
		 * @see Time#SUM_TIME
		 *
		 * @param container
		 *      A {@link ProcedureContainer} with tagged {@link ProcedureComponent}s.
		 * @return A sum time {@link Map} whose keys are tags and values are the corresponding sum time.
		 */
		public static Map<String, Long> sumProcedureComponentsTimesByTags(
				ProcedureContainer<?> container
		) {
			// Group by tag
			final Map<String, Long> timeMap =
					sumProcedureComponentsTimesByTags(container, new HashMap<>());
			// Total time.
			long sum = timeMap.values().stream().reduce(Long::sum).orElse(0L);
			timeMap.put(SUM_TIME, sum);
			return timeMap;
		}

		/**
		 * Sum the total time of the given {@link ProcedureComponent} and all its
		 * sub-containers(i.e. {@link ProcedureComponent#getSubProcedureContainers()})'s
		 * components too with the same <strong>Tag</strong> and accumulating into the
		 * given {@link Map} <code>timeMap</code>.
		 *
		 * @param container A {@link ProcedureContainer} with tagged {@link ProcedureComponent}s.
		 * @param timeMap   A {@link Map} stores accumulated time with tags as keys.
		 * @return A sum time {@link Map} whose keys are tags and values are the
		 * corresponding sum time.
		 */
		private static Map<String, Long> sumProcedureComponentsTimesByTags(
				ProcedureContainer<?> container, Map<String, Long> timeMap
		) {
			Long time;
			for (ProcedureComponent<?> component : container.getComponents()) {
				if (component instanceof TimeCounted) {
					time = timeMap.get(component.getTag());
					if (time == null){
						time = 0L;
					}
					time += ((TimeCounted) component).getTime();
					timeMap.put(component.getTag(), time);
				}

				if (component.getSubProcedureContainers() != null &&
						!component.getSubProcedureContainers().isEmpty()
				) {
					for (ProcedureContainer<?> subContainer :
							component.getSubProcedureContainers().values()
					) {
						sumProcedureComponentsTimesByTags(subContainer, timeMap);
					}
				}
			}
			return timeMap;
		}
	}

	/**
	 * Utilities for statistics in {@link ProcedureContainer}.
	 */
	public static class Statistics {

		/**
		 * Do int counting.
		 *
		 * @param statistics
		 *      A {@link Map} to contain statistics data.
		 * @param key
		 *      The key to count int.
		 * @param increment
		 *      Increment for the counting.
		 * @return <code>statistics</code>
		 */
		public static Map<String, Object> countInt(
				Map<String, Object> statistics, String key, int increment
		) {
			Integer count = (Integer) statistics.get(key);
			if (count == null) {
				count = 0;
			}
			statistics.put(key, count + increment);
			return statistics;
		}

		/**
		 * Push a value into the {@link LinkedList} in {@link Map} with <code>key</code>.
		 *
		 * @param <V>
		 *      Type of the value.
		 * @param statistics
		 *      A {@link Map} to contain statistics data.
		 * @param key
		 *      The key to store the {@link LinkedList}.
		 * @param value
		 *      The value to push.
		 * @return <code>statistics</code>
		 */
		public static <V> Map<String, Object> push(
				Map<String, Object> statistics, String key, V value
		) {
			@SuppressWarnings("unchecked")
			Collection<V> collection = (Collection<V>) statistics.get(key);
			if (collection == null){
				statistics.put(key, collection = new LinkedList<>());
			}
			collection.add(value);
			return statistics;
		}

		/**
		 * Utilities for recording iteration info.s for {@link ProcedureComponent}.
		 *
		 * @see BasicIterationInfo4Optimization
		 */
		public static class IterationInfos {

			/**
			 * Store convergence value for the last {@link BasicIterationInfo4Optimization}
			 * in {@link StatisticsConstants.Procedure#STATISTIC_ITERATION_INFOS} in
			 * <code>statistics</code>.
			 *
			 * @param statistics
			 *      A {@link Map} to contain statistics data.
			 * @param conv
			 *      The convergence value to record.
			 */
			public static void updateConvergenceInfo(Map<String, Object> statistics, int conv) {
				@SuppressWarnings("unchecked")
				List<BasicIterationInfo4Optimization<?>> iterInfos =
						(List<BasicIterationInfo4Optimization<?>>)
						statistics.get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS);

				BasicIterationInfo4Optimization<?> iterInfo = iterInfos.get(iterInfos.size() - 1);
				iterInfo.setConvergence(conv);
			}


			/**
			 * Push Iteration Info.s into <code>statistics</code>(
			 * {@link StatisticsConstants.Procedure#STATISTIC_ITERATION_INFOS}).
			 * <p>
			 * For <strong>heuristic</strong> only.
			 *
			 * @param <CollectionItem>
			 *     Type of collection item of dataset: {@link Instance} or Equivalence Class.
			 * @param <Sig>
			 *     Type of feature (subset) significance.
			 * @param <AdditionalAttr>
			 *     Type of additional attribute(s), can be {@link Integer}, {@link int[]} or
			 *     {@link Integer} {@link Collection}.
			 * @param <SigCalLength>
			 *     Type of length of feature(s) use in significance calculation.
			 * @param statistics
			 *      A {@link Map} to contain statistics data.
			 * @param collectionItems
			 *      A {@link CollectionItem} {@link Collection}.
			 * @param currentIteration
			 *      Current iteration number/generation.
			 * @param currentReductLength
			 *      Length of current reduct after adding the <code>additionalAttr</code> for
			 *      the iteration.
			 * @param currentSig
			 *      Significance of current reduct after adding the <code>additionalAttr</code> for
			 *      the iteration.
			 * @param additionalAttr
			 *      Attribute(s) selected for adding into the reduct.
			 * @param sigCalLength
			 *      The length of attribute(s) used in calculating significance for this
			 *      iteration.
			 * @return {@link BasicIterationInfo4Heuristic} {@link List}.
			 */
			public static <CollectionItem, Sig extends Number, AdditionalAttr, SigCalLength>
					List<BasicIterationInfo4Heuristic<Sig, AdditionalAttr, SigCalLength>>
				pushInfoOfIteration(
					Map<String, Object> statistics, Collection<CollectionItem> collectionItems,
					int currentIteration, int currentReductLength, Sig currentSig,
					AdditionalAttr additionalAttr, SigCalLength sigCalLength
			) {
				@SuppressWarnings("unchecked")
				List<BasicIterationInfo4Heuristic<Sig, AdditionalAttr, SigCalLength>> collection =
						(List<BasicIterationInfo4Heuristic<Sig, AdditionalAttr, SigCalLength>>)
						statistics.get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS);
				if (collection == null) {
					statistics.put(
							StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS,
							collection = new LinkedList<>()
					);
				}

				BasicIterationInfo4Heuristic<Sig, AdditionalAttr, SigCalLength> iterInfo;
				collection.add(
						iterInfo = new BasicIterationInfo4Heuristic<>(
								currentIteration,
								collectionItems.size(),
								collectionItems.iterator().next().getClass()
						)
				);
				iterInfo.setCurrentReductLength(currentReductLength);
				iterInfo.setAdditionalAttribute(additionalAttr);
				iterInfo.setSignificanceCalculationAttributeLength(sigCalLength);
				iterInfo.setSignificance(currentSig);

				return collection;
			}

			/**
			 * Update the last {@link BasicIterationInfo4Heuristic} in
			 * {@link StatisticsConstants.Procedure#STATISTIC_ITERATION_INFOS} with
			 * <code>removedUniverseInsatnce</code> and <code>removedRecord</code>.
			 *
			 * @see StatisticsConstants.Procedure#STATISTIC_ITERATION_INFOS
			 * @see BasicIterationInfo4Heuristic#getInstanceRemoved()
			 * @see BasicIterationInfo4Heuristic#getRecordRemoved()
			 *
			 * @param <Sig>
			 *      Type of feature (subset) significance.
			 * @param <AdditionalAttr>
			 *      Type of additional attribute(s), can be {@link Integer}, {@link int[]} or
			 *      {@link Integer} {@link Collection}.
			 * @param <SigCalLength>
			 *      Type of length of feature(s) use in significance calculation.
			 * @param statistics
			 *      A {@link Map} to contain statistics data.
			 * @param removedInstance
			 *      The number of removed {@link Instance}s.
			 * @param removedRecord
			 *      The number of removed records.
			 */
			public static <Sig extends Number, AdditionalAttr, SigCalLength> void
				updateRemoves4HeuristicStreamlining(
					Map<String, Object> statistics, int removedInstance, Integer removedRecord
			) {
				@SuppressWarnings("unchecked")
				LinkedList<BasicIterationInfo4Heuristic<Sig, AdditionalAttr, SigCalLength>> iterInfos =
						(LinkedList<BasicIterationInfo4Heuristic<Sig, AdditionalAttr, SigCalLength>>)
						statistics.get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS);

				BasicIterationInfo4Heuristic<Sig, AdditionalAttr, SigCalLength> lastIterInfo =
						iterInfos.getLast();
				lastIterInfo.setInstanceRemoved(removedInstance);
				lastIterInfo.setRecordRemoved(removedRecord);
			}


			/**
			 * Push Iteration Info.s into <code>statistics</code>(
			 * {@link StatisticsConstants.Procedure#STATISTIC_ITERATION_INFOS}).
			 *
			 * @see #pushInfoOfIteration(Map, int, int, Collection, int[], AttributeEncoding[],
			 *      Iterator, Number[])
			 *
			 * @param <CollectionItem>
			 *      {@link Instance} or Equivalence Classes.
			 * @param <Encoded>
			 *      Type of {@link AttributeEncoding}.
			 * @param statistics
			 *      A {@link Map} to contain statistics data.
			 * @param maxIterationNum
			 *      The max iteration number.
			 * @param currentIteration
			 *      Current iteration number/generation.
			 * @param collectionItems
			 *      A {@link CollectionItem} {@link Collection}.
			 * @param entityAttributes
			 *      Entities contains attributes info/coding in {@link Encoded}.
			 * @param finalAttributes
			 *      Final attributes for entity attributes.
			 * @param fitnessValue
			 *      The fitness values of entities.
			 * @return A {@link List} of {@link BasicIterationInfo4Optimization}.
			 */
			public static <CollectionItem, Encoded extends AttributeEncoding<?>>
				List<BasicIterationInfo4Optimization<Number>> pushInfoOfIteration(
					Map<String, Object> statistics, int maxIterationNum, int currentIteration,
					Collection<CollectionItem> collectionItems,
					Encoded[] entityAttributes, Iterator<int[]> finalAttributes,
					Number[] fitnessValue
			) {
				@SuppressWarnings("unchecked")
				OptEntityBasicInfo<Number>[] entityInfos =
						new OptEntityBasicInfo[entityAttributes.length];
				for (int i = 0; i < entityAttributes.length; i++) {
					entityInfos[i] = new OptEntityBasicInfo<>();
					entityInfos[i].setEntityAttributeLength(entityAttributes[i].getAttributes().length);
					entityInfos[i].setFinalAttributes(finalAttributes.next());
					entityInfos[i].setCurrentFitnessValue(fitnessValue[i]);
				}

				@SuppressWarnings("unchecked")
				List<BasicIterationInfo4Optimization<Number>> collection =
						(List<BasicIterationInfo4Optimization<Number>>)
						statistics.get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS);
				if (collection == null) {
					statistics.put(
							StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS,
							collection = new ArrayList<>(maxIterationNum)
					);
				}
				collection.add(
						new BasicIterationInfo4Optimization<Number>(
								currentIteration,
								collectionItems.size(),
								collectionItems.iterator().next().getClass()
						).setOptimizationEntityBasicInfo(entityInfos)
				);
				return collection;
			}

			/**
			 * Push Iteration Info.s into <code>statistics</code>(
			 * {@link StatisticsConstants.Procedure#STATISTIC_ITERATION_INFOS}).
			 * <p>
			 * Different from {@link #pushInfoOfIteration(Map, int, int, Collection, AttributeEncoding[],
			 * Iterator, Number[])}, this methods is for {@link Encoded} that encodes attribute indexes
			 * (starts from 0). So, in this method, <code>finalAttributesIndex</code> would be
			 * transformed into the corresponding attribute values based on the indexes(in
			 * <code>entityAttributes</code>) and the source attributes(<code>sourceAttributes</code>).
			 *
			 * @see #pushInfoOfIteration(Map, int, int, Collection, AttributeEncoding[], Iterator,
			 * Number[])
			 *
			 * @param <CollectionItem>
			 *      {@link Instance} or Equivalence Classes.
			 * @param <Encoded>
			 *      Type of {@link AttributeEncoding}.
			 * @param statistics
			 *      A {@link Map} to contain statistics data.
			 * @param maxIterationNum
			 *      The max iteration number.
			 * @param currentIteration
			 *      Current iteration number/generation.
			 * @param collectionItems
			 *      A {@link CollectionItem} {@link Collection}.
			 * @param sourceAttributes
			 *      The source {@link Instance} attributes.(Starts from 1)
			 * @param entityAttributes
			 *      Entities contains attributes info/coding in {@link Encoded}. Here, codings only
			 *      contains the index of the corresponding <code>attributes</code> in
			 *      <code>sourceAttributes</code>.
			 * @param finalAttributesIndex
			 *      Final attribute indexes for entity attributes. To obtain the genuine final
			 *      attribute values, this method obtains based on <code>sourceAttributes</code>.
			 * @param fitnessValue
			 *      The fitness values of entities.
			 * @return A {@link List} of {@link BasicIterationInfo4Optimization}.
			 */
			public static <CollectionItem, Encoded extends AttributeEncoding<?>>
				List<BasicIterationInfo4Optimization<Number>> pushInfoOfIteration(
					Map<String, Object> statistics, int maxIterationNum, int currentIteration,
					Collection<CollectionItem> collectionItems,
					int[] sourceAttributes, Encoded[] entityAttributes, Iterator<int[]> finalAttributesIndex,
					Number[] fitnessValue
			) {
				@SuppressWarnings("unchecked")
				OptEntityBasicInfo<Number>[] entityInfos =
						new OptEntityBasicInfo[entityAttributes.length];
				for (int i = 0; i < entityAttributes.length; i++) {
					int[] finalAttributeIndexes = finalAttributesIndex.next();

					entityInfos[i] = new OptEntityBasicInfo<Number>();
					entityInfos[i].setEntityAttributeLength(entityAttributes[i].getAttributes().length);
					entityInfos[i].setFinalAttributes(
							Arrays.stream(finalAttributeIndexes)
									// transform indexes into actual attribute values
									.map(ind -> sourceAttributes[ind])
									.toArray()
					);
					entityInfos[i].setCurrentFitnessValue(fitnessValue[i]);
				}

				@SuppressWarnings("unchecked")
				List<BasicIterationInfo4Optimization<Number>> collection =
						(List<BasicIterationInfo4Optimization<Number>>)
						statistics.get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS);
				if (collection == null) {
					statistics.put(
							StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS,
							collection = new ArrayList<>(maxIterationNum)
					);
				}
				collection.add(
						new BasicIterationInfo4Optimization<Number>(
								currentIteration,
								collectionItems.size(),
								collectionItems.iterator().next().getClass()
						).setOptimizationEntityBasicInfo(entityInfos)
				);
				return collection;
			}

			/**
			 * Collect entity info.s(i.e. {@link OptEntityBasicInfo}) in
			 * {@link BasicIterationInfo4Optimization} is <code>statistics</code> saved as
			 * {@link StatisticsConstants.Procedure#STATISTIC_ITERATION_INFOS}.
			 * <p>
			 * <strong>Notice</strong>: Even if <code>useInspectedAsKeys</code> is true, if
			 * {@link OptEntityBasicInfo#getInspectedReduct()} is <strong>null</strong>,
			 * the {@link Map} will use sorted {@link OptEntityBasicInfo#getInspectedReduct()}
			 * as keys.
			 *
			 * @param iterInfos
			 *      {@link BasicIterationInfo4Optimization} {@link List} that contains iteration
			 *      info.s.
			 * @param globalBestLimit
			 *      The number of global best value limitation. Served as the initial size of the
			 *      returned map.
			 * @param useInspectedAsKeys
			 *      Whether to use inspected attributes as keys for the returned {@link Map}. If true,
			 *      use sorted {@link OptEntityBasicInfo#getInspectedReduct()} as keys; else, use
			 *      sorted {@link OptEntityBasicInfo#getFinalAttributes()} as keys;
			 * @return entity info.s in {@link Map} whose keys are the sorted attributes entities
			 *      represent.
			 */
			public static Map<IntArrayKey, Collection<OptEntityBasicInfo<?>>>
				collectGlobalBestEntityMap(
					List<BasicIterationInfo4Optimization<?>> iterInfos, int globalBestLimit,
					boolean useInspectedAsKeys
			) {
				Map<IntArrayKey, Collection<OptEntityBasicInfo<?>>> entityMap =
						new HashMap<>(globalBestLimit);
				for (int i = iterInfos.size() - 1; i >= 0; i--) {
					boolean breakLoop = false;
					for (OptEntityBasicInfo<?> entityInfo :
							iterInfos.get(i).getOptimizationEntityBasicInfo()
					) {
						if (entityInfo.getSupremeMark() != null &&
								SupremeMarkType.isGlobalBest(entityInfo.getSupremeMark().getSupremeMarkType())
						) {
							// Loop until the last one marked as global best entity.
							if (entityInfo.getSupremeMark().getRank() == 1L) breakLoop = true;

							IntArrayKey key;
							if (!useInspectedAsKeys || entityInfo.getInspectedReduct() == null) {
								Arrays.sort(entityInfo.getFinalAttributes());
								key = new IntArrayKey(entityInfo.getFinalAttributes());
							} else {
								key = new IntArrayKey(
										entityInfo.getInspectedReduct().stream()
												.sorted().mapToInt(v -> v).toArray()
								);
							}

							Collection<OptEntityBasicInfo<?>> entities = entityMap.get(key);
							if (entities == null){
								entityMap.put(key, entities = new LinkedList<>());
							}
							entities.add(entityInfo);
						}
					}
					if (breakLoop){
						break;
					}
				}
				return entityMap;
			}

			/**
			 * Loop over {@link OptimizationReduct} and load the corresponding
			 * {@link OptEntityBasicInfo}s in {@link Map} with
			 * {@link OptEntityBasicInfo#isSolution()} and
			 * {@link OptEntityBasicInfo#getInspectedReduct()} set.
			 *
			 * @see #loadInspectedReducts(Map, Collection)
			 *
			 * @param iterEntities
			 *      {@link OptEntityBasicInfo}s in  {@link Map}. (which can be obtained by
			 *      {@link #collectGlobalBestEntityMap(List, int, boolean)}.
			 * @param reducts
			 *      A @{@link Collection} of {@link OptimizationReduct} {@link Collection}.
			 * @throws IllegalStateException if entity in <code>reducts</code> is not found in
			 *      <code>iterEntities</code>.
			 */
			public static void markSolutions(
					Map<IntArrayKey, Collection<OptEntityBasicInfo<?>>> iterEntities,
					Collection<Collection<OptimizationReduct>> reducts
			) throws IllegalStateException {
				for (Collection<OptimizationReduct> optReducts : reducts) {
					for (OptimizationReduct optReduct : optReducts) {
						IntArrayKey key = new IntArrayKey(
								optReduct.getRedsCodingBeforeInspection().getAttributes()
						);

						Collection<OptEntityBasicInfo<?>> iterEntitiesFound = iterEntities.get(key);
						if (iterEntitiesFound == null) {
							key = new IntArrayKey(
									optReduct.getRedsAfterInspection().stream().sorted().mapToInt(v -> v).toArray()
							);
							iterEntitiesFound = iterEntities.get(key);
						}

						if (iterEntitiesFound == null) {
							iterEntities.keySet().forEach(System.out::println);

							throw new IllegalStateException(
									String.format("The corresponding entity is not found in the " +
													"collected Iteration Info.s in Statistics: %s",
											ArrayUtils.intArrayToString(key.getKey(), 20)
									)
							);
						} else {
							for (OptEntityBasicInfo<?> entity : iterEntitiesFound) {
								entity.setSolution(true);
								entity.setInspectedReduct(optReduct.getRedsAfterInspection());
							}
						}
					}
				}
			}

			/**
			 * Loop over {@link OptimizationReduct} and load the corresponding
			 * {@link OptEntityBasicInfo}s in {@link Map} with
			 * {@link OptEntityBasicInfo#getInspectedReduct()} set.
			 * <p>
			 * Different from {@link #markSolutions(Map, Collection)}, this method doesn't
			 * set solution({@link OptEntityBasicInfo#setSolution(boolean)}).
			 *
			 * @see #markSolutions(Map, Collection)
			 *
			 * @param iterEntities
			 *      {@link OptEntityBasicInfo}s in  {@link Map}. (which can be obtained by
			 *      {@link #collectGlobalBestEntityMap(List, int, boolean)}.
			 * @param reducts
			 *      A @{@link Collection} of {@link OptimizationReduct} {@link Collection}.
			 * @throws IllegalStateException if entity in <code>reducts</code> is not found in
			 *      <code>iterEntities</code>.
			 */
			public static void loadInspectedReducts(
					Map<IntArrayKey, Collection<OptEntityBasicInfo<?>>> iterEntities,
					Collection<Collection<OptimizationReduct>> reducts
			) throws IllegalStateException {
				for (Collection<OptimizationReduct> optReducts : reducts) {
					for (OptimizationReduct optReduct : optReducts) {
						IntArrayKey key = new IntArrayKey(
								optReduct.getRedsCodingBeforeInspection().getAttributes()
						);

						Collection<OptEntityBasicInfo<?>> iterEntitiesFound = iterEntities.get(key);
						if (iterEntitiesFound == null) {
							throw new IllegalStateException(
									String.format("The corresponding entity is not found in the " +
													"collected Iteration Info.s in Statistics: %s",
											ArrayUtils.intArrayToString(key.getKey(), 20)
									)
							);
						} else {
							for (OptEntityBasicInfo<?> entity : iterEntitiesFound) {
								entity.setInspectedReduct(optReduct.getRedsAfterInspection());
							}
						}
					}
				}
			}

			/**
			 * Loop over {@link OptimizationReduct} and load the corresponding
			 * {@link OptEntityBasicInfo}s in {@link Map} with
			 * {@link OptEntityBasicInfo#getInspectedReduct()} set.
			 * <p>
			 * Different from {@link #markSolutions(Map, Collection)}, this methods is for
			 * {@link OptimizationReduct} that encodes attribute indexes (starts from 0). So, in this
			 * method, indexes in <code>reducts</code> would be transformed into the corresponding
			 * attribute values based on the source attributes(<code>sourceAttributes</code>).
			 *
			 * @see #markSolutions(Map, Collection)
			 *
			 * @param iterEntities
			 *      {@link OptEntityBasicInfo}s in  {@link Map}. (which can be obtained by
			 *      {@link #collectGlobalBestEntityMap(List, int, boolean)}.
			 * @param reducts
			 *      A @{@link Collection} of {@link OptimizationReduct} {@link Collection}.
			 * @param sourceAttributes
			 *      The source {@link Instance} attributes.(Starts from 1)
			 * @throws IllegalStateException if entity in <code>reducts</code> is not found in
			 *      <code>iterEntities</code>.
			 */
			public static void loadInspectedReducts(
					Map<IntArrayKey, Collection<OptEntityBasicInfo<?>>> iterEntities,
					Collection<Collection<OptimizationReduct>> reducts,
					int[] sourceAttributes
			) throws IllegalStateException {
				for (Collection<OptimizationReduct> optReducts : reducts) {
					for (OptimizationReduct optReduct : optReducts) {
						IntArrayKey key = new IntArrayKey(
								Arrays.stream(optReduct.getRedsCodingBeforeInspection().getAttributes())
										.map(i -> sourceAttributes[i]).toArray()
						);

						Collection<OptEntityBasicInfo<?>> iterEntitiesFound = iterEntities.get(key);
						if (iterEntitiesFound == null) {
							throw new IllegalStateException(
									String.format("The corresponding entity is not found in the " +
													"collected Iteration Info.s in Statistics: %s",
											ArrayUtils.intArrayToString(key.getKey(), 20)
									)
							);
						} else {
							for (OptEntityBasicInfo<?> entity : iterEntitiesFound) {
								entity.setInspectedReduct(optReduct.getRedsAfterInspection());
							}
						}
					}
				}
			}
		}

		/**
		 * Combine statistics ({@link Map}s) in {@link Procedure} into one {@link Map}.
		 * 
		 * @see #combineProcedureStatics(Procedure, Map)
		 *
		 * @param procedure
		 *      {@link Procedure} instance to be processed.
		 * @return A {@link Map} as combined statistics.
		 */
		public static Map<String, Object> combineProcedureStatics(Procedure procedure) {
			return combineProcedureStatics(procedure, new HashMap<>());
		}

		/**
		 * Combine statistics ({@link Map}s) in {@link Procedure} into one {@link Map}.
		 * <p>
		 * <ul>
		 *     <li>For {@link Number}: {@link Integer}, {@link Long}, {@link Double}
		 *         <p>
		 *         The combined value is calculated by the sum value of values with identical key
		 *         in {@link Map}.
		 *     </li>
		 *     <li>For {@link Collection}
		 *         <p>
		 *         The combined value is a new {@link Collection}(the same as the 1st value to be
		 *         combined) that contains all values by calling {@link Collection#add(Object)}.
		 *     </li>
		 *     <li>For {@link Map}
		 *         <p>
		 *         The combined value is the 1st value(instance) of all with the rest of the key-
		 *         value entries being added by calling {@link Map#putAll(Map)}.
		 *     </li>
		 *     <li>For int[] or Integer[]
		 *         <p>
		 *         The combined value is a simple new array with all values.
		 *     </li>
		 *     <li>Otherwise
		 *         <p>
		 *         Not implemented yet.
		 *     </li>
		 * </ul>
		 *
		 * @see #combineProcedureStatics(Procedure, Map)
		 *
		 * @param procedure
		 *      {@link Procedure} instance to be processed.
		 * @param map
		 *      A {@link Map} to contain the combined statistics data.
		 * @return A {@link Map} as combined statistics.
		 */
		@SuppressWarnings({"rawtypes", "unchecked"})
		private static Map<String, Object> combineProcedureStatics(Procedure procedure, Map<String, Object> map) {
			if (procedure instanceof StatisticsCalculated) {
				Object value;
				Map<String, Object> currentStatistics =
						((StatisticsCalculated) procedure)
							.getStatistics()
							.getData();
				for (Map.Entry<String, Object> entry : currentStatistics.entrySet()) {
					value = map.get(entry.getKey());
					if (value != null) {
						if (value instanceof Number) {
							if (value instanceof Integer){
								value = ((Integer) value) + ((Integer) entry.getValue());
							} else if (value instanceof Long){
								value = ((Long) value) + ((Long) entry.getValue());
							} else if (value instanceof Double){
								value = ((Double) value) + ((Double) entry.getValue());
							} else{
								throw new RuntimeException("Unimplemented type : " + value.getClass().getName());
							}
						} else if (value instanceof Collection) {
							try {
								Collection collection = (Collection) value.getClass().newInstance();
								for (Object obj : (Collection) value){
									collection.add(obj);
								}
								for (Object obj : (Collection) entry.getValue()){
									collection.add(obj);
								}
								value = collection;
							} catch (InstantiationException | IllegalAccessException e) {
								e.printStackTrace();
							}
						} else if (value instanceof Map) {
							((Map) value).putAll((Map) entry.getValue());
						} else if (value instanceof int[]) {
							int[] array = Arrays.copyOf((int[]) value, ((int[]) value).length + (((int[]) entry.getValue()).length));
							for (int i = ((int[]) value).length; i < array.length; i++) {
								array[i] = ((int[]) entry.getValue())[i - ((int[]) value).length];
							}
							value = array;
						} else if (value instanceof Integer[]) {
							Integer[] array = Arrays.copyOf((Integer[]) value, ((Integer[]) value).length + (((Integer[]) entry.getValue()).length));
							for (int i = ((Integer[]) value).length; i < array.length; i++) {
								array[i] = ((Integer[]) entry.getValue())[i - ((Integer[]) value).length];
							}
							value = array;
						} else {
							throw new RuntimeException("Unimplemented type : " + value.getClass().getName());
						}
						map.put(entry.getKey(), value);
					} else {
						map.put(entry.getKey(), entry.getValue());
					}
				}
			}
			if (procedure instanceof ProcedureContainer) {
				for (ProcedureComponent<?> component :
						((ProcedureContainer<?>) procedure).getComponents()
				) {
					combineProcedureStatics(component, map);
				}
			} else {
				if (((ProcedureComponent<?>) procedure).getSubProcedureContainers() != null &&
						!((ProcedureComponent<?>) procedure).getSubProcedureContainers().isEmpty()
				) {
					for (ProcedureContainer<?> subContainer :
							((ProcedureComponent<?>) procedure).getSubProcedureContainers().values()
					) {
						combineProcedureStatics(subContainer, map);
					}
				}
			}
			return map;
		}

		/**
		 * Display {@link StatisticsCalculated} using the given {@link Logger}.
		 *
		 * @param log
		 *      {@link Logger} instance.
		 * @param statistic
		 *      {@link StatisticsCalculated} to be displayed.
		 */
		public static void displayOne(Logger log, StatisticsCalculated statistic) {
			if (statistic != null && statistic.getStatistics() != null &&
					!statistic.getStatistics().getData().isEmpty()
			) {
				LoggerUtil.printLine(log, "=", 70);
				log.info("Statistics of 【{}】", statistic.staticsName());
				LoggerUtil.printLine(log, "-", 70);
				for (Map.Entry<String, Object> entry : statistic.getStatistics().getData().entrySet()) {
					if (entry.getValue() instanceof Collection)
						log.info("	" + "{}({}) : {}", entry.getKey(), entry.getValue() == null ? 0 : ((Collection<?>) entry.getValue()).size(), entry.getValue());
					else if (entry.getValue() instanceof Integer[])
						log.info("	" + "{}({}) : {}", entry.getKey(), entry.getValue() == null ? 0 : ((Integer[]) entry.getValue()).length, entry.getValue());
					else
						log.info("	" + "{} : {}", entry.getKey(), entry.getValue());
				}
				LoggerUtil.printLine(log, "=", 70);
			}
		}

		public static void displayAll(Logger log, Procedure procedure) {
			if (procedure instanceof StatisticsCalculated) displayOne(log, (StatisticsCalculated) procedure);
			if (procedure instanceof ProcedureContainer) {
				if (((ProcedureContainer<?>) procedure).getComponents() != null &&
						!((ProcedureContainer<?>) procedure).getComponents().isEmpty()
				) {
					for (ProcedureComponent<?> comp : ((ProcedureContainer<?>) procedure).getComponents()) {
						displayAll(log, comp);
					}
				}
			} else {
				if (((ProcedureComponent<?>) procedure).getSubProcedureContainers() != null &&
						!((ProcedureComponent<?>) procedure).getSubProcedureContainers().isEmpty()
				) {
					for (ProcedureContainer<?> con : ((ProcedureComponent<?>) procedure).getSubProcedureContainers().values())
						displayAll(log, con);
				}
			}
		}

		/**
		 * Dig and get the class of {@link ProcedureContainer} and the classes of
		 * {@link ProcedureComponent}s and {@ProcedureContainer}s it contains.
		 * 
		 * @see #classesOfProcedureComponent(ProcedureComponent)
		 *
		 * @param container
		 *      A {@link ProcedureContainer}.
		 * @return A {@link Collection} of {@link Class}es.
		 */
		public static Collection<Class<?>> classesOfProcedureContainer(ProcedureContainer<?> container) {
			Collection<Class<?>> classes = new HashSet<>();
			classes.add(container.getClass());
			if (container.getComponents() != null)
				for (ProcedureComponent<?> component : container.getComponents()) {
					classes.addAll(classesOfProcedureComponent(component));
				}
			return classes;
		}

		/**
		 * Dig and get the class of {@link ProcedureComponent} and the classes of
		 * {@link ProcedureComponent}s and {@ProcedureContainer}s it contains.
		 *
		 * @see #classesOfProcedureContainer(ProcedureContainer)
		 *
		 * @param component
		 *      A {@link ProcedureComponent}.
		 * @return A {@link Collection} of {@link Class}es.
		 */
		public static Collection<Class<?>> classesOfProcedureComponent(ProcedureComponent<?> component) {
			Collection<Class<?>> classes = new HashSet<>();
			classes.add(component.getClass());
			if (component.getSubProcedureContainers() != null) {
				for (ProcedureContainer<?> container : component.getSubProcedureContainers().values()) {
					classes.addAll(classesOfProcedureContainer(container));
				}
			}
			return classes;
		}

		/**
		 * Transform {@link ProcedureContainer}'s info. into JSON string.
		 *
		 * @see JSONObject#toJSONString(Object)
		 * @see #procedureContainerJSONInfo(ProcedureContainer)
		 *
		 * @param container
		 *      {@link ProcedureContainer} instance.
		 * @return JSON string.
		 */
		public static String toJSONInfo(ProcedureContainer<?> container) {
			return JSONObject.toJSONString(procedureContainerJSONInfo(container));
		}

		/**
		 * Transform {@link ProcedureContainer}'s info. into {@link Map}.
		 * 
		 * @see #procedureComponentJSONInfo(ProcedureComponent)
		 *
		 * @param container
		 *      {@link ProcedureContainer} instance.
		 * @return {@link Map} that contains keys: name, description, class and the correspondent
		 *      values.
		 */
		public static Map<String, Object> procedureContainerJSONInfo(ProcedureContainer<?> container) {
			Map<String, Object> map = new HashMap<>();
			map.put("name", container.shortName());
			map.put("description", container.description());
			map.put("class", container.getClass().getSimpleName());
			List<ProcedureComponent<?>> components = container.getComponents();
			if (components != null) {
				Map<String, Map<String, Object>> innerComponents = new HashMap<>(components.size());
				for (ProcedureComponent<?> comp : components)
					innerComponents.put(comp.getDescription(), procedureComponentJSONInfo(comp));
				map.put("components", innerComponents);
			}
			return map;
		}

		/**
		 * Transform {@link ProcedureComponent}'s info. into {@link Map}.
		 *
		 * @param component
		 *      {@link ProcedureComponent} instance.
		 * @return {@link Map} that contains keys: tag, description, class, subContainers and the
		 *      correspondent values.
		 */
		public static Map<String, Object> procedureComponentJSONInfo(ProcedureComponent<?> component) {
			Map<String, Object> map = new HashMap<>();

			map.put("tag", component.getTag());
			map.put("description", component.getDescription());
			map.put("class", component.getClass().getSimpleName());
			Map<String, Object> containerMap = new HashMap<>();
			for (Map.Entry<String, ProcedureContainer<?>> entry : component.getSubProcedureContainers().entrySet())
				containerMap.put(entry.getKey(), procedureContainerJSONInfo(entry.getValue()));
			map.put("subContainers", containerMap);
			return map;
		}
	}

	public static class Report {

		public static class ExecutionTime {
			@SuppressWarnings("unchecked")
			public static long sum(ReportGenerated<?> report) {
				long sum = 0L;
				if (report.getReport() instanceof Map) {
					sum += sum((Map<String, ?>) report.getReport());
				}
				return sum;
			}

			@SuppressWarnings("unchecked")
			private static long sum(Map<String, ?> map) {
				long sum = 0;
				for (Map.Entry<String, ?> entry : map.entrySet()) {
					if (ReportConstants.Procedure.REPORT_EXECUTION_TIME.equals(entry.getKey())) {
						if (entry.getValue() instanceof Long) {
							sum += (Long) entry.getValue();
						} else if (entry.getValue() instanceof Collection) {
							sum += sum((Collection<?>) entry.getValue());
						}
					} else if (entry.getValue() instanceof Map) {
						sum += sum((Map<String, ?>) entry.getValue());
					}
				}
				return sum;
			}

			@SuppressWarnings("unchecked")
			private static long sum(Collection<?> collection) {
				long sum = 0;
				for (Object obj : collection) {
					if (obj instanceof Collection) {
						sum += sum((Collection<?>) obj);
					} else if (obj instanceof Map) {
						sum += sum((Map<String, ?>) obj);
					} else if (obj instanceof Long) {
						if (obj != null) sum += (Long) obj;
					}
				}
				return sum;
			}

			/**
			 * Save executed time of {@link Component} by remark for current round.
			 *
			 * <p> period = <code>component.getTime()</code> -
			 * <code>localParameters</code>[<code>"executedTime"</code>]
			 * report: {
			 * 	component.getDescription(): {
			 * 		{@link ReportConstants.Procedure#REPORT_EXECUTION_TIME}:
			 * 		[<code>component.getTime()</code>]
			 * 	}
			 * }
			 * </pre>
			 *
			 * @param localParameters A {@link Map} as Component local parameters/storage.
			 * @param report          {@link Map} with String as key and Map as value.
			 * @param reportMark      Mark for current round.
			 * @param component       {@link TimeCountedProcedureComponent} to be saved.
			 * @see {@link ExecutionTime#save(Map, String, long...)}.
			 */
			public static void save(
					Map<String, Object> localParameters,
					Map<String, Map<String, Object>> report, String reportMark,
					TimeCountedProcedureComponent<?> component
			) {
				@SuppressWarnings("unchecked")
				Map<String, Long> executedTime = (Map<String, Long>) localParameters.get("executedTime");
				if (executedTime == null) localParameters.put("executedTime", executedTime = new HashMap<>());
				Long historyTime = executedTime.get(component.getDescription());
				if (historyTime == null) historyTime = 0L;
				if (component.getTime() - historyTime >= 0) {
					ExecutionTime.save(report, reportMark, component.getTime() - historyTime);
				} else {
					throw new RuntimeException("Component time is less than history time: " +
							component.getTime() + " < " + historyTime
					);
				}
				executedTime.put(component.getDescription(), component.getTime());
			}

			/**
			 * Save the time of {@link TimeCountedProcedureComponent} by
			 * {@link ReportConstants.Procedure#REPORT_EXECUTION_TIME}, named by
			 * <code>component.getDescription()</code>.
			 * <p>
			 * <pre>
			 * report: {
			 * 	component.getDescription(): {
			 * 		{@link ReportConstants.Procedure#REPORT_EXECUTION_TIME}:
			 * 		[<code>component.getTime()</code>]
			 * 	}
			 * }
			 * </pre>
			 *
			 * @param report    {@link Map} with String as key and Map as value.
			 * @param component A {@link TimeCountedProcedureComponent} instance.
			 */
			public static void save(
					Map<String, Map<String, Object>> report,
					TimeCountedProcedureComponent<?> component
			) {
				if (component instanceof TimeCountedProcedureComponent) {
					Map<String, Object> reportContent = report.get(component.getDescription());
					if (reportContent == null) report.put(component.getDescription(), reportContent = new HashMap<>());
					@SuppressWarnings("unchecked")
					List<Long> executionTimes = (List<Long>) reportContent.get(ReportConstants.Procedure.REPORT_EXECUTION_TIME);
					if (executionTimes == null)
						reportContent.put(ReportConstants.Procedure.REPORT_EXECUTION_TIME, executionTimes = new LinkedList<>());
					executionTimes.add(component.getTime());
				}
			}

			/**
			 * Save the time of {@link TimeCountedProcedureComponent} by
			 * {@link ReportConstants.Procedure#REPORT_EXECUTION_TIME}, named by
			 * <code>componentDesc</code>.
			 * <p>
			 * <pre>
			 * report: {
			 * 	component.getDescription(): {
			 * 		{@link ReportConstants.Procedure#REPORT_EXECUTION_TIME}:
			 * 		[<code>component.getTime()</code>]
			 * 	}
			 * }
			 * </pre>
			 *
			 * @param report        {@link Map} with String as key and Map as value.
			 * @param componentDesc {@link TimeCountedProcedureComponent}'s description.
			 * @param time          Execution times to be saved in order.
			 */
			public static void save(
					Map<String, Map<String, Object>> report, String componentDesc,
					long... time
			) {
				Map<String, Object> reportContent = report.get(componentDesc);
				if (reportContent == null) report.put(componentDesc, reportContent = new HashMap<>());
				@SuppressWarnings("unchecked")
				List<Long> executionTimes = (List<Long>) reportContent.get(ReportConstants.Procedure.REPORT_EXECUTION_TIME);
				if (executionTimes == null)
					reportContent.put(ReportConstants.Procedure.REPORT_EXECUTION_TIME, executionTimes = new LinkedList<>());
				for (long t : time) executionTimes.add(t);
			}

			/**
			 * Save execution times of current report record(current round).
			 * <p>
			 * <pre>
			 * report: [
			 * 	0: {
			 * 		{@link ReportConstants.Procedure#REPORT_EXECUTION_TIME}:
			 * 		[<code>time[0]</code>, ...]
			 * 	},
			 * 	1: {...}
			 * ]
			 * </pre>
			 *
			 * @param report {@link List} with {@link Map} as report item for the current round.
			 * @param time   Execution times to be saved.
			 */
			public static void save(List<Map<String, Object>> report, long... time) {
				List<Long> executionTimes;
				Map<String, Object> reportItem = new HashMap<>();
				reportItem.put(ReportConstants.Procedure.REPORT_EXECUTION_TIME, executionTimes = new LinkedList<>());
				for (long t : time) executionTimes.add(t);
				report.add(reportItem);
			}
		}

		public static class DatasetRealTimeInfo {
			/**
			 * Save dataset real-time info.
			 *
			 * @param report               A {@link Map} for report.
			 * @param componentDesc        Unique description of {@link ProcedureComponent} for identification.
			 * @param universeSize         Current total Universe size.
			 * @param compactedUniveseSize The size of compacted universes/structures.
			 * @param reductSize           Current reduct size. (One or more reduct)
			 */
			public static void save(
					Map<String, Map<String, Object>> report, String componentDesc,
					int universeSize, int compactedUniveseSize, int... reductSize
			) {
				// REPORT_CURRENT_UNIVERSE_SIZE
				saveItem(report, componentDesc, ReportConstants.Procedure.REPORT_CURRENT_UNIVERSE_SIZE, universeSize);
				// REPORT_CURRENT_COMPACTED_UNIVERSE_SIZE
				saveItem(report, componentDesc, ReportConstants.Procedure.REPORT_CURRENT_COMPACTED_UNIVERSE_SIZE, compactedUniveseSize);
				// REPORT_CURRENT_REDUCT_SIZE
				saveItem(report, componentDesc, ReportConstants.Procedure.REPORT_CURRENT_REDUCT_SIZE, reductSize);
			}
		}

		public static void countInt(
				Map<String, Map<String, Object>> report, String componentDesc,
				String key, int increment
		) {
			Map<String, Object> data = report.get(componentDesc);
			if (data == null) report.put(componentDesc, data = new HashMap<>());

			Integer count = (Integer) data.get(key);
			if (count == null) count = 0;
			data.put(key, count + increment);
		}

		public static <V> void saveItem(
				Map<String, Map<String, Object>> report, String componentDesc,
				String key, V value
		) {
			Map<String, Object> reportContent = report.get(componentDesc);
			if (reportContent == null) report.put(componentDesc, reportContent = new HashMap<>());
			reportContent.put(key, value);
		}

		public static void displayOne(Logger log, ReportGenerated<?> report) {
			if (report != null && report.getReport() != null) {
				LoggerUtil.printLine(log, "=", 70);
				log.info("Report of 【{}】", report.reportName());
				LoggerUtil.printLine(log, "-", 70);
				if (report.getReport() instanceof Map) {
					Map<?, ?> map = (Map<?, ?>) report.getReport();
					for (String key : ((ReportMapGenerated<?, ?>) report).getReportMapKeyOrder()) {
						Object value = map.get(key);
						if (value instanceof Collection)
							log.info("	" + "{}({}) : {}", key, value == null ? 0 : ((Collection<?>) value).size(), value);
						else if (value instanceof Integer[])
							log.info("	" + "{}({}) : {}", key, value == null ? 0 : ((Integer[]) value).length, value);
						else
							log.info("	" + "{} : {}", key, value);
					}
					LoggerUtil.printLine(log, "-", 70);
				} else if (report.getReport() instanceof Collection) {
					Collection<?> collection = (Collection<?>) report.getReport();
					int index = 0;
					for (Object value : collection) {
						if (value instanceof Collection)
							log.info("	" + "{}({}) : {}", index, value == null ? 0 : ((Collection<?>) value).size(), value);
						else if (value instanceof Integer[])
							log.info("	" + "{}({}) : {}", index, value == null ? 0 : ((Integer[]) value).length, value);
						else
							log.info("	" + "{} : {}", index, value);
					}
					LoggerUtil.printLine(log, "-", 70);
				}
				log.info("	" + "sum({}) : {}", ReportConstants.Procedure.REPORT_EXECUTION_TIME, ExecutionTime.sum(report));
				LoggerUtil.printLine(log, "=", 70);
			}
		}

		public static void displayAll(Logger log, Procedure procedure) {
			if (procedure instanceof ReportGenerated) displayOne(log, (ReportGenerated<?>) procedure);
			if (procedure instanceof ProcedureContainer) {
				if (((ProcedureContainer<?>) procedure).getComponents() != null &&
						!((ProcedureContainer<?>) procedure).getComponents().isEmpty()
				) {
					for (ProcedureComponent<?> comp : ((ProcedureContainer<?>) procedure).getComponents()) {
						displayAll(log, comp);
					}
				}
			} else {
				if (((ProcedureComponent<?>) procedure).getSubProcedureContainers() != null &&
						!((ProcedureComponent<?>) procedure).getSubProcedureContainers().isEmpty()
				) {
					for (ProcedureContainer<?> con : ((ProcedureComponent<?>) procedure).getSubProcedureContainers().values())
						displayAll(log, con);
				}
			}
		}
	}
}