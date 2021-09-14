package featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.inspect;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import common.utils.ArrayCollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
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
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedAlgorithm;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedUtils;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4CombInReverse;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator.CapacityCal4SqrtAttrSize;
import featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge.NestedEquivalenceClassesMergerParameters;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.ReductionParameters;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedStreamingDataCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.inspection.IPNECSkipInspectionProcedureContainer4Streaming;
import featureSelection.tester.procedure.opt.genetic.procedure.common.inspection.LocalInspectionProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.inspection.IPNECInspectionProcedureContainer4Streaming;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.original.inspection.IPRECInspectionProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Genetic Algorithm</strong> reduct inspection execution for <code>IP-NEC</code>.
 * Mainly, this {@link ProcedureContainer} contains only 1 {@link ProcedureComponent}:
 * <ul>
 *  <li>
 *  	<strong>Inspection</strong>
 *  	<p>Use IP-REC/IP-NEC inspection to execute.
 *  </li>
 * </ul>
 * <p>
 * In this {@link DefaultProcedureContainer}(only!), the following parameters are used in 
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_GENERATION_RECORD}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_COLLECTION_ITEMS}</li>
 * </ul>
 * 
 * @see FeatureImportance
 * @see NestedEquivalenceClassBasedAlgorithm.IncrementalPartition.Inspection
 * @see LocalInspectionProcedureContainer
 * @see IPRECInspectionProcedureContainer
 * @see IPNECInspectionProcedureContainer4Streaming
 * @see IPNECSkipInspectionProcedureContainer4Streaming
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link NestedEquivalenceClassBasedStreamingDataCalculation}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <MergeParams>
 * 		Type of implemented {@link NestedEquivalenceClassesMergerParameters} as merging parameters for
 * 		{@link NestedEquivalenceClassBasedStreamingDataCalculation}.
 * @param <Chr>
 * 		Type of Implemented {@link Chromosome}.
 * @param <FValue>
 * 		Type of {@link Comparable} extended fitness value.
 */
@Slf4j
public class IPNECInspectionProcedureContainer<Cal extends NestedEquivalenceClassBasedStreamingDataCalculation<Sig, MergeParams>,
												Sig extends Number, 
												MergeParams extends NestedEquivalenceClassesMergerParameters,
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
	
	public IPNECInspectionProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	}

	@Override
	public String shortName() {
		return "GA Inspection(IP-NEC inspection method)";
	}
	
	@Override
	public String staticsName() {
		return shortName();
	}

	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings({ "unchecked" })
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
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Cal, Sig, ?, Chr, FValue> params =
								(ReductionParameters<Cal, Sig, ?, Chr, FValue>) parameters[p++];
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						GenerationRecord<Chr, FValue> geneRecord =
								(GenerationRecord<Chr, FValue>) parameters[p++];
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Map<IntArrayKey, Collection<OptimizationReduct>> result = new HashMap<>(
								FastMath.max(16, geneRecord.getFitness().size())
						);
						
						int[] redArray;
						IntArrayKey key;
						Collection<Integer> redAfterInspection;
						Collection<OptimizationReduct> optimizationReducts;
						
						// If fitness equals to max fitness => considered as reduct is found.
						if (params.getReductionAlgorithm().compareMaxFitness(params.getMaxFitness(), geneRecord)<=0) {
							// execute inspection

							AttrProcessStrategy4CombInReverse inspectAttributeProcessStrategy =
									new AttrProcessStrategy4CombInReverse(
										new AttrProcessStrategyParams()
											.set(AttrProcessStrategy4CombInReverse.PARAMETER_EXAM_CAPACITY_CALCULATOR, 
												new CapacityCal4SqrtAttrSize()
											)
									);


							for (Fitness<Chr, FValue> fitness: geneRecord.getFitness()) {
								if (fitness.getFitnessValue().getValue().doubleValue() <
										geneRecord.getBestFitness().getValue().doubleValue()
								) {
									continue;
								} else {
									redArray = fitness.getChromosome().getAttributes();
								}

								redAfterInspection = 
										redArray.length==1?
										Arrays.asList(redArray[0]):
										NestedEquivalenceClassBasedAlgorithm
											.IncrementalPartition
											.Inspection
											.computeEquivalenceClasses(
												inspectAttributeProcessStrategy.initiate(
													new IntegerArrayIterator(redArray)
												), 
												equClasses
											);
								
								TimerUtils.timePause((TimeCounted) component);

								// validate if the result is correct.
//								if (redAfterInspection.size()>1) {
//									checkInspectionRedundant(
//											instances.size(), equClasses, redArray,
//											redAfterInspection
//									);
//								}

								// Collect reduct.

								redArray = ArrayCollectionUtils.getIntArrayByCollection(redAfterInspection);
								Arrays.sort(redArray);
								key = new IntArrayKey(redArray);
								
								optimizationReducts = result.get(key);
								if (optimizationReducts==null){
									result.put(key, optimizationReducts=new LinkedList<>());
								}
								optimizationReducts.add(new OptimizationReduct(fitness.getChromosome(), redAfterInspection));

								TimerUtils.timeContinue((TimeCounted) component);
							}
							return new Object[] {
									result,
									geneRecord.getFitness().size()+1
							};
						// else skip inspection for reduct has not been found
						}else {
							TimerUtils.timePause((TimeCounted) component);

							for (Fitness<Chr, FValue> fitness: geneRecord.getFitness()) {	
								if (fitness.getFitnessValue().getValue().doubleValue() <
										geneRecord.getBestFitness().getValue().doubleValue()
								) {
									continue;
								}else {
									redArray = fitness.getChromosome().getAttributes();
								}

								redAfterInspection = new HashSet<>(redArray.length);
								for (int attr: redArray){
									redAfterInspection.add(attr);
								}
								
								Arrays.sort(redArray);
								key = new IntArrayKey(redArray);
								
								optimizationReducts = result.get(key);
								if (optimizationReducts==null)	result.put(key, optimizationReducts=new LinkedList<>());
								optimizationReducts.add(new OptimizationReduct(fitness.getChromosome(), redAfterInspection));
							}
							
							TimerUtils.timeContinue((TimeCounted) component);
							return new Object[] {
									result,
							};
						}
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
						//	[STATISTIC_ITERATION_INFOS]
						ProcedureUtils.Statistics.IterationInfos.loadInspectedReducts(
								ProcedureUtils
									.Statistics
									.IterationInfos
									.collectGlobalBestEntityMap(
										getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS),
										geneRecord.getFitness().size(), false
									), 
								reducts.values()
							);
						//	[STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP,
							reducts
						);
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
			int instanceSize, Collection<EquivalenceClass> equClasses,
			int[] redB4Inspect, Collection<Integer> reduct
	) {
		try {
			RoughEquivalenceClassBasedUtils
				.Validation
				.checkIfReductHasRedundancyValidBySREC(instanceSize, equClasses, redB4Inspect, reduct);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@SuppressWarnings("unused")
	private void redWithBoundaries(Collection<EquivalenceClass> equClasses, int[] red) {
		if (RoughEquivalenceClassBasedUtils
				.Validation
				.redWithBoundaries4EquivalenceClasses(equClasses, new IntegerArrayIterator(red))
		) {
			throw new RuntimeException("0-NEC exists with reduct: "+Arrays.toString(red));
		}
	}
}