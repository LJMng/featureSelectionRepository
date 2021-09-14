package featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.psoInitiate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge.NestedEquivalenceClassesMerger;
import featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge.NestedEquivalenceClassesMergerParameters;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Integer;
import featureSelection.repository.entity.opt.particleSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedStreamingDataCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.common.psoInitiate.ParticleSwarmOptimizationInitiateProcedureContainer;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.original.psoInitiate.ParticleSwarmOptimizationInitiateProcedureContainer4REC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Initialization of <strong>Particle Swarm Optimization</strong>.  this contains 3 
 * {@link ProcedureComponent}s : 
 * <ul>
 *  <li>
 *  	<strong>Get the Equivalent Class</strong>
 *  	<p>Compute and acquire the {@link EquivalenceClass}es.
 *  </li>
 *  <li>
 *  	<strong>PSO basic initiation</strong>
 *  	<p>Create the instance of {@link FeatureImportance}, a {@link GenerationRecord} and calculate
 *  		max fitness if needed
 *  </li>
 * </ul>
 * <p> Besides initializations in {@link ParticleSwarmOptimizationInitiateProcedureContainer},
 * here also generate {@link EquivalenceClass} Collection as
 * {@link ParameterConstants#PARAMETER_OPTIMIZATION_COLLECTION_ITEMS} for collection items in 
 * <code>Particle Swarm Optimization</code> execution.
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_COLLECTION_ITEM}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_REDUCT}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_REDUCT_SIG}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_IP_NEC_DYNAMIC_DATA_EQUIVALENCE_CLASS_MERGER}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_COLLECTION_ITEMS}</li>
 * </ul>
 * <p>
 * Among them, {@link NestedEquivalenceClassBasedStreamingDataCalculation} instance should be set into
 * {@link ProcedureParameters} by user with value associated key 
 * {@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}.
 * 
 * @see NestedEquivalenceClassBasedStreamingDataCalculation
 * @see NestedEquivalenceClassesMergerParameters
 * @see ParticleSwarmOptimizationInitiateProcedureContainer
 * @see ParticleSwarmOptimizationInitiateProcedureContainer4REC
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
 */
@Slf4j
public class ParticleSwarmOptimizationInitiateProcedureContainer4NECStreaming<Cal extends NestedEquivalenceClassBasedStreamingDataCalculation<Sig, MergeParams>,
																				Sig extends Number, 
																				MergeParams extends NestedEquivalenceClassesMergerParameters>
	extends DefaultProcedureContainer<Object[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;

	public ParticleSwarmOptimizationInitiateProcedureContainer4NECStreaming(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new ArrayList<>(3);
	}

	@Override
	public String shortName() {
		return "initialization";
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
			// 1. Get arrived equivalent classes.
			new TimeCountedProcedureComponent<Map<IntArrayKey, EquivalenceClass>>(
					ComponentTags.TAG_COMPACT,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return NestedEquivalenceClassBasedAlgorithm
								.Basic
								.equivalenceClass(
									instances,
									new IntegerArrayIterator(attributes)
								);
					}, 
					(component, equClasses) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS, equClasses);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_COMPRESSED, true);
						
						Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> previouseNestedEquivalenceClasses =
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM);
						int previouseUniverseSize = 
							previouseNestedEquivalenceClasses.values().stream()
								.mapToInt(NestedEquivalenceClass::getInstanceSize)
								.sum();
						Collection<Instance> arrivedUniverse =
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						int arrivedUniverseSize = arrivedUniverse.size();
						getParameters().setNonRoot("universeSizeSum", previouseUniverseSize+arrivedUniverseSize);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "Previouse universe: {}"), previouseUniverseSize);
							log.info(LoggerUtil.spaceFormat(2, "Arrived universe: {}"), arrivedUniverseSize);
							log.info(LoggerUtil.spaceFormat(2, "Total universe: {}"), (previouseUniverseSize+arrivedUniverseSize));
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						int[] attributes = getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES);
						int attributeLength = arrivedUniverse.iterator().next().getAttributeValues().length-1;
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder
								.newBuilder()
								.setPreviousInstanceNumber(previouseUniverseSize)
								.setCurrentInstanceNumber(arrivedUniverseSize)
								.setCompressedInstanceNumber(equClasses.size())
								.setPreviousConditionalAttributeNumber(attributeLength-attributes.length)
								.setCurrentConditionalAttributeNumber(attributes.length)
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
				}.setDescription("Get arrived equivalent classes"),
			// 2. Update and check if reduct re-searching is necessary: true-continue; false-exit.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT_SIG),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get(ParameterConstants.PARAMETER_IP_NEC_DYNAMIC_DATA_EQUIVALENCE_CLASS_MERGER),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Cal calculation =
								(Cal) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> previousNestedEquClasses =
								(Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>)
								parameters[p++];
						int[] reduct =
								(int[]) parameters[p++];
						Sig previousReductSig =
								(Sig) parameters[p++];
						Map<IntArrayKey, EquivalenceClass> arrivedEquivalenceClasses =
								(Map<IntArrayKey, EquivalenceClass>)
								parameters[p++];
						NestedEquivalenceClassesMerger<MergeParams, NestedEquivalenceClass<EquivalenceClass>> merger =
								(NestedEquivalenceClassesMerger<MergeParams, NestedEquivalenceClass<EquivalenceClass>>)
								parameters[p++];
						ReductionParameters<?, ?, FitnessValue<Integer>> params = 
								(ReductionParameters<?, ?, FitnessValue<Integer>>) 
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */					
						calculation.update4Arrived(
								new NestedEquivalenceClassBasedStreamingDataCalculation
										.Update4ArrivedInputs<>(
											attributes,
											previousNestedEquClasses, 
											new IntegerArrayIterator(reduct),
											previousReductSig,
											arrivedEquivalenceClasses,
											merger
										)
						);
						
						if (!calculation.getNecInfoWithMap().isEmptyBoundaryClass()) {
							if (params.getMaxFitness()==null) {
								int posCount = 0;
								for (NestedEquivalenceClass<EquivalenceClass> nestedEquClass: calculation.getNecInfoWithMap().getNestedEquClasses().values()) {
									for (EquivalenceClass equClass: nestedEquClass.getEquClasses().values()) {
										if (equClass.sortable()){
											posCount+=equClass.getInstanceSize();
										}
									}
								}
								params.setMaxFitness(new FitnessValue4Integer(posCount));
							}
						}
						return new Object[] {
								!calculation.getNecInfoWithMap().isEmptyBoundaryClass(),
								calculation.getResult(),
								calculation.getNecInfoWithMap().getNestedEquClasses().values()
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						boolean boundariesLeft = (boolean) result[r++];
						Sig newSig = (Sig) result[r++];
						Collection<NestedEquivalenceClass<EquivalenceClass>> boundaries =
								(Collection<NestedEquivalenceClass<EquivalenceClass>>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_PROCEDURE_EXIT_MARK, !boundariesLeft);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_NEW_GLOBAL_SIG, newSig);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS, boundaries);
						/* ------------------------------------------------------------------------------ */
						if (!boundariesLeft) {
							if (logOn) {
								log.info(LoggerUtil.spaceFormat(1, "No boundaries NEC left, ready to exit!"));
							}
						}else {
							if (logOn) {
								int universeSize = 0;
								for(NestedEquivalenceClass<EquivalenceClass> nec: boundaries)
									universeSize+=nec.getInstanceSize();
								log.info(LoggerUtil.spaceFormat(2, "x{} 0-NEC (x{} U) left, compile."), 
										boundaries.size(), universeSize);
							}
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						BasicExecutionInstanceInfo.Builder builder = 
							statistics.get(StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER);
						builder.setExecutedRecordNumberNumber(
								boundaries.stream()
									.mapToInt(NestedEquivalenceClass::getItemSize)
									.sum(), 
								EquivalenceClass.class
							);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update and check if reduct re-searching is necessary"),
			// 3. PSO basic initialization.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "3. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Cal calculation = (Cal) parameters[p++];
						ReductionParameters<?, ?, FitnessValue<Integer>> params = 
								(ReductionParameters<?, ?, FitnessValue<Integer>>) 
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						if (ProcedureUtils.procedureExitMark(getParameters())) {
							return null;
						}else {
							ReductionAlgorithm<EquivalenceClass, ?, ?, FitnessValue<Integer>, Cal, Sig> redAlg = params.getReductionAlgorithm();
							return new Object[] {
									calculation,
									redAlg,
									new GenerationRecord<>(),
							};
						}
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						if (ProcedureUtils.procedureExitMark(getParameters()))	return;
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Cal calculation =
								(Cal) result[r++];
						ReductionAlgorithm<EquivalenceClass, ?, ?, FitnessValue<Integer>, Cal, Sig> redAlg =
								(ReductionAlgorithm<EquivalenceClass, ?, ?, FitnessValue<Integer>, Cal, Sig>)
								result[r++];
						GenerationRecord<?, ?, FitnessValue<Integer>> geneRecord =
								(GenerationRecord<?, ?, FitnessValue<Integer>>) result[r++];
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							ReductionParameters<?, ?, FitnessValue<Integer>> params =
									getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
							log.info(LoggerUtil.spaceFormat(2, "Max fitness = {}"), 
									params.getMaxFitness().getValue() instanceof Integer?
									String.format("%2d", params.getMaxFitness().getValue().intValue()):
									String.format("%.4f", params.getMaxFitness().getValue().doubleValue())
							);
						}
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM, redAlg);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD, geneRecord);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("PSO basic initialization"),
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
	private void checkUniverseTotalSize(Collection<NestedEquivalenceClass<EquivalenceClass>> boundaries) throws Exception {
		int totalUniverseSize = getParameters().get("universeSizeSum");
		int count=0;
		Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> previousNestedEquClasses =
				getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM);
		for (NestedEquivalenceClass<EquivalenceClass> nec: boundaries) {
			count+=nec.getInstanceSize();
			int ecount = 0;
			for (EquivalenceClass e: nec.getEquClasses().values())	ecount+= e.getInstanceSize();
			if (ecount!=nec.getInstanceSize()) {
				throw new Exception("Universe size abnormal, expect "+ecount+", get "+nec.getInstanceSize());
			}
		}
		for (NestedEquivalenceClass<EquivalenceClass> nec: previousNestedEquClasses.values()) {
			count+=nec.getInstanceSize();
			int ecount = 0;
			for (EquivalenceClass e: nec.getEquClasses().values())	ecount+= e.getInstanceSize();
			if (ecount!=nec.getInstanceSize()) {
				throw new Exception("Universe size abnormal, expect "+ecount+", get "+nec.getInstanceSize());
			}
		}
		if (totalUniverseSize!=count) {
			throw new Exception("Universe size abnormal, expect "+totalUniverseSize+", get "+count);
		}
	}
}