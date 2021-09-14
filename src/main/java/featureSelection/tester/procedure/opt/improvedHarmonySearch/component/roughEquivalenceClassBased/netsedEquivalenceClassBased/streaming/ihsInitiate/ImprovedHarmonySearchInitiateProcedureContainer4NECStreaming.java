package featureSelection.tester.procedure.opt.improvedHarmonySearch.component.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.ihsInitiate;

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
import featureSelection.repository.entity.opt.improvedHarmonySearch.GenerationRecord;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.fitness.fValue.FitnessValue4Integer;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedStreamingDataCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.harmonyInitiate.HarmonyInitiateProcedureContainer;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.ihsInitiate.ImprovedHarmonySearchInitiateProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Initialization of <strong>Improved Harmony Search Algorithm</strong>. Besides initializations in 
 * {@link HarmonyInitiateProcedureContainer}, also generating {@link EquivalenceClass}es as
 * {@link ParameterConstants#PARAMETER_OPTIMIZATION_COLLECTION_ITEMS}
 * for collection items in <code>Improved Harmony Search Algorithm</code> execution.
 * <p>
 * This is a {@link DefaultProcedureContainer}. Procedure contains 5 {@link ProcedureComponent}s,
 * referring to steps:
 * <ul>
 * 	<li>
 * 		<strong>Get the Equivalence Class</strong>
 * 		<p>Compute and obtain the {@link EquivalenceClass}es.
 * 	</li>
 * 	<li>
 * 		<strong>Update and check if reduct re-searching is necessary: true-continue; false-exit.</strong>
 * 		<p>Update {@link NestedEquivalenceClass}es and {@link EquivalenceClass}es. Re-search if 0-NEC
 * 	    	exists.
 * 	</li>
 * 	<li>
 * 		<strong>IHS basic initialization</strong>
 * 		<p>Creating a {@link GenerationRecord} and calculating max fitness if needed
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_COLLECTION_ITEM}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_REDUCT}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_REDUCT_SIG}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_IP_NEC_DYNAMIC_DATA_EQUIVALENCE_CLASS_MERGER}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_COLLECTION_ITEMS}</li>
 * </ul>
 * 
 * @see ImprovedHarmonySearchInitiateProcedureContainer
 * @see NestedEquivalenceClassBasedStreamingDataCalculation
 * @see NestedEquivalenceClassesMergerParameters
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <MergeParams>
 *     Type of implemented {@link NestedEquivalenceClassesMergerParameters}.
 * @param <Hrmny>
 * 		Type of Implemented {@link Harmony}.
 */
@Slf4j
public class ImprovedHarmonySearchInitiateProcedureContainer4NECStreaming<Cal extends NestedEquivalenceClassBasedStreamingDataCalculation<Integer, MergeParams>,
																			MergeParams extends NestedEquivalenceClassesMergerParameters,
																			Hrmny extends Harmony<?>>
	extends DefaultProcedureContainer<Object[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;

	public ImprovedHarmonySearchInitiateProcedureContainer4NECStreaming(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new ArrayList<>(1);
	}

	@Override
	public String shortName() {
		return "Initiation";
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
			// 1. Get arrived equivalence classes.
			new TimeCountedProcedureComponent<Object[]>(
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
						return new Object[] {
							NestedEquivalenceClassBasedAlgorithm
								.Basic
								.equivalenceClass(
									instances,
									new IntegerArrayIterator(attributes)
								)
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Map<IntArrayKey, EquivalenceClass> equClasses = (Map<IntArrayKey, EquivalenceClass>) result[r++];
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
							log.info(LoggerUtil.spaceFormat(2, "Previouse instances: {}"), previouseUniverseSize);
							log.info(LoggerUtil.spaceFormat(2, "Arrived instances: {}"), arrivedUniverseSize);
							log.info(LoggerUtil.spaceFormat(2, "Total instances: {}"), (previouseUniverseSize+arrivedUniverseSize));
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
				}.setDescription("Get arrived equivalence classes"),
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
						Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>
								previousNestedEquClasses =
								(Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>)
								parameters[p++];
						int[] reduct =
								(int[]) parameters[p++];
						Integer previousReductSig =
								(Integer) parameters[p++];
						Map<IntArrayKey, EquivalenceClass> arrivedEquivalenceClasses =
								(Map<IntArrayKey, EquivalenceClass>)
								parameters[p++];
						NestedEquivalenceClassesMerger<MergeParams, NestedEquivalenceClass<EquivalenceClass>>
								merger =
								(NestedEquivalenceClassesMerger<MergeParams, NestedEquivalenceClass<EquivalenceClass>>)
								parameters[p++];
						ReductionParameters<Integer, Hrmny, FitnessValue<Integer>> params =
								(ReductionParameters<Integer, Hrmny, FitnessValue<Integer>>)
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
										if (equClass.sortable())	posCount+=equClass.getInstanceSize();
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
						Integer newSig = (Integer) result[r++];
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
			// 3. IHS basic initiation.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Cal calculation = (Cal) parameters[p++];
						ReductionParameters<Integer, Hrmny, FitnessValue<Integer>> params =
								(ReductionParameters<Integer, Hrmny, FitnessValue<Integer>>) parameters[p++];
						Collection<NestedEquivalenceClass<EquivalenceClass>> boundaries =
								(Collection<NestedEquivalenceClass<EquivalenceClass>>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						if (ProcedureUtils.procedureExitMark(getParameters())) {
							return null;
						}else {
							if (params.getMaxFitness()==null) {
								params.setMaxFitness(
									(FitnessValue<Integer>) params.getRedAlg()
																.fitnessValue(calculation, boundaries, params.getAttributes())
								);
							}
							return new Object[] {
									calculation,
									params.getRedAlg(),
									new GenerationRecord<>(),
							};
						}
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						if (result!=null) {
							int r=0;
							Cal calculation = (Cal) result[r++];
							ReductionAlgorithm<Cal, Integer, ?, FitnessValue<Integer>> redAlg =
									(ReductionAlgorithm<Cal, Integer, ?, FitnessValue<Integer>>) result[r++];
							GenerationRecord<FitnessValue<Integer>> generRecord =
									(GenerationRecord<FitnessValue<Integer>>) result[r++];
							/* ------------------------------------------------------------------------------ */
							getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
							getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM, redAlg);
							getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD, generRecord);
							/* ------------------------------------------------------------------------------ */
						}
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
			}.setDescription("IHS basic initiation"),
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