package featureSelection.tester.procedure.opt.genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
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
import featureSelection.basic.support.alg.roughEquivalenceClassBased.nestedEquivalenceClass.NestedEquivalenceClassBasedStrategy;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.basic.support.dataFormat.dynamicData.IncrementalStreamingData;
import featureSelection.basic.support.reductMiningStrategy.optimization.GeneticAlgorithmReductMiningStrategy;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedUtils;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge.NestedEquivalenceClassesMergerParameters;
import featureSelection.repository.entity.alg.rec.nestedEC.reductionResult.ReductionResult4Streaming;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.ReductionParameters;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.initlization.ChromosomeInitialization;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedStreamingDataCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.genetic.procedure.common.chromosomeInitiate.ChromosomeInitiateProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.common.generationLoop.DefaultGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.common.generationLoop.ReductPrioritizedGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.inspect.IPNECInspectionProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.singleSolutionSelection.KnowledgeGranularityBasedSingleSolutionSelectionProcedureContainer4NEC;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.gaInitiate.GeneticAlgorithmInitiateProcedureContainer4IPNECStreaming;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.inspection.IPNECInspectionProcedureContainer4Streaming;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.inspection.IPNECSkipInspectionProcedureContainer4Streaming;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.iteration.optimization.BasicIterationInfo4Optimization;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.OptEntityBasicInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Genetic Algorithm</strong> <code>PLUS</code> <strong>Feature 
 * Selection</strong> (<strong>IP-NEC for Streaming data</strong>, compatible with <i>Static data</i>).
 * <p>
 * This is a {@link SelectiveComponentsProcedureContainer}. Procedure contains 5 
 * {@link ProcedureComponent}s, refer to steps: 
 * <ul>
 *  <li>
 *  	<strong>Initiate</strong>:
 *  	<p>Some initializations for Genetic Algorithm. In the process, if no <code>0-NEC</code> is
 *  		found in the merged/updated {@link NestedEquivalenceClass}es, <code>exitMark</code> is
 *  		set to <code>true</code> and no further action will be made in the follow steps in the
 *  		tester.
 *  	<p><code>GeneticAlgorithmInitiateProcedureContainer4NECStreaming</code>
 *  </li>
 *  <li>
 *  	<strong>Initiate chromosomes</strong>:
 *  	<p>Initiate chromosomes randomly using {@link ChromosomeInitialization} and available attributes given
 *  		by user.
 *  	<p><code>ChromosomeInitiateProcedureContainer</code>
 *  </li>
 *  <li>
 *  	<strong>Generation loops</strong>:
 *  	<p>Loop generations, calculate chromosomes' fitness, cross-over, mutate. Until reaching maximum
 *  		fitness && maximum iteration/maximum convergence.
 *  	<p><code>ReductPrioritizedGenerationLoopProcedureContainer</code>
 *  	<p>Please <strong>don't use</strong>
 *  		<code>DefaultGenerationLoopProcedureContainer</code> or
 *  		<code>GlobalBestPrioritizedGenerationLoopProcedureContainer</code>
 *  		'cause errors may occur in <code>Chromosome to reducts</code> if no reduct was found.
 *  </li>
 *  <li>
 *  	<strong>Chromosome to reducts</strong>:
 *  	<p>Transfer chromosomes to reducts.
 *  	<p><code>IPNECInspectionProcedureContainer</code>
 *  	<p><code>IPNECInspectionProcedureContainer4Streaming</code>
 *  	<p><code>IPNECSkipInspectionProcedureContainer4Streaming</code>
 *  </li>
 *  <li>
 *  	<strong>Return reducts</strong>:
 *  	<p>Return reducts as result.  {@link ReductionResult4Streaming} instances are set in this
 *  		procedure.
 *  	<p>If returning single solution is set in parameter settings, execute solution selection.
 *  	<p><code>KnowledgeGranularityBasedSingleSolutionSelectionProcedureContainer4NEC</code>
 *  </li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_REDUCT}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_COLLECTION_ITEM}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_REDUCT_LIST_AFTER_INSPECTATION}</li>
 * </ul>
 * 
 * @author Benjamin_L
 * 
 * @see GeneticAlgorithmInitiateProcedureContainer4IPNECStreaming
 * @see ChromosomeInitiateProcedureContainer
 * @see DefaultGenerationLoopProcedureContainer
 * @see IPNECInspectionProcedureContainer
 * @see IPNECInspectionProcedureContainer4Streaming
 * @see IPNECSkipInspectionProcedureContainer4Streaming
 * @see KnowledgeGranularityBasedSingleSolutionSelectionProcedureContainer4NEC
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
@RoughSet
@ThreadSafetyNotSecured
public class GeneticAlgorithmIPNECStreamingFeatureSelectionTester<Cal extends NestedEquivalenceClassBasedStreamingDataCalculation<Sig,? extends NestedEquivalenceClassesMergerParameters>,
																	Sig extends Number,
																	CollectionItem,
																	Chr extends Chromosome<?>,
																	FValue extends FitnessValue<?>>
	extends SelectiveComponentsProcedureContainer<ReductionResult4Streaming<Sig>[]>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				StatisticsCalculated,
				NestedEquivalenceClassBasedStrategy,
				GeneticAlgorithmReductMiningStrategy,
				IncrementalStreamingData
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	protected String[] componentExecOrder;
	
	public GeneticAlgorithmIPNECStreamingFeatureSelectionTester(ProcedureParameters parameters, boolean logOn) {
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
								component.getSubProcedureContainers()
										.values()
										.iterator()
										.next()
										.exec();
					}, 
					null
				){
					@Override public void init() {}
	
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate")
			.setSubProcedureContainer(
				"GeneticAlgorithmInitiateProcedureContainer", 
				new GeneticAlgorithmInitiateProcedureContainer4IPNECStreaming<>(getParameters(), logOn)
			),
			// 2. Initiate chromosomes.
			new ProcedureComponent<Chr[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
					}, 
					(component, parameters) -> {
						if (!ProcedureUtils.procedureExitMark(getParameters())) {
							return (Chr[]) component.getSubProcedureContainers()
													.values()
													.iterator()
													.next()
													.exec();
						}else {
							return null;
						}
					}, 
					(component, chromosome)->{
						/* ------------------------------------------------------------------------------ */
						if (ProcedureUtils.procedureExitMark(getParameters()))	return;
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
	
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate chromosomes")
			.setSubProcedureContainer(
				"ChromosomeInitiateProcedureContainer", 
				new ChromosomeInitiateProcedureContainer<>(getParameters(), logOn)
			),
			// 3. Generation loops.
			new ProcedureComponent<GenerationRecord<Chr, FValue>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
					}, 
					(component, parameters) -> {
						if (!ProcedureUtils.procedureExitMark(getParameters())) {
							return (GenerationRecord<Chr, FValue>) 
										component.getSubProcedureContainers()
												.values()
												.iterator()
												.next()
												.exec();
						}else {
							return null;
						}
					}, 
					(component, geneRecord)->{
						/* ------------------------------------------------------------------------------ */
						if (ProcedureUtils.procedureExitMark(getParameters()))	return;
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
				}.setDescription("Generation loops")
			.setSubProcedureContainer(
				"GenerationLoopProcedureContainer", 
				new ReductPrioritizedGenerationLoopProcedureContainer<>(getParameters(), logOn)
			),
			// 4. Chromosome to reducts.
			new ProcedureComponent<Object[]>(
					ComponentTags.TAG_INSPECT_LAST,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("4. "+component.getDescription());
					},
					(component, parameters) -> {
						if (!ProcedureUtils.procedureExitMark(getParameters())) {
							return (Object[])
									component.getSubProcedureContainers()
												.values()
												.iterator()
												.next()
												.exec();
						}else {
							return null;
						}
					}, 
					null
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Chromosome to reducts")
			.setSubProcedureContainer(
				"InspectionProcedureContainer", 
				new IPNECInspectionProcedureContainer4Streaming<>(getParameters(), logOn)
//				new IPNECSkipInspectionProcedureContainer4Streaming<>(getParameters(), logOn)
			),
			// 5. Return reducts.
			new ProcedureComponent<ReductionResult4Streaming<Sig>[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("5. "+component.getDescription());
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int[] reduct = getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT);
						Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> reductNestedEquClasses =
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM);
						ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue> params =
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
							GenerationRecord<Chr, FValue> geneRecord = 
									getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
							Collection<EquivalenceClass> equClasses =
									getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS);
								
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
													geneRecord.getBestFitness().getValue(),
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
								
								return new ReductionResult4Streaming[] {
									new ReductionResult4Streaming<>(
											false, 
											equClasses,
											chosenReduct,
											geneRecord.getBestFitness().getValue(),
											null
										)
								};
							}
						// New reduct is found. (0-NEC is not empty)
						}else {
							// load fields from parameters.
							Map<IntArrayKey, Collection<OptimizationReduct>> newReducts = 
									getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION);
							Sig newGlobalSig = getParameters().get(ParameterConstants.PARAMETER_NEW_GLOBAL_SIG);
							NestedEquivalenceClassBasedStreamingDataCalculation<Sig,? extends NestedEquivalenceClassesMergerParameters> calculation =
									getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE);
							
							@SuppressWarnings("rawtypes")
							ReductionResult4Streaming[] result = new ReductionResult4Streaming[newReducts.size()];
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
								int i=0;
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
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_ITERATION_INFOS]
						ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue> params = 
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						if (params.getSingleSolutionOnly()==null || !params.getSingleSolutionOnly()) {
							List<BasicIterationInfo4Optimization<?>> iterInfos =
									getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS);
							
							if (iterInfos!=null) {
								for (int i=iterInfos.size()-1; i>=0; i--) {
									boolean breakLoop = false;
									// Loop over entity info.s and filter inspected ones with the same full length 
									//	(previous reduct included) as the solution's.
									// Searching should only carry out among those latest global best.
									for (OptEntityBasicInfo<?> entityInfo: iterInfos.get(i).getOptimizationEntityBasicInfo()) {
										// If finished searching in the latest global best, break loop.
										if (entityInfo.getSupremeMark()!=null && 
											entityInfo.getSupremeMark().getRank()==1L
										) {
											breakLoop = true;
										}
										// set solution.
										if (entityInfo.getInspectedReduct()!=null)	entityInfo.setSolution(true);
									}
									if (breakLoop)	break;
								}
							}
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
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