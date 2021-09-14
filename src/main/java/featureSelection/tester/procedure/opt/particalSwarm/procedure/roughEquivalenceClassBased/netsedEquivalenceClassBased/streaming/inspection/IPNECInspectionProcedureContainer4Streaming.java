package featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.inspection;

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
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedUtils;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4CombInReverse;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator.CapacityCal4SqrtAttrSize;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.position.BytePosition4RealAttributeReflected;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4RSCREC;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Particle Swarm Optimization</strong> Final reduct inspection execution. Mainly, this
 * contains 1 {@link ProcedureComponent}:
 * <ul>
 * 	<li>
 * 		<strong>Inspection</strong>
 * 		<p>Use the IP-REC inspection method to execute. 
 * 	</li>
 * </ul>
 * 
 * @see FeatureImportance
 * @see NestedEquivalenceClassBasedAlgorithm.IncrementalPartition.Inspection
 * 
 * @author Benjamin_L
 *
 * @param <Velocity>
 * 		Type of {@link Particle}'s Velocity.
 * @param <Posi>
 * 		Type of implemented {@link Position}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
public class IPNECInspectionProcedureContainer4Streaming<Velocity,
														Posi extends Position<?>,
														FValue extends FitnessValue<?>>
	extends DefaultProcedureContainer<Object[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	public IPNECInspectionProcedureContainer4Streaming(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	}

	@Override
	public String shortName() {
		return "PSO Inspection(IP-NEC inspection method)";
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
			// 1. Stream Inspection.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_INSPECT_LAST,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Velocity, Posi, FValue> params =
								(ReductionParameters<Velocity, Posi, FValue>) parameters[p++];
						Collection<NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses =
								(Collection<NestedEquivalenceClass<EquivalenceClass>>)
								parameters[p++];
						GenerationRecord<Velocity, Posi, FValue> geneRecord =
								(GenerationRecord<Velocity, Posi, FValue>) 
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						boolean executedInspection = true;
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						int[] redArray;
						IntArrayKey key;
						Collection<Integer> redAfterInspection;
						Collection<OptimizationReduct> optimizationReducts;
						
						Map<IntArrayKey, Collection<OptimizationReduct>> redMap = new HashMap<>(
								FastMath.max(16, geneRecord.countDistinctBestFitness())
						);
						
						// If fitness equals to max fitness => considered as reduct is found.
						if (params.getReductionAlgorithm().compareMaxFitness(
								params.getMaxFitness(), geneRecord.getGlobalBestFitness().getFitnessValue()
							)<=0
						) {
							AttrProcessStrategy4CombInReverse inspectAttributeProcessStrategy =
									new AttrProcessStrategy4CombInReverse(
										new AttrProcessStrategyParams()
											.set(AttrProcessStrategy4CombInReverse.PARAMETER_EXAM_CAPACITY_CALCULATOR, 
												new CapacityCal4SqrtAttrSize()
											)
									);
							for (Fitness<Posi, FValue> f: geneRecord.getGlobalBestFitnessCollection()) {
								// Load actual attributes.
								TimerUtils.timePause((TimeCounted) component);
								
								redArray = new int[f.getPosition().getAttributes().length];
								for (int a=0; a<redArray.length; a++)
									redArray[a] = params.getAttributes()[f.getPosition().getAttributes()[a]];
								
								TimerUtils.timeContinue((TimeCounted) component);

								redAfterInspection = 
										NestedEquivalenceClassBasedAlgorithm
											.IncrementalPartition
											.Inspection
											.computeNestedEquivalenceClasses(
												inspectAttributeProcessStrategy.initiate(
													new IntegerArrayIterator(redArray)
												), 
												nestedEquClasses
											);//*/

								TimerUtils.timePause((TimeCounted) component);
								
								redArray = ArrayCollectionUtils.getIntArrayByCollection(redAfterInspection);
								Arrays.sort(redArray);
								key = new IntArrayKey(redArray);
								
								optimizationReducts = redMap.get(key);
								if (optimizationReducts==null)	redMap.put(key, optimizationReducts=new LinkedList<>());
								optimizationReducts.add(
									new OptimizationReduct(
										new BytePosition4RealAttributeReflected(
											(Position<byte[]>) f.getPosition(), params.getAttributes()
										), 
										redAfterInspection
									)
								);
									
								TimerUtils.timeContinue((TimeCounted) component);
							}
							return new Object[] {
									redMap, executedInspection
							};
						// else skip inspection for reduct has not been found
						}else {
							TimerUtils.timePause((TimeCounted) component);
							
							ProcedureContainer<Object[]> comp =
									(ProcedureContainer<Object[]>) 
									component.getSubProcedureContainers().values().iterator().next();
							Object[] compResult = comp.exec();
							
							TimerUtils.timeContinue((TimeCounted) component);
							return compResult;
						}
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Map<IntArrayKey, Collection<OptimizationReduct>> reducts = (Map<IntArrayKey, Collection<OptimizationReduct>>) result[r++];
						boolean executedInspection = (boolean) result[r++];
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
						if (executedInspection) {
							GenerationRecord<Velocity, Posi, FValue> geneRecord =
									getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
							if (geneRecord.getGlobalBestFitness()!=null) {
								ReductionParameters<Velocity, Posi, FValue> params = 
										getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
								
								ProcedureUtils.Statistics.IterationInfos.markSolutions(
									ProcedureUtils
										.Statistics
										.IterationInfos
										.collectGlobalBestEntityMap(
											getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS),
											geneRecord.getGlobalBestFitnessCollection().size(),
											params.isInspectReductInGreedySearch()
										), 
									reducts.values()
								);
							}
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Stream Inspection")
			.setSubProcedureContainer(
				"IPNECSkipInspectionProcedureContainer4Streaming", 
				new IPNECSkipInspectionProcedureContainer4Streaming<>(getParameters(), false)
			),
		};
	}

	@Override
	public Object[] exec() throws Exception {
		Object result = null;
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
			
			result = each.exec();
		}
		return (Object[]) result;
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
	
	@SuppressWarnings("unused")
	private void checkInspectionRedundant(
			int universeSize, Collection<EquivalenceClass> equClasses, int[] redB4Inspect, Collection<Integer> reduct
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
	private void redWithBoundaries(Collection<EquivalenceClass> equClasses, Collection<Integer> red) throws Exception {
		if (RoughEquivalenceClassBasedUtils
				.Validation
				.redWithBoundaries4EquivalenceClasses(equClasses, new IntegerCollectionIterator(red))
		) {
			throw new Exception("0-NEC exists with reduct: "+red);
		}
	}
}
