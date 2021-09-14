package featureSelection.tester.procedure.heuristic.toleranceClassPositiveRegionIncremental;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.container.SelectiveComponentsProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.ToleranceClassPositiveRegionStrategy;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.support.calculation.alg.FeatureImportance4ToleranceClassPositiveRegionIncremental;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.toleranceClassPositiveRegionIncremental.component.ReductInspectionProcedureContainer;
import featureSelection.tester.procedure.heuristic.toleranceClassPositiveRegionIncremental.component.SignificantAttributeSeekingLoopProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Quick Reduct - Tolerance Class Positive Region Incremental
 * reduction</strong> for static data processing.
 * <p>
 * This is a {@link SelectiveComponentsProcedureContainer}. Procedure contains 4
 * {@link ProcedureComponent}s, referring to steps:
 * <ul>
 * 	<li><strong>Initiate</strong>: 
 * 		<p>Initialise {@link Calculation} instance & {@link ToleranceClassObtainer} instance.
 * 	</li>
 * 	<li><strong>Cache complete instances</strong>: 
 * 		<p>Use {@link ToleranceClassObtainer#getCacheInstanceGroups(Collection)} to obtain instance
 * 	    	groups for global usages in obtaining Tolerance classes.
 * 	</li>
 * 	<li><strong>Calculate Global positive region</strong>: 
 * 		<p>Calculate the global Positive Region: POS<sub>C</sub>(D) by tolerance Classes.
 * 	</li>
 * 	<li><strong>Sig loop</strong>: 
 * 		<p>Loop and seek the most significant attribute to add into reduct.
 * 		<p><code>SignificantAttributeSeekingLoopProcedureContainer</code>
 * 	</li>
 * 	<li><strong>Inspection</strong>: 
 * 		<p>Inspect redundant features in reduct.
 * 		<p><code>ReductInspectionProcedureContainer</code>
 * 	</li>
 * </ul>
 * The following parameters are required to be set in {@link ProcedureParameters}:
 * <ul>
 * 	<li>{@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_SIG_DEVIATION}</li>
 * 	<li>toleranceClassObtainerClass</li>
 * </ul>
 *
 * @see SignificantAttributeSeekingLoopProcedureContainer
 * @see ReductInspectionProcedureContainer
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class ToleranceClassPositiveRegionIncrementalHeuristicQRTester4StaticData
	extends DefaultProcedureContainer<Collection<Integer>>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				ToleranceClassPositiveRegionStrategy,
				StatisticsCalculated,
				QuickReductHeuristicReductStrategy
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	public ToleranceClassPositiveRegionIncrementalHeuristicQRTester4StaticData(ProcedureParameters parameters, boolean logOn) {
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
		
		return "QR-TCPR(static)"+
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
						Collection<Instance> instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo
									.Builder
									.newBuilder()
									.loadCurrentInfo(instances)
						);
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
						if (completeData4Attributes!=null) {
							getParameters().setNonRoot("completeData4Attributes", completeData4Attributes);
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
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Cache complete instances"),
			// 3. Calculate Global positive region.
			new TimeCountedProcedureComponent<Integer>(
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
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						FeatureImportance4ToleranceClassPositiveRegionIncremental calculation = 
								(FeatureImportance4ToleranceClassPositiveRegionIncremental)
								parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer = 
								(ToleranceClassObtainer) parameters[p++];
						InstancesCollector completeData4Attributes =
								(InstancesCollector) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return calculation.calculate(
									toleranceClassObtainer.obtain(
										instances, instances,
										new IntegerArrayIterator(attributes),
										completeData4Attributes
									).entrySet()
								).getResult();
					}, 
					(component, globalSig) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("globalPos", globalSig);
						if (logOn){
							log.info(LoggerUtil.spaceFormat(1, "globalPos = {}"), globalSig);
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
				}.setDescription("Calculate Global positive region"),
			// 4. Sig loop.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					getParameters(), 
					(component)->{
						if (logOn)	log.info("4. "+component.getDescription());
					}, 
					(component, parameters)->{
						return (Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "|Reduct Candidate| = {}"), red.size());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								new ArrayList<>(red)
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
					return shortName()+" | 4. of "+getComponents().size()+".";
				}
			}.setDescription("Sig loop.")
				.setSubProcedureContainer(
					"SignificantAttributeSeekingLoopProcedureContainer", 
					new SignificantAttributeSeekingLoopProcedureContainer(getParameters(), logOn)
				),
			// 5. Inspection
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("5. "+component.getDescription());
					}, 
					(component, parameters) -> {
						return (Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(LoggerUtil.spaceFormat(1, "|Reduct Finally| = {}"), red.size());
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_AFTER_INSPECT]
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT, red);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection")
				.setSubProcedureContainer(
					"ReductInspectionProcedureContainer", 
					new ReductInspectionProcedureContainer(getParameters(), logOn)
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