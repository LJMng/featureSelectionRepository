package featureSelection.tester.procedure.heuristic.liuRoughSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
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
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.LiuRoughSetStrategy;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.repository.algorithm.alg.liuRoughSet.LiuRoughSetAlgorithm;
import featureSelection.repository.support.calculation.alg.FeatureImportance4LiuRoughSet;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.liuRoughSet.procedure.CoreProcedureContainer;
import featureSelection.tester.procedure.heuristic.liuRoughSet.procedure.ReductInspectionProcedureContainer;
import featureSelection.tester.procedure.heuristic.liuRoughSet.procedure.SignificantAttributeSeekingLoopProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Quick Reduct - Liu Rough Set</strong> Feature Selection.
 * <p>
 * Original paper:
 * <a href="http://cjc.ict.ac.cn/quanwenjiansuo/2003-05.pdf">"Research on Efficient Algorithms for
 * Rough Set Methods"</a> by Wei Wei, Junhong Wang, Jiye Liang, Xin Mi, Chuangyin Dang.
 * <p>
 * This is a {@link DefaultProcedureContainer}. Procedure contains 5 {@link ProcedureComponent}s,
 * refer to steps:
 * <ul>
 * 	<li>
 * 		<strong>Get global positive region</strong>:
 * 		<p>Obtain the global positive region: <i>sig(U/C)</i>.
 * 	</li>
 * 	<li>
 * 		<strong>Get Core</strong>:
 * 		<p>Obtain Core.
 * 		<p><code>CoreProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Sig loop preparation</strong>:
 * 		<p>Obtain (Rough) Equivalence Classes({@link Collection}s of {@link Instance}
 * 			{@link Collection}) of the current reduct(i.e. Core).
 * 	</li>
 * 	<li>
 * 		<strong>Sig loop</strong>:
 * 		<p>Loop and search for the most significant attribute and add into reduct until reaching
 * 	    	exit condition.
 * 		<p><code>SignificantAttributeSeekingLoopProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Inspection</strong>:
 * 		<p>Inspect the reduct and remove redundant attributes.
 * 		<p><code>ReductInspectionProcedureContainer</code>
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_REDUCT_LIST}</li>
 * </ul>
 *
 *
 * @see FeatureImportance4LiuRoughSet
 * @see LiuRoughSetAlgorithm
 * @see CoreProcedureContainer
 * @see SignificantAttributeSeekingLoopProcedureContainer
 * @see ReductInspectionProcedureContainer
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class LiuRoughSetHeuristicQRTester<Sig extends Number>
	extends DefaultProcedureContainer<Collection<Integer>>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				StatisticsCalculated,
				LiuRoughSetStrategy,
				QuickReductHeuristicReductStrategy
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	public LiuRoughSetHeuristicQRTester(ProcedureParameters paramaters, boolean logOn) {
		super(logOn? log: null, paramaters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
	}
	
	@Override
	public String shortName() {
		return "QR-Liu-RoughSet"+
				"("+ ProcedureUtils.ShortName.calculation(getParameters())+")"+
				"("+ ProcedureUtils.ShortName.byCore(getParameters())+")";
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
			// 1. Get global positive region
			new TimeCountedProcedureComponent<Sig>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("1. "+component.getDescription());
						}
						
						FeatureImportance4LiuRoughSet<Sig> calculation =
								((Class<? extends FeatureImportance4LiuRoughSet<Sig>>)
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS))
												.newInstance();
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								calculation
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						FeatureImportance4LiuRoughSet<Sig> calculation =
								(FeatureImportance4LiuRoughSet<Sig>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						calculation.calculate(instances,  new IntegerArrayIterator(attributes), instances.size());
						return calculation.getResult();
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("globalSig", result);
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Global pos = {}"),
									result
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> Instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder()
								.loadCurrentInfo(Instances)
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
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Get global positive region"),
			// 2. Get Core 
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("2. "+component.getDescription());
						}
					},
					(component, parameters) -> {
						return (Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, core) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("core", core);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								core.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Get Core")
				.setSubProcedureContainer(
					"CoreProcedureContainer", 
					new CoreProcedureContainer<>(this.getParameters(), logOn)
				),
			// 3. Sig loop preparation
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("3. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get("core"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						List<Instance> instances =
								(List<Instance>) parameters[p++];
						Collection<Integer> core =
								(Collection<Integer>) parameters[p++];
						FeatureImportance4LiuRoughSet<Sig> calculation =
								(FeatureImportance4LiuRoughSet<Sig>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						List<Integer> red = new LinkedList<>(core);
						Collection<Collection<Instance>> roughClasses =
								LiuRoughSetAlgorithm
									.Basic
									.equivalenceClass(
											instances,
											new IntegerCollectionIterator(red)
									);
						Sig redSig = calculation.calculate(instances, new IntegerCollectionIterator(red), instances.size())
												.getResult();
						return new Object[] {
								red, 
								redSig, 
								roughClasses
							};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						List<Integer> red = (List<Integer>) result[r++];
						Sig redSig = (Sig) result[r++];
						Collection<Collection<Instance>> roughClasses = (Collection<Collection<Instance>>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						getParameters().setNonRoot("redSig", redSig);
						getParameters().setNonRoot("roughClass", roughClasses);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_POS_HISTORY]
						List<Sig> posHistory =
								(List<Sig>) statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (posHistory==null){
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY,
									posHistory = new LinkedList<>()
							);
						}
						posHistory.add((Sig) result[1]);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								((Collection<?>) result[0]).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						//	[REPORT_SIG_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, component.getDescription(),
								ReportConstants.Procedure.REPORT_SIG_HISTORY,
								redSig
						);
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Sig loop preparation"),
			// 4. Sig loop
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("4. "+component.getDescription());
					}, 
					(component, parameters) -> {
						return (Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result);
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "|Reduct Candidate| = {}"),
									result.size()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT, result);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 6. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Sig loop")
				.setSubProcedureContainer(
					"SignificantAttributeSeekingLoopProcedureContainer", 
					new SignificantAttributeSeekingLoopProcedureContainer<Sig>(getParameters(), logOn)
				),
			// 5. Inspection
			new ProcedureComponent<List<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("5. "+component.getDescription());
						}
					}, 
					(component, parameters) -> {
						return (List<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "|Reduct Finally| = {}"),
									result.size()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT, result);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
					}
				){
					@Override public void init() {}
						
					@Override public String staticsName() {
						return shortName()+" | 7. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection")
				.setSubProcedureContainer(
					"ReductInspectionProcedureContainer",
					new ReductInspectionProcedureContainer<Sig>(getParameters(), logOn)
				),
		};
	}

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