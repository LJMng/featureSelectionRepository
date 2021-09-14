package featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.XieDynamicIncompleteDecisionSystemReductionStrategy;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.support.calculation.alg.xieDynamicIncrementalDSReduction.FeatureImportance4XieDynamicIncompleteDSReduction;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component.CoreProcedureContainer;
import featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component.SignificantAttributeSeekingLoopProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Xie Dynamic In-complete Decision System Reduction(DIDS)</strong> feature 
 * selection for static data processing.
 * <p>
 * This is a {@link DefaultProcedureContainer}. Procedure contains 4 {@link ProcedureComponent}s,
 * referring to steps:
 * <ul>
 * 	<li><strong>Initiate</strong>: 
 * 		<p>Initialise {@link Calculation} instance.
 * 	</li>
 * 	<li><strong>Calculate Global positive region</strong>: 
 * 		<p>Calculate the global Positive Region: POS<sub>C</sub>(D) by tolerance Classes.
 * 	</li>
 * 	<li><strong>Get core</strong>: 
 * 		<p>Calculate Core. 
 * 		<p><code>CoreProcedureContainer</code>
 * 	</li>
 * 	<li><strong>Sig loop</strong>: 
 * 		<p>Loop and seek the most significant attribute to add into reduct.
 * 		<p><code>SignificantAttributeSeekingLoopProcedureContainer</code>
 * 	</li>
 * </ul>
 * The following parameters are required to be set in {@link ProcedureParameters}:
 * <ul>
 * 	<li>{@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_QR_EXEC_CORE}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_SIG_DEVIATION}</li>
 * 	<li>toleranceClassObtainerClass</li>
 * </ul>
 *
 * @see CoreProcedureContainer
 * @see SignificantAttributeSeekingLoopProcedureContainer
 *
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class XieDynamicIncompleteDSReductionHeuristicQRTester4StaticData<Sig extends Number>
	extends DefaultProcedureContainer<Collection<Integer>>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				XieDynamicIncompleteDecisionSystemReductionStrategy,
				StatisticsCalculated,
				QuickReductHeuristicReductStrategy
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	public XieDynamicIncompleteDSReductionHeuristicQRTester4StaticData(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
	}

	@Override
	public String shortName() {
		Class<? extends ToleranceClassObtainer> toleranceClassObtainerClass =
				getParameters().get("toleranceClassObtainerClass");
		ToleranceClassObtainer toleranceClassObtainer = null;
		try {
			toleranceClassObtainer = toleranceClassObtainerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// do nothing.
		}
		
		return "QR-DIDS(static)"+
				String.format("(%s)", toleranceClassObtainer==null? null: toleranceClassObtainer.name());
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
			// 1. Initialise.
			new ProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("1. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS),
								getParameters().get("toleranceClassObtainerClass"),
							});
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Class<? extends Calculation<?>> calculationClass =
								(Class<? extends Calculation<?>>) parameters[p++];
						Class<? extends ToleranceClassObtainer> toleranceClassObtainerClass =
								(Class<? extends ToleranceClassObtainer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						return new Object[] {
								calculationClass.newInstance(),
								toleranceClassObtainerClass.newInstance()
						};
					}, 
					(component, results) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Calculation<?> calculation = (Calculation<?>) results[r++];
						ToleranceClassObtainer toleranceClassObtainer = (ToleranceClassObtainer) results[r++];
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
						this.getParameters().setNonRoot("toleranceClassObtainer", toleranceClassObtainer);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> universeInstances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder().loadCurrentInfo(universeInstances)
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
				}.setDescription("Initialise"),
			// 2. Cache complete instances.
			new TimeCountedProcedureComponent<InstancesCollector>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get("toleranceClassObtainer"),
							});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer = 
								(ToleranceClassObtainer) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return toleranceClassObtainer.getCacheInstanceGroups(instances);
					}, 
					(component, completeData4Attributes) -> {
						/* ------------------------------------------------------------------------------ */
						if (completeData4Attributes!=null)
							getParameters().setNonRoot("completeData4Attributes", completeData4Attributes);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								0
						);
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Cache complete instances"),
			// 3. Calculate Global feature significance.
			new TimeCountedProcedureComponent<Sig>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("toleranceClassObtainer"),
								getParameters().get("completeData4Attributes"),
						});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances = (Collection<Instance>) parameters[p++];
						int[] attributes = (int[]) parameters[p++];
						FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation = 
								(FeatureImportance4XieDynamicIncompleteDSReduction<Sig>)
								parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer = 
								(ToleranceClassObtainer) parameters[p++];
						InstancesCollector completeData4Attributes = 
								(InstancesCollector) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return calculation.calculate(
									instances, new IntegerArrayIterator(attributes),
									toleranceClassObtainer, completeData4Attributes
								).getResult();
					}, 
					(component, globalSig) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot("globalSig", globalSig);
						if (logOn){
							log.info(LoggerUtil.spaceFormat(1, "globalSig = {}"), globalSig);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								0
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Calculate Global feature significance"),
			// 4. Get core
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("4. "+component.getDescription());
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int[] attributes = getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES);
						Boolean byCore = getParameters().get(ParameterConstants.PARAMETER_QR_EXEC_CORE);
						/* ------------------------------------------------------------------------------ */
						return byCore!=null && byCore?
								(Collection<Integer>) 
								component.getSubProcedureContainers()
										.values()
										.iterator()
										.next()
										.exec():
								new HashSet<>(attributes.length);
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								result.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Get core")
				.setSubProcedureContainer(
					"CoreProcedureContainer", 
					new CoreProcedureContainer<>(getParameters(), logOn)
				),
			// 5. Calculate core feature significance.
			new TimeCountedProcedureComponent<Sig>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("5. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("toleranceClassObtainer"),
								getParameters().get("completeData4Attributes"),
							});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances = (Collection<Instance>) parameters[p++];
						Collection<Integer> reduct = (Collection<Integer>) parameters[p++];
						FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation = 
								(FeatureImportance4XieDynamicIncompleteDSReduction<Sig>) 
								parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer =
								(ToleranceClassObtainer) parameters[p++];
						InstancesCollector completeData4Attributes = 
								(InstancesCollector) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return calculation.calculate(
									instances, new IntegerCollectionIterator(reduct),
									toleranceClassObtainer, completeData4Attributes
								).getResult();
					}, 
					(component, redSig) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("redSig", redSig);
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "redSig = {}"), redSig);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								0
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Calculate Global feature significance"),
			// 6. Sig loop.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					getParameters(), 
					(component)->{
						if (logOn)	log.info("6. "+component.getDescription());
					}, 
					(component, parameters)->{
						return (Collection<Integer>) 
								component.getSubProcedureContainers()
										.values()
										.iterator()
										.next()
										.exec();
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "|Reduct| = {}"), red.size());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								red
						);
						//	[STATISTIC_RED_AFTER_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT,
								red
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								red.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
						/* ------------------------------------------------------------------------------ */
					}
			) {
				@Override public void init() {}
			
				@Override public String staticsName() {
					return shortName()+" | 6. of "+getComponents().size()+".";
				}
			}.setDescription("Sig loop.")
				.setSubProcedureContainer(
					"SignificantAttributeSeekingLoopProcedureContainer", 
					new SignificantAttributeSeekingLoopProcedureContainer<>(getParameters(), logOn)
				),
		};
	}

	@Override
	public long getTime() {
		return getComponents().stream()
					.map(comp->ProcedureUtils.Time.sumProcedureComponentTimes(comp))
					.reduce(Long::sum).orElse(0L);
	}

	@Override
	public Map<String, Long> getTimeDetailByTags() {
		return ProcedureUtils.Time.sumProcedureComponentsTimesByTags(this);
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return getComponents().stream().map(ProcedureComponent::getDescription).toArray(String[]::new);
	}
}