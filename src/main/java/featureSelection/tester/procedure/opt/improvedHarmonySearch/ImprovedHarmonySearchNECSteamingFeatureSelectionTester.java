package featureSelection.tester.procedure.opt.improvedHarmonySearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.utils.CollectionUtils;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.container.SelectiveComponentsProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.support.reductMiningStrategy.optimization.ImprovedHarmonySearchReductMiningStrategy;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedUtils;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge.NestedEquivalenceClassesMergerParameters;
import featureSelection.repository.entity.alg.rec.nestedEC.reductionResult.ReductionResult4Streaming;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.improvedHarmonySearch.GenerationRecord;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedStreamingDataCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.generationLoop.DefaultGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.generationLoop.GlobalBestPrioritizedGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.generationLoop.ReductPrioritizedGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.harmonyInitiate.HarmonyInitiateProcedureContainer;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.ihsInitiate.ImprovedHarmonySearchInitiateProcedureContainer4NECStreaming;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.inspection.IPNECInspectionProcedureContainer4Streaming;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.inspection.IPNECSkipInspectionProcedureContainer4Streaming;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.singleSolutionSelection.KnowledgeGranularityBasedSingleSolutionSelectionProcedureContainer4NEC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Improved Harmony Search Algorithm</strong> <code>PLUS</code> 
 * <strong>Feature Selection (IP-NEC for <strong>Streaming data</strong>)</strong>. 
 * <p>
 * This is a {@link SelectiveComponentsProcedureContainer}. Procedure contains 5
 * {@link ProcedureComponent}s, referring to steps:
 * <ul>
 *  <li>
 *  	<strong>Initiate</strong>:
 *  	<p>Some initializations for Improved Harmony Search Algorithm. In the process, if no <code>0-NEC
 *      	</code> is found in the merged/updated {@link NestedEquivalenceClass}es, <code>exitMark
 *      	</code> is set to <code>true</code> and no further action will be made in the follow steps
 *      	in the tester.
 *  	<p><code>ImprovedHarmonySearchInitiateProcedureContainer4NECStreaming</code>,
 *  </li>
 *  <li>
 *  	<strong>Initiate harmonies</strong>:
 *  	<p>Initiate harmonies randomly.
 *  	<p><code>HarmonyInitiateProcedureContainer</code>
 *  </li>
 *  <li>
 *  	<strong>Generation loops</strong>:
 *  	<p>Loop generations, calculate harmonies' fitness, "study". Until reaching maximum
 *  		iteration/maximum convergence/maximum fitness.
 *  	<p><code>DefaultGenerationLoopProcedureContainer</code>
 *  	<p><code>GlobalBestPrioritizedGenerationLoopProcedureContainer</code>
 *  	<p><code>ReductPrioritizedGenerationLoopProcedureContainer</code>
 *  </li>
 *  <li>
 *  	<strong>Harmony to reducts</strong>:
 *  	<p>Transfer harmonies into reducts.
 *  	<p><code>IPNECInspectionProcedureContainer4Streaming</code>,
 *  	<p><code>IPNECSkipInspectionProcedureContainer4Streaming</code>
 *  </li>
 *  <li>
 *  	<strong>Return reducts</strong>:
 *  	<p>Return reducts as result. (Nothing particular)
 *  	<p><code>ProcedureComponent</code>
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
 * @see ImprovedHarmonySearchInitiateProcedureContainer4NECStreaming
 * @see HarmonyInitiateProcedureContainer
 * @see DefaultGenerationLoopProcedureContainer
 * @see GlobalBestPrioritizedGenerationLoopProcedureContainer
 * @see ReductPrioritizedGenerationLoopProcedureContainer
 * @see IPNECInspectionProcedureContainer4Streaming
 * @see IPNECSkipInspectionProcedureContainer4Streaming
 *
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <Hrmny>
 * 		Type of Implemented {@link Harmony}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class ImprovedHarmonySearchNECSteamingFeatureSelectionTester<Sig extends Number,
																	Hrmny extends Harmony<?>,
																	FValue extends FitnessValue<?>>
	extends SelectiveComponentsProcedureContainer<ReductionResult4Streaming<Sig>[]>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				StatisticsCalculated,
				ImprovedHarmonySearchReductMiningStrategy
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	private String[] componentExecOrder;
	
	public ImprovedHarmonySearchNECSteamingFeatureSelectionTester(ProcedureParameters parameters, boolean logOn) {
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
				"ImprovedHarmonySearchInitiateProcedureContainer",
				new ImprovedHarmonySearchInitiateProcedureContainer4NECStreaming<>(getParameters(), logOn)
			),
			// 2. Initiate harmonies.
			new ProcedureComponent<Hrmny[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(),
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
					},
					(component, parameters) -> {
						if (!ProcedureUtils.procedureExitMark(getParameters())) {
							return (Hrmny[])
									CollectionUtils.firstOf(component.getSubProcedureContainers().values())
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
				"HarmonyInitiateProcedureContainer",
				new HarmonyInitiateProcedureContainer<>(getParameters(), logOn)
			),
			// 3. Generation loops.
			new ProcedureComponent<GenerationRecord<FValue>>(
					ComponentTags.TAG_SIG,
					this.getParameters(),
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
					},
					(component, parameters) -> {
						if (!ProcedureUtils.procedureExitMark(getParameters())) {
							return (GenerationRecord<FValue>)
									CollectionUtils.firstOf(component.getSubProcedureContainers().values())
													.exec();
						}else {
							return null;
						}
					},
					(component, geneRecord)->{
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
			// 4. Harmony to reducts.
			new ProcedureComponent<Map<IntArrayKey, Collection<OptimizationReduct>>>(
					ComponentTags.TAG_INSPECT_LAST,
					this.getParameters(),
					(component) -> {
						if (logOn)	log.info("4. "+component.getDescription());
					},
					(component, parameters) -> {
						if (!ProcedureUtils.procedureExitMark(getParameters())) {
							return (Map<IntArrayKey, Collection<OptimizationReduct>>)
									CollectionUtils.firstOf(component.getSubProcedureContainers().values())
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
				}.setDescription("Harmony to reducts")
				.setSubProcedureContainer(
					"InspectionProcedureContainer",
					new IPNECInspectionProcedureContainer4Streaming<>(getParameters(), logOn)
//					new IPNECSkipInspectionProcedureContainer4Streaming<>(getParameters(), logOn)
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
						ReductionParameters<Sig, Hrmny, FitnessValue<Sig>> params =
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
							GenerationRecord<FValue> geneRecord =
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
									result[i++] =
											new ReductionResult4Streaming<>(
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
									// reduct = reduct U newReduct
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
}