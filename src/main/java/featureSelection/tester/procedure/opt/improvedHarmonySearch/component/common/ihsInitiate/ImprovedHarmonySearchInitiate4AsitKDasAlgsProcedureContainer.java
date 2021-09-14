package featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.ihsInitiate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.utils.LoggerUtil;
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
import featureSelection.repository.entity.opt.improvedHarmonySearch.GenerationRecord;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import featureSelection.repository.support.calculation.asitKDasFitness.DefaultAsitKDasFitnessCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.roughEquivalenceClassBased.original.ihsInitiate.ImprovedHarmonySearchInitiateProcedureContainer4REC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Initialization of <strong>Improved Harmony Search Algorithm</strong>. Mainly, 
 * <ul>
 * 	<li>Creating the instance of {@link FeatureImportance}</li> 
 * 	<li>Creating a {@link GenerationRecord}</li> 
 * 	<li>Calculating max fitness if needed</li> 
 * </ul>
 * <p>
 * Different from {@link ImprovedHarmonySearchInitiateProcedureContainer}, this one initiate 
 * using the created the instance of {@link FeatureImportance} with previous reduct and other info 
 * to support calculations for data arrived. 
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * </ul>
 * <p>
 * Among them, {@link DefaultAsitKDasFitnessCalculation} instance should be set into 
 * {@link ProcedureParameters} by user with value associated key 
 * {@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}.
 * 
 * @see ImprovedHarmonySearchInitiateProcedureContainer
 * @see ImprovedHarmonySearchInitiateProcedureContainer4REC
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
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
public class ImprovedHarmonySearchInitiate4AsitKDasAlgsProcedureContainer<Cal extends FeatureImportance<Sig>,
																		Sig extends Number, 
																		CollectionItem,
																		Hrmny extends Harmony<?>,
																		FValue extends FitnessValue<Sig>>
	extends DefaultProcedureContainer<Object[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;

	public ImprovedHarmonySearchInitiate4AsitKDasAlgsProcedureContainer(ProcedureParameters parameters, boolean logOn) {
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
			// 1. IHS basic initiation.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						
						Collection<Instance> instances =
							getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						getParameters().setNonRoot(
								ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS, 
								instances
							);
						
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								instances,
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Cal calculation = (Cal) parameters[p++];
						ReductionParameters<Sig, Hrmny, FValue> params = (ReductionParameters<Sig, Hrmny, FValue>) parameters[p++];
						Collection<Instance> universes = (Collection<Instance>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						if (params.getMaxFitness()==null) {
							params.setMaxFitness(
								(FValue) params.getRedAlg()
												.fitnessValue(calculation, universes, params.getAttributes())
							);
						}
						return new Object[] {
								params.getRedAlg(),
								new GenerationRecord<>(),
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						ReductionAlgorithm<Cal, Sig, CollectionItem, FValue> redAlg = (ReductionAlgorithm<Cal, Sig, CollectionItem, FValue>) result[r++];
						GenerationRecord<FValue> generRecord = (GenerationRecord<FValue>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM, redAlg);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD, generRecord);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> universeInstances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo
									.Builder
									.newBuilder()
									.loadCurrentInfo(universeInstances)
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
			}.setDescription("IHS basic initiation"),
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
}