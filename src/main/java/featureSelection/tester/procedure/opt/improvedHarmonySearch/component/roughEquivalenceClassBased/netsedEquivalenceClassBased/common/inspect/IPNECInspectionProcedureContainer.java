package featureSelection.tester.procedure.opt.improvedHarmonySearch.component.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.inspect;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import common.utils.ArrayCollectionUtils;
import common.utils.CollectionUtils;
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
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4CombInReverse;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator.CapacityCal4SqrtAttrSize;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.improvedHarmonySearch.GenerationRecord;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.entity.ByteHarmony4RealAttributeReflected;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.inspection.IPNECSkipInspectionProcedureContainer4Streaming;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Improved Harmony Search Algorithm</strong> Final reduct inspection execution. Mainly, this 
 * contains only 1 {@link ProcedureComponent}:
 * <ul>
 * 	<li>
 * 		<strong>Inspection</strong>
 * 		<p>Use the local inspection method in {@link ProcedureParameters} to execute. 
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_GENERATION_RECORD}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_COLLECTION_ITEMS}</li>
 * </ul>
 * 
 * @see FeatureImportance
 * 
 * @author Benjamin_L
 *
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <Hrmny>
 * 		Type of Implemented {@link Harmony}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
public class IPNECInspectionProcedureContainer<Sig extends Number,
												Hrmny extends Harmony<?>,
												FValue extends FitnessValue<Sig>>
	extends DefaultProcedureContainer<Map<IntArrayKey, Collection<OptimizationReduct>>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	public IPNECInspectionProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	}

	@Override
	public String shortName() {
		return "IHS Inspection(Local inspection method)";
	}
	
	@Override
	public String staticsName() {
		return shortName();
	}

	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings("unchecked")
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
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get("bestFitness")
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						GenerationRecord<FValue> geneRecord = (GenerationRecord<FValue>) parameters[p++];
						ReductionParameters<Sig, Hrmny, FValue> params = (ReductionParameters<Sig, Hrmny, FValue>) parameters[p++];
						Collection<EquivalenceClass> equClasses = (Collection<EquivalenceClass>) parameters[p++];
						Fitness<Sig, FValue> bestFitness = (Fitness<Sig, FValue>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Map<IntArrayKey, Collection<OptimizationReduct>> result =
								new HashMap<>(geneRecord.getDistinctBestFitnessCount());
						
						if (geneRecord.getBestFitness()!=null && 
							params.getRedAlg().compareToMaxFitness(bestFitness, params)>=0
						) {
							AttrProcessStrategy4CombInReverse inspectAttributeProcessStrategy =
									new AttrProcessStrategy4CombInReverse(
										new AttrProcessStrategyParams()
											.set(AttrProcessStrategy4CombInReverse.PARAMETER_EXAM_CAPACITY_CALCULATOR, 
												new CapacityCal4SqrtAttrSize()
											)
									);
							
							IntArrayKey key;
							int[] redArray;
							Collection<Integer> redAfterInspectation;
							Collection<OptimizationReduct> optimizationReducts;
							for (Harmony<?> harmony: geneRecord.getBestHarmonies()) {
								// Acquire actual attributes.
								redArray = Arrays.stream(harmony.getAttributes())
												.map(index->(int) params.getAttributes()[index])
												.toArray();
								
								redAfterInspectation = 
										redArray.length==1?
										Arrays.asList(redArray[0]):
										NestedEquivalenceClassBasedAlgorithm
											.IncrementalPartition
											.Inspection
											.computeEquivalenceClasses(
												inspectAttributeProcessStrategy.initiate(new IntegerArrayIterator(redArray)),
												equClasses
											);
								
								TimerUtils.timePause((TimeCounted) component);
								
								if (redAfterInspectation.size()!=redArray.length) {
									redArray = ArrayCollectionUtils.getIntArrayByCollection(redAfterInspectation);
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
										redAfterInspectation
									)
								);
							
								TimerUtils.timeContinue((TimeCounted) component);
							}
							return result;
						}else {
							TimerUtils.timePause((TimeCounted) component);
							result = (Map<IntArrayKey, Collection<OptimizationReduct>>)
									CollectionUtils.firstOf(component.getSubProcedureContainers().values())
													.exec();
							TimerUtils.timeContinue((TimeCounted) component);
							return result;
						}
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
						GenerationRecord<FValue> geneRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						ReductionParameters<Sig, Hrmny, FValue> params = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						Fitness<Sig, FValue> bestFitness = getParameters().get("bestFitness");
						
						if (geneRecord.getBestFitness()!=null && 
							params.getRedAlg().compareToMaxFitness(bestFitness, params)>=0
						) {
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
			}.setDescription("Inspection")
			.setSubProcedureContainer(
				"IPNECSkipInspectionProcedureContainer4Streaming", 
				new IPNECSkipInspectionProcedureContainer4Streaming<>(getParameters(), logOn)
			),
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
}
