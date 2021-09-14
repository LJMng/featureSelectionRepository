package featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.harmonyInitiate;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import common.utils.LoggerUtil;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.improvedHarmonySearch.GenerationRecord;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.HarmonyFactory;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.code.HarmonyInitialization;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.iteration.optimization.BasicIterationInfo4Optimization;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.SupremeMark;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Harmony initialization of <strong>Improved Harmony Search Algorithm</strong>. Using the given 
 * {@link HarmonyInitialization} to initiate harmonies. Contains 3 {@link ProcedureComponent}s,
 * referring to steps:
 * <ul>
 * 	<li>
 * 		<strong>Harmony group initialization</strong>: 
 * 		<p>To control the initialization.
 * 	</li>
 * 	<li>
 * 		<strong>Harmony fitness initialization</strong>: 
 * 		<p>Calculate the finesses of current harmonies.
 * 	</li>
 * 	<li>
 * 		<strong>* Harmony inspection (Removed)</strong>: 
 * 		<p>Using the given inspection medthod at {@link ProcedureParameters} to execute.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <CollectionItem>
 * 		Universe or EquivalentClass.
 * @param <Hrmny>
 * 		Type of Implemented {@link Harmony}.
 */
@Slf4j
public class HarmonyInitiateProcedureContainer<Cal extends FeatureImportance<Sig>,
												Sig extends Number, 
												CollectionItem,
												Hrmny extends Harmony<?>,
												FValue extends FitnessValue<Sig>>
	extends DefaultProcedureContainer<Hrmny[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	
	public HarmonyInitiateProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "HarmonyInitialization";
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
			// 1. Harmony group initialization.
			new TimeCountedProcedureComponent<Hrmny[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_RANDOM_INSTANCE),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Sig, Hrmny, FValue> params =
								(ReductionParameters<Sig, Hrmny, FValue>) parameters[p++];
						Random random =
								(Random) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return params.getHarmonyInitializationAlg().init(params, random);
					}, 
					(component, harmonyMemoryGroup) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("harmonyMemoryGroup", harmonyMemoryGroup);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
						
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Harmony group initialization"),
			// 2. Harmony fitness initialization.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get("harmonyMemoryGroup"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Sig, Hrmny, FValue> params =
								(ReductionParameters<Sig, Hrmny, FValue>) parameters[p++];
						Collection<CollectionItem> collectionList =
								(Collection<CollectionItem>) parameters[p++];
						Hrmny[] harmonyMemoryGroup =
								(Hrmny[]) parameters[p++];
						Cal calculation =
								(Cal) parameters[p++];
						GenerationRecord<FValue> generRecord =
								(GenerationRecord<FValue>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						Fitness<Sig, FValue>[] fitnessArray = new Fitness[harmonyMemoryGroup.length];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// gBest = hGroup[0];  oldX= hGroup[0] ; gBest.fitness=0; iter = 0
						Harmony<?> gBest = null;
						Fitness<Sig, FValue> bestFitness = null;
						// For each in hGroup.
						Fitness<Sig, FValue> fitness;
						for (int h=0; h<harmonyMemoryGroup.length; h++) {
							// Calculate vector's fitness.
							fitnessArray[h] = fitness = 
								params.getRedAlg()
										.fitness(
											calculation,
											harmonyMemoryGroup[h], collectionList, params.getAttributes()
										);
							// if Xi > gBest.fitness, oldX = Xi; 
							if (bestFitness==null || fitness.compareToFitness(bestFitness)>0) {
								bestFitness = fitness;
								gBest = HarmonyFactory.copyHarmony(harmonyMemoryGroup[h]);
							}else {
								// do nothing.
							}
						}
						generRecord.updateBestFitness(bestFitness, gBest);
						return new Object[] {
								harmonyMemoryGroup,
								bestFitness,
								gBest,
								fitnessArray
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Hrmny[] harmonyMemoryGroup = (Hrmny[]) result[r++];
						Fitness<Sig, FValue> bestFitness = (Fitness<Sig, FValue>) result[r++];
						Harmony<?> gBest = (Harmony<?>) result[r++];
						Fitness<Sig, FValue>[] fitnessArray = (Fitness<Sig, FValue>[]) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("harmonyMemoryGroup", harmonyMemoryGroup);
						getParameters().setNonRoot("gBest", gBest);
						getParameters().setNonRoot("bestFitness", bestFitness);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_ITERATION_INFOS]
						GenerationRecord<FValue> generRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						ReductionParameters<Sig, Hrmny, FValue> params = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						Collection<CollectionItem> collectionItems = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS);
						
						List<BasicIterationInfo4Optimization<Number>> iterInfos;
						getParameters().setNonRoot(
								StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS,
								iterInfos =
									ProcedureUtils.Statistics.IterationInfos.pushInfoOfIteration(
										statistics.getData(),
										params.getIteration(), generRecord.getGeneration(), collectionItems, 
										params.getAttributes(),
										harmonyMemoryGroup,
										Arrays.stream(harmonyMemoryGroup).map(Hrmny::getAttributes).iterator(), 
										Arrays.stream(fitnessArray).map(f->f.getFitnessValue().getValue()).toArray(Number[]::new)
									)
							);
						
						BasicIterationInfo4Optimization<?> iterInfo = iterInfos.get(0);
						
						for (int h=0; h<harmonyMemoryGroup.length; h++) {
							if (harmonyMemoryGroup[h].getAttributes().length==gBest.getAttributes().length &&
								Arrays.equals(harmonyMemoryGroup[h].getAttributes(), gBest.getAttributes())
							) {
								iterInfo.getOptimizationEntityBasicInfo()[h]
										.setSupremeMark(SupremeMark.nextGlobalBest());
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
			}.setDescription("Harmony fitness initialization"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Hrmny[] exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray)
			this.getComponents().add(each);
		
		componentArray[0].exec();
		Hrmny[] harmony = (Hrmny[]) ((Object[]) componentArray[1].exec())[0];
		
		return harmony;
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

	@SuppressWarnings("unused")
	private String reportMark() {
		int h = (int) localParameters.get("h");
		return "Harmony["+(h+1)+"]";
	}
}