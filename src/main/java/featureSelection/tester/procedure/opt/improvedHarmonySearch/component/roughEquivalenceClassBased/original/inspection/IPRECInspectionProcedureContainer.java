package featureSelection.tester.procedure.opt.improvedHarmonySearch.component.roughEquivalenceClassBased.original.inspection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import common.utils.ArrayCollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4CombInReverse;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator.CapacityCal4SqrtAttrSize;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.improvedHarmonySearch.GenerationRecord;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.entity.ByteHarmony4RealAttributeReflected;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4RSCREC;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Improved Harmony Algorithm</strong> Final reduct inspection execution. Mainly, this contains 
 * only 1 ProcedureComponent: 
 * <ul>
 * 	<li>
 * 		<strong>Inspection</strong>
 * 		<p>Use the IP-REC inspection method to execute. 
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_GENERATION_RECORD}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_ALGORITHM}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}</li>
 * </ul>
 * 
 * @see FeatureImportance
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition.Inspection
 * 
 * @author Benjamin_L
 *
 * @param <Hrmny>
 * 		Type of Implemented {@link Harmony}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
public class IPRECInspectionProcedureContainer<Hrmny extends Harmony<?>,
												FValue extends FitnessValue<?>>
	extends DefaultProcedureContainer<Map<IntArrayKey, Collection<OptimizationReduct>>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	public IPRECInspectionProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	}

	@Override
	public String shortName() {
		return "IHS Inspection(IP-REC inspection method)";
	}
	
	@Override
	public String staticsName() {
		return shortName();
	}

	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Inspection.
			new TimeCountedProcedureComponent<Map<IntArrayKey, Collection<OptimizationReduct>>>(
					ComponentTags.TAG_INSPECT_LAST,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						GenerationRecord<FValue> geneRecord =
								(GenerationRecord<FValue>) parameters[p++];
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						ReductionParameters<?, Hrmny, FValue> params =
								(ReductionParameters<?, Hrmny, FValue>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						int[] redArray;
						IntArrayKey key;
						Collection<Integer> redAfterInspection;
						Collection<OptimizationReduct> optimizationReducts;
							
						AttrProcessStrategy4CombInReverse inspectAttributeProcessStrategy =
							new AttrProcessStrategy4CombInReverse(
								new AttrProcessStrategyParams()
									.set(AttrProcessStrategy4CombInReverse.PARAMETER_EXAM_CAPACITY_CALCULATOR, 
										new CapacityCal4SqrtAttrSize()
									)
							);
						
						Map<IntArrayKey, Collection<OptimizationReduct>> result = new HashMap<>(
								FastMath.max(16, geneRecord.getDistinctBestFitnessCount())
						);
						
						for (Harmony<?> harmony : geneRecord.getBestHarmonies()) {
							// Obtain actual attributes.
							redArray = Arrays.stream(harmony.getAttributes())
											.map(index->params.getAttributes()[index])
											.toArray();
								
							redAfterInspection = 
								RoughEquivalenceClassBasedExtensionAlgorithm
									.IncrementalPartition
									.Inspection
									.compute(
										inspectAttributeProcessStrategy.initiate(
											new IntegerArrayIterator(redArray)
										), 
										equClasses
									);
								
							TimerUtils.timePause((TimeCounted) component);
							
							if (redAfterInspection.size()!=redArray.length) {
								redArray = ArrayCollectionUtils.getIntArrayByCollection(redAfterInspection);
							}
							Arrays.sort(redArray);
							key = new IntArrayKey(redArray);
								
							optimizationReducts = result.get(key);
							if (optimizationReducts==null)	result.put(key, optimizationReducts=new LinkedList<>());
							optimizationReducts.add(
								new OptimizationReduct(
									new ByteHarmony4RealAttributeReflected(
										(Harmony<byte[]>) harmony, params.getAttributes()
									), 
									redAfterInspection
								)
							);
							
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return result;
					}, 
					(component, reducts) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, reducts);
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "|reduct| = {}"), reducts.size());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP,
							reducts
						);
						//	[STATISTIC_ITERATION_INFOS]
						GenerationRecord<FValue> geneRecord = 
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						if (geneRecord.getBestFitness()!=null) {
							ProcedureUtils.Statistics.IterationInfos.markSolutions(
								ProcedureUtils
									.Statistics
									.IterationInfos
									.collectGlobalBestEntityMap(
										getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS),
										geneRecord.getBestHarmonies().size(), false
									), 
								reducts.values()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Inspection"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<IntArrayKey, Collection<OptimizationReduct>> exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
		}
		return (Map<IntArrayKey, Collection<OptimizationReduct>>) componentArray[0].exec();
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

	public void checkInspectionRedundant(
			int universeSize, Collection<EquivalenceClass> equClasses,
			int[] redB4Inspect, Collection<Integer> reduct
	) {
		int originSize = reduct.size();
		Collection<Integer> copy = new ArrayList<>(reduct);
		RoughEquivalenceClassBasedExtensionAlgorithm
			.SimpleCounting
			.RealTimeCounting
			.inspection(universeSize, new PositiveRegionCalculation4RSCREC(), 0, equClasses, reduct);
		if (reduct.size()!=originSize) {
			log.error("Attribute redundant before inspection: {}", Arrays.toString(redB4Inspect));
			log.error("Attribute redundant after inspection: {}", copy);
			log.error("Attribute inspection expected: {}", reduct);
			throw new RuntimeException("Attribute redundant after inspection!");
		}
	}
}