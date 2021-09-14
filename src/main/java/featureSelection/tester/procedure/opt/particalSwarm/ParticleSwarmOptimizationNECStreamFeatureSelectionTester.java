package featureSelection.tester.procedure.opt.particalSwarm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.utils.CollectionUtils;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.container.SelectiveComponentsProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.support.reductMiningStrategy.optimization.ParticleSwarmOptimizationReductMiningStrategy;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedUtils;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge.NestedEquivalenceClassesMergerParameters;
import featureSelection.repository.entity.alg.rec.nestedEC.reductionResult.ReductionResult4Streaming;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedStreamingDataCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.genetic.procedure.common.generationLoop.ReductPrioritizedGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.common.particleInitiate.ParticleInitiateProcedureContainer;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.multiSolutionSelection.GranularityThresholdBasedMultiSolutionSelectionProcedureContainer4NEC;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.multiSolutionSelection.RandomThresholdBasedMultiSolutionSelectionProcedureContainer4NEC;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.multiSolutionSelection.ShortestThresholdBasedMultiSolutionSelectionProcedureContainer4NEC;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.multiSolutionSelection.SkipMultiSolutionSelectionProcedureContainer4NEC;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.singleSolutionSelection.KnowledgeGranularityBasedSingleSolutionSelectionProcedureContainer4NEC;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.inspection.IPNECInspectionProcedureContainer4Streaming;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.inspection.IPNECSkipInspectionProcedureContainer4Streaming;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.psoInitiate.ParticleSwarmOptimizationInitiateProcedureContainer4NECStreaming;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Particle Swarm Optimization</strong> <code>PLUS</code> 
 * <strong>Feature Selection</strong>.
 * <p>
 * Check out the original paper <a href="https://linkinghub.elsevier.com/retrieve/pii/S0169260713003477">
 * "Supervised hybrid feature selection based on PSO and rough sets for medical diagnosis"</a> 
 * by H.Hannah Inbarani, Ahmad Taher Azar, G. Jothi.
 * <p>
 * Particularly, this tester is for <strong>Incremental Partition Nested Equivalent Class based(IP-NEC)
 * </strong> feature selection using PSO(based on H.Hannah PSO).
 * <p>
 * This is a {@link SelectiveComponentsProcedureContainer}. Procedure contains 4 
 * {@link ProcedureComponent}s, referring to steps:
 * <ul>
 * 	<li>
 * 		<strong>Initiate</strong>: 
 * 		<p>Some initializations for Particle Swarm Optimization.
 * 		<p><code>ParticleSwarmOptimizationInitiateProcedureContainer4NECStreaming</code>,  
 * 	</li>
 * 	<li>
 * 		<strong>Initiate particles</strong>: 
 * 		<p>Initiate particles randomly.
 * 		<p><code>ParticleInitiateProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Generation loops</strong>: 
 * 		<p>Loop generations, calculate particles' fitness, "study". Until reaching maximum fitness and 
 * 			reaching maximum iteration/maximum convergence.
 * 		<p><code>ReductPrioritizedGenerationLoopProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Solution selection</strong>:
 * 		<p>Execute multi-solution selection, select limited multi-solution based on threshold or other 
 * 			strategies.
 * 		<p><code>SkipMultiSolutionSelectionProcedureContainer4NEC</code>
 * 		<p><code>GranularityThresholdBasedMultiSolutionSelectionProcedureContainer4NEC</code>
 * 		<p><code>RandomThresholdBasedMultiSolutionSelectionProcedureContainer4NEC</code>
 * 		<p><code>ShortestThresholdBasedMultiSolutionSelectionProcedureContainer4NEC</code>
 * 	</li>
 * 	<li>
 * 		<strong>Particles to reducts</strong>:
 * 		<p>Transfer particles to reducts.
 * 		<p><code>IPNECInspectionProcedureContainer4Streaming</code>
 * 		<p><code>IPNECSkipInspectionProcedureContainer4Streaming</code>
 * 	</li>
 * 	<li>
 * 		<strong>Return reducts</strong>:
 * 		<p>Collect reducts info and return {@link ReductionResult4Streaming} array.
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_REDUCT}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_COLLECTION_ITEMS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_COLLECTION_ITEM}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_GENERATION_RECORD}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_REDUCT_LIST_AFTER_INSPECTATION}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_NEW_GLOBAL_SIG}</li>
 * </ul>
 * 
 * @author Benjamin_L
 * 
 * @see ParticleSwarmOptimizationFeatureSelectionTester
 * 
 * @see ParticleSwarmOptimizationInitiateProcedureContainer4NECStreaming
 * @see ParticleInitiateProcedureContainer
 * @see ReductPrioritizedGenerationLoopProcedureContainer
 * @see SkipMultiSolutionSelectionProcedureContainer4NEC
 * @see GranularityThresholdBasedMultiSolutionSelectionProcedureContainer4NEC
 * @see RandomThresholdBasedMultiSolutionSelectionProcedureContainer4NEC
 * @see ShortestThresholdBasedMultiSolutionSelectionProcedureContainer4NEC
 * @see IPNECInspectionProcedureContainer4Streaming
 * @see IPNECSkipInspectionProcedureContainer4Streaming
 *
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <Velocity>
 * 		Type of {@link Particle}'s Velocity.
 * @param <Posi>
 * 		Type of implemented {@link Position}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
public class ParticleSwarmOptimizationNECStreamFeatureSelectionTester<Sig extends Number,
																		Velocity,
																		Posi extends Position<?>,
																		FValue extends FitnessValue<?>>
	extends SelectiveComponentsProcedureContainer<ReductionResult4Streaming<Integer>[]>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				StatisticsCalculated,
				ParticleSwarmOptimizationReductMiningStrategy
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	private String[] componentExecOrder;
	
	public ParticleSwarmOptimizationNECStreamFeatureSelectionTester(ProcedureParameters parameters, boolean logOn) {
		super(logOn, parameters);
		statistics = new Statistics();
		report = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Opt("+
					ProcedureUtils.ShortName.optimizationAlgorithm(getParameters())+"-"+
					ProcedureUtils.ShortName.calculation(getParameters())+
				")";
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
	public void initDefaultComponents(boolean logOn) {
		ProcedureComponent<?>[] componentArray = new ProcedureComponent<?>[] {
			// 1. Initiate.
			new ProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("1. "+component.getDescription());
					}, 
					(component, parameters) -> {
						return (Object[])
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					},
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
	
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate")
				.setSubProcedureContainer(
					"ParticleSwarmOptimizationInitiateProcedureContainer", 
					new ParticleSwarmOptimizationInitiateProcedureContainer4NECStreaming<>(getParameters(), logOn)
				),
			// 2. Initiate particles.
			new ProcedureComponent<Particle<Velocity, Posi, FValue>[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
					}, 
					(component, parameters) -> {
						if (ProcedureUtils.procedureExitMark(getParameters())) {
							return null;
						}else {
							return (Particle<Velocity, Posi, FValue>[])
									CollectionUtils.firstOf(component.getSubProcedureContainers().values())
													.exec();
						}
					}, 
					(component, chromosome)->{
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
	
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate particles")
				.setSubProcedureContainer(
					"ParticleInitiateProcedureContainer", 
					new ParticleInitiateProcedureContainer<>(getParameters(), logOn)
				),
			// 3. Generation loops.
			new ProcedureComponent<GenerationRecord<Velocity, Posi, FValue>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
					}, 
					(component, parameters) -> {
						if (ProcedureUtils.procedureExitMark(getParameters())) {
							return null;
						}else {
							return (GenerationRecord<Velocity, Posi, FValue>)
									CollectionUtils.firstOf(component.getSubProcedureContainers().values())
													.exec();
						}
					}, 
					(component, geneRecord)->{
						/* ------------------------------------------------------------------------------ */
						if (ProcedureUtils.procedureExitMark(getParameters()))	return;
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
		
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Generation loops")
				.setSubProcedureContainer(
					"GenerationLoopProcedureContainer", 
					new ReductPrioritizedGenerationLoopProcedureContainer<>(getParameters(), logOn)
				),
			// 4. Solution selection.
			new ProcedureComponent<GenerationRecord<Velocity, Posi, FValue>>(
					ComponentTags.TAG_SOLUTION_SELECTION,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("4. "+component.getDescription());
					},
					(component, parameters) -> {
						if (ProcedureUtils.procedureExitMark(getParameters())) {
							return null;
						}else {
							return (GenerationRecord<Velocity, Posi, FValue>) 
									component.getSubProcedureContainers()
									.get("MultiSolutionSelectionProcedureContainer")
									.exec();
						}
					}, 
					null
				){
					@Override public void init() {}
						@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Solution selection")
				.setSubProcedureContainer(
					"MultiSolutionSelectionProcedureContainer", 
					new SkipMultiSolutionSelectionProcedureContainer4NEC<>(getParameters(), logOn)
//					new GranularityThresholdBasedMultiSolutionSelectionProcedureContainer4NEC<>(getParameters(), logOn)
//					new RandomThresholdBasedMultiSolutionSelectionProcedureContainer4NEC<>(getParameters(), logOn)
//					new ShortestThresholdBasedMultiSolutionSelectionProcedureContainer4NEC<>(getParameters(), logOn)
				),
			// 5. Particles to reducts.
			new ProcedureComponent<Map<IntArrayKey, Collection<OptimizationReduct>>>(
					ComponentTags.TAG_INSPECT_LAST,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("5. "+component.getDescription());
					},
					(component, parameters) -> {
						if (ProcedureUtils.procedureExitMark(getParameters())) {
							return null;
						}else {
							Object[] objArray = 
								(Object[]) component.getSubProcedureContainers()
													.get("InspectionProcedureContainer")
													.exec();
							return (Map<IntArrayKey, Collection<OptimizationReduct>>) objArray[0];
						}
					}, 
					null
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Particles to reducts")
				.setSubProcedureContainer(
					"InspectionProcedureContainer", 
//					new IPNECInspectionProcedureContainer4Streaming<>(getParameters(), logOn)
					new IPNECSkipInspectionProcedureContainer4Streaming<>(getParameters(), logOn)
				),
			// 6. Return reducts.
			new ProcedureComponent<ReductionResult4Streaming<Sig>[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("6. "+component.getDescription());
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int[] reduct =
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT);
						Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> reductNestedEquClasses =
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM);
						ReductionParameters<?, ?, FitnessValue<Integer>> params =
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						/* ------------------------------------------------------------------------------ */
						// No new reduct is found. (0-NEC is empty)
						if (reduct!=null && ProcedureUtils.procedureExitMark(getParameters())) {
							// load fields from parameters.
							Sig newGlobalSig = getParameters().get(ParameterConstants.PARAMETER_NEW_GLOBAL_SIG);
							
							// return empty reduct result.
							Map<?, ?> map = new HashMap<>(0);
							getParameters().setNonRoot(
									ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION,
									map
							);
							statistics.put(
								StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP,
								map
							);
							return new ReductionResult4Streaming[] {
									new ReductionResult4Streaming<>(
										false, 
										RoughEquivalenceClassBasedUtils
											.collectEquivalenceClassesIn(reductNestedEquClasses.values()),
										reduct,
										newGlobalSig,
										null
									)
								};
						// 1st time processing the data
						}else if (reduct==null) {
							// load fields from parameters.
							Map<IntArrayKey, Collection<OptimizationReduct>> newReducts = 
									getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION);
							Collection<EquivalenceClass> equClasses =
									getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS);
							GenerationRecord<Velocity, Posi, FValue> geneRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);

							// return multiple solutions
							if (params.getSingleSolutionOnly()==null || !params.getSingleSolutionOnly()) {
								@SuppressWarnings("rawtypes")
								ReductionResult4Streaming[] result = new ReductionResult4Streaming[newReducts.size()];
								int i=0;
								for (IntArrayKey newReduct: newReducts.keySet()) {								
									// store ReductionResult4Streaming
									result[i++] = new ReductionResult4Streaming<>(
													false, 
													equClasses,
													newReduct.key(),
													geneRecord.getGlobalBestFitness().getFitnessValue().getValue(),
													null
												);
								}
								return result;
							// return single solution
							}else {
								ProcedureContainer<int[]> subComp = 
										(ProcedureContainer<int[]>)
										CollectionUtils.firstOf(component.getSubProcedureContainers().values());
								int[] chosenReduct = subComp.exec();
								
								return new ReductionResult4Streaming[] {
										new ReductionResult4Streaming<>(
											false, 
											equClasses,
											chosenReduct,
											geneRecord.getGlobalBestFitness().getFitnessValue().getValue(),
											null
										)
									};
							}
						// New reduct is found. (0-NEC is not empty)
						}else {
							// load fields from parameters.
							Map<IntArrayKey, Collection<OptimizationReduct>> newReducts = 
									getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION);
							NestedEquivalenceClassBasedStreamingDataCalculation<Sig,? extends NestedEquivalenceClassesMergerParameters> calculation =
									getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE);
							Sig newGlobalSig = getParameters().get(ParameterConstants.PARAMETER_NEW_GLOBAL_SIG);
							
							@SuppressWarnings("rawtypes")
							ReductionResult4Streaming[] result = new ReductionResult4Streaming[newReducts.size()];
							int i=0;
							Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> boundaries =
									calculation.getNecInfoWithMap().getNestedEquClasses();
								
							Collection<NestedEquivalenceClass<EquivalenceClass>> updatedNestedEquClasses =
								new ArrayList<>(reductNestedEquClasses.size()+boundaries.size());
							updatedNestedEquClasses.addAll(boundaries.values());
							updatedNestedEquClasses.addAll(reductNestedEquClasses.values());
							Collection<EquivalenceClass> newEquClasses =
									RoughEquivalenceClassBasedUtils
										.collectEquivalenceClassesIn(updatedNestedEquClasses);
							// return multiple solutions
							if (params.getSingleSolutionOnly()==null || !params.getSingleSolutionOnly()) {
								for (IntArrayKey key: newReducts.keySet()) {
									// reduct = reduct ∪ newReduct
									int[] reductArray = Arrays.copyOf(reduct, reduct.length+key.key().length);
									for (int r=reduct.length, j=0; r<reductArray.length; r++, j++)
										reductArray[r] = key.key()[j];
									Arrays.sort(reductArray);
									// store ReductionResult4Streaming
									result[i++] = new ReductionResult4Streaming<>(
													true, 
													newEquClasses,
													reductArray,
													newGlobalSig,
													null
												);
								}
								return result;
							// return single solution
							}else {
								ProcedureContainer<int[]> subComp = 
										(ProcedureContainer<int[]>) 
										component.getSubProcedureContainers().values().iterator().next();
								int[] chosenReduct = subComp.exec();
									
								// reduct = reduct ∪ newReduct
								int[] fullReduct = Arrays.copyOf(reduct, reduct.length+chosenReduct.length);
								for (int r=reduct.length, j=0; r<fullReduct.length; r++, j++)
									fullReduct[r] = chosenReduct[j];
								Arrays.sort(fullReduct);
									
								return new ReductionResult4Streaming[] {
										new ReductionResult4Streaming<>(
												true, 
												newEquClasses,
												fullReduct,
												newGlobalSig,
												null
											)
									};
							}
						}
					}, 
					null
				) {
					@Override public void init() {}
						
					@Override public String staticsName() {
						return shortName()+" | 6. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Return reducts")
				.setSubProcedureContainer(
					"SingleSolutionSelectionProcedureContainer4NEC", 
					new KnowledgeGranularityBasedSingleSolutionSelectionProcedureContainer4NEC(getParameters(), logOn)
				),
		};
		//	Component order.
		componentExecOrder = new String[componentArray.length];
		for (int i=0; i<componentArray.length; i++) {
			this.setComponent(componentArray[i].getDescription(), componentArray[i]);
			componentExecOrder[i] = componentArray[i].getDescription();
		}
	}
	
	@Override
	public String[] componentsExecOrder() {
		return componentExecOrder;
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		String[] keys = new String[this.getComponents().size()];
		int i=0;
		for (ProcedureComponent<?> comp: this.getComponents())	keys[i++] = comp.getDescription();
		return keys;
	}

	@Override
	public long getTime() {
		long total = 0;
		for (ProcedureComponent<?> component : this.getComponents())
			total += ProcedureUtils.Time.sumProcedureComponentTimes(component);
		return total;
	}

	@Override
	public Map<String, Long> getTimeDetailByTags() {
		return ProcedureUtils.Time.sumProcedureComponentsTimesByTags(this);
	}
	
	
	@SuppressWarnings("unused")
	private void redWithBoundaries(Collection<EquivalenceClass> equClasses, int[] red) throws Exception {
		if (RoughEquivalenceClassBasedUtils
				.Validation
				.redWithBoundaries4EquivalenceClasses(equClasses, new IntegerArrayIterator(red))
		) {
			throw new Exception("0-NEC exists with reduct: "+Arrays.toString(red));
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