package featureSelection.tester.procedure.heuristic.liuQuickHash;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
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
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.LiuQuickHashStrategy;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.repository.algorithm.alg.quickHash.LiuQuickHashAlgorithm;
import featureSelection.repository.entity.alg.liuQuickHash.EquivalenceClass;
import featureSelection.repository.entity.alg.liuQuickHash.RoughEquivalenceClass;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.liuQuickHash.procedure.ReductInspectionProcedureContainer;
import featureSelection.tester.procedure.heuristic.liuQuickHash.procedure.SignificantAttributeSeekingLoopProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Quick Reduct - Liu Quick Hash</strong> Feature Selection.
 * <p>
 * Original paper:
 * <a href="http://cjc.ict.ac.cn/quanwenjiansuo/2009-8/ly.pdf">"Quick Attribute Reduction Algorithm
 * with Hash"</a> by Liu Yong, Xiong Rong, Chu Jian.
 * <p>
 * This is a {@link DefaultProcedureContainer}. Procedure contains 5 {@link ProcedureComponent}s,
 * refer to steps:
 * <ul>
 * 	<li>
 * 		<strong>Get the equivalence classes</strong>:
 * 		<p>Obtain the Equivalence Classes induced by C.
 * 	</li>
 * 	<li>
 * 		<strong>Calculate global positive & negative region</strong>:
 * 		<p>Calculate the global Positive & Negative regions of U/C.
 * 	</li>
 * 	<li>
 * 		<strong>Sig loop preparation</strong>:
 * 		<p>Wrap all Equivalence Classes induced by C into one Rough Equivalence Class.
 * 	</li>
 * 	<li>
 * 		<strong>Sig loop</strong>:
 * 		<p>Loop and search for significant attributes as reduct attributes until exit.
 * 		<p><code>SignificantAttributeSeekingLoopProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Inspection</strong>:
 * 		<p>Inspect the reduct and remove redundant attributes.
 * 		<p><code>ReductInspectionProcedureContainer</code>
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following basic parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>equClasses</li>
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
public class LiuQuickHashHeuristicQRTester
	extends DefaultProcedureContainer<Collection<Integer>>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				StatisticsCalculated,
				LiuQuickHashStrategy,
				QuickReductHeuristicReductStrategy
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	public LiuQuickHashHeuristicQRTester(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
	}
	
	@Override
	public String shortName() {
		return "QR-Liu-QuickHash"+
				"("+ ProcedureUtils.ShortName.byCore(getParameters())+")"+
				"("+ ProcedureUtils.ShortName.calculation(getParameters())+")";
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
			// 1. Get the equivalence classes
			new TimeCountedProcedureComponent<Collection<EquivalenceClass>>(
					ComponentTags.TAG_COMPACT,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("1. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
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
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return LiuQuickHashAlgorithm.equivalenceClass(
									instances,
									new IntegerArrayIterator(attributes)
								);
					}, 
					(component, equClasses) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("equClasses", equClasses);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> Instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder()
								.loadCurrentInfo(Instances, false)
								.setCompressedInstanceNumber(equClasses.size())
								.setExecutedRecordNumberNumber(equClasses.size(), EquivalenceClass.class)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								equClasses.size(), 
								0
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Get the equivalence classes"),
			// 2. Calculate global positive & negative region
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("2. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("equClasses"),
						});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Count in-consistent instances and collect global positive region.
						int unconsGP = 0;
						Collection<EquivalenceClass> globalPositive = new HashSet<>();
						for (EquivalenceClass equ : equClasses) {
							if (!equ.cons())	unconsGP += equ.instanceSize();
							else				globalPositive.add(equ);
						}
						return new Object[] {
								globalPositive,
								unconsGP,
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<EquivalenceClass> globalPositive = (Collection<EquivalenceClass>) result[r++];
						int unconsGP = (int) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().set(false, "globalPositive", globalPositive);
						getParameters().set(false, "unconsGP", unconsGP);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							int pos = 0;	for (EquivalenceClass e: globalPositive)	pos+= e.instanceSize();
							log.info(
									LoggerUtil.spaceFormat(1, "Pos = {}, Neg = {}"),
									pos, unconsGP
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>)getParameters().get("equClasses")).size(), 
								0
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Calculate global positive & negative region"),
			// 3. Sig loop preparation
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("3. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("equClasses"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Initiate rough equivalence class induced by none attribute: all equivalence
						//  classes are considered equivalent.
						Collection<RoughEquivalenceClass> roughClasses = new HashSet<>(1);
						RoughEquivalenceClass roughClass = new RoughEquivalenceClass();
						for (EquivalenceClass equ: equClasses) {
							roughClass.addAnEquivalenceClass(equ);
						}
						roughClasses.add(roughClass);
						return new Object[] {
								new HashSet<>(), 
								roughClasses
							};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<Integer> red = (Collection<Integer>) result[r++];
						Collection<RoughEquivalenceClass> roughClasses = (Collection<RoughEquivalenceClass>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						getParameters().setNonRoot("roughClasses", roughClasses);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>)getParameters().get("equClasses")).size(), 
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
				}.setDescription("Sig loop preparation"),
			// 4. Sig loop
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("6. "+component.getDescription());
						}
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
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								result
						);
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
						return shortName()+" | 6. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Sig loop")
				.setSubProcedureContainer(
					"SignificantAttributeSeekingLoopProcedureContainer", 
					new SignificantAttributeSeekingLoopProcedureContainer(getParameters(), logOn)
				),
			// 5. Inspection
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("5. "+component.getDescription());
						}
					}, 
					(component, parameters) -> {
						return (Collection<Integer>)
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
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT,
								result
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
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