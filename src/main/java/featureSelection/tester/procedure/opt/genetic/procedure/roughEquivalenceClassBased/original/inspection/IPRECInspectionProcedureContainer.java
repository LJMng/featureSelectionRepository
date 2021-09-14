package featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.original.inspection;

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
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
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
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedAlgorithm;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedUtils;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4CombInReverse;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator.CapacityCal4SqrtAttrSize;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4RSCREC;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.genetic.procedure.common.inspection.LocalInspectionProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.inspect.IPNECInspectionProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.inspection.IPNECInspectionProcedureContainer4Streaming;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.inspection.IPNECSkipInspectionProcedureContainer4Streaming;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Genetic Algorithm</strong> reduct inspection execution. Mainly, this contains only 1
 * {@link ProcedureComponent}:
 * <ul>
 *  <li>
 *  	<strong>Inspection</strong>
 *  	<p>Use the IP-REC inspection method to execute.
 *  </li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_GENERATION_RECORD}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_ALGORITHM}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}</li>
 * </ul>
 * 
 * @see FeatureImportance}
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition.Inspection}
 * 
 * @see LocalInspectionProcedureContainer
 * @see IPNECInspectionProcedureContainer
 * @see IPNECInspectionProcedureContainer4Streaming
 * @see IPNECSkipInspectionProcedureContainer4Streaming
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <CollectionItem>
 * 		Universe or EquivalenceClass.
 * @param <Chr>
 * 		Type of Implemented {@link Chromosome}.
 * @param <FValue>
 * 		Type of {@link Comparable} extended fitness value.
 */
@Slf4j
public class IPRECInspectionProcedureContainer<Cal extends FeatureImportance<Sig>,
												Sig extends Number, 
												CollectionItem,
												Chr extends Chromosome<?>,
												FValue extends FitnessValue<?>>
	extends DefaultProcedureContainer<Object[]>
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
		return "GA Inspection(IP-REC inspection method)";
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
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_INSPECT_LAST,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p = 0;
						int[] attributes =
								(int[]) parameters[p++];
						GenerationRecord<Chr, FValue> geneRecord =
								(GenerationRecord<Chr, FValue>) parameters[p++];
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Map<IntArrayKey, Collection<OptimizationReduct>> result = new HashMap<>(geneRecord.getFitness().size());
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
							
						Collection<EquivalenceClass> equClasses =
								RoughEquivalenceClassBasedAlgorithm
									.Basic
									.equivalenceClass(
										instances,
										new IntegerArrayIterator(attributes)
									);
							
						for (Fitness<Chr, FValue> fitness : geneRecord.getFitness()) {
							redArray = fitness.getChromosome().getAttributes();
								
							redAfterInspection = 
									RoughEquivalenceClassBasedExtensionAlgorithm
										.IncrementalPartition
										.Inspection
										.compute(
											inspectAttributeProcessStrategy.initiate(new IntegerArrayIterator(redArray)), 
											equClasses
										);
								
							TimerUtils.timePause((TimeCounted) component);
							
							if (redAfterInspection.size()>1)
								checkInspectionRedundant(instances.size(), equClasses, redArray, redAfterInspection);
							
							if (redAfterInspection.size()!=redArray.length)
								redArray = ArrayCollectionUtils.getIntArrayByCollection(redAfterInspection);
							Arrays.sort(redArray);
							key = new IntArrayKey(redArray);
								
							optimizationReducts = result.get(key);
							if (optimizationReducts==null)	result.put(key, optimizationReducts=new LinkedList<>());
							optimizationReducts.add(new OptimizationReduct(fitness.getChromosome(), redAfterInspection));
							
							TimerUtils.timeContinue((TimeCounted) component);
						}

						return new Object[] {
								result,
								geneRecord.getFitness().size()
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Map<IntArrayKey, Collection<OptimizationReduct>> reducts = (Map<IntArrayKey, Collection<OptimizationReduct>>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, reducts);
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "|reduct| = {}"), reducts.size());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						GenerationRecord<Chr, FValue> geneRecord =
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						//	[STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP,
							reducts
						);
						//	[STATISTIC_ITERATION_INFOS]
						ProcedureUtils.Statistics.IterationInfos.markSolutions(
							ProcedureUtils
								.Statistics
								.IterationInfos
								.collectGlobalBestEntityMap(
									getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS),
									geneRecord.getFitness().size(), false
								), 
							reducts.values()
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
						//	[REPORT_OPTIMIZATION_SIG_CALCULATION_TIMES]
						ProcedureUtils.Report
								.saveItem(report, 
										component.getDescription(),
										ReportConstants.Procedure.REPORT_OPTIMIZATION_EXIT_REDUCT_SIZE,
										reducts.size()
						);
						//	[REPORT_OPTIMIZATION_CURRENT_MAX_FITNESS]
						ProcedureUtils.Report
								.saveItem(report, 
										component.getDescription(),
										ReportConstants.Procedure.REPORT_OPTIMIZATION_CURRENT_MAX_FITNESS,
										reducts.isEmpty()? 0: geneRecord.getBestFitness()
						);
						//	[REPORT_OPTIMIZATION_SIG_CALCULATION_TIMES]
						ProcedureUtils.Report
								.saveItem(report, 
										component.getDescription(),
										ReportConstants.Procedure.REPORT_OPTIMIZATION_EXIT_ITERATION,
										geneRecord.getGeneration()
						);
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

	@Override
	public Object[] exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
		}
		return (Object[]) componentArray[0].exec();
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

	private void checkInspectionRedundant(
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

	@SuppressWarnings("unused")
	private void redWithBoundaries(Collection<EquivalenceClass> equClasses, Collection<Integer> red) {
		if (RoughEquivalenceClassBasedUtils
				.Validation
				.redWithBoundaries4EquivalenceClasses(equClasses, new IntegerCollectionIterator(red))
		) {
			throw new RuntimeException("0-NEC exists with reduct: "+red);
		}
	}
}