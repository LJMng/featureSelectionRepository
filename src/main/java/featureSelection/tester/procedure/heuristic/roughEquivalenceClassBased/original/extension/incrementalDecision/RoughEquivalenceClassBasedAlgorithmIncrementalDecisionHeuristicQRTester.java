package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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
import featureSelection.basic.procedure.container.SelectiveComponentsProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.roughEquivalenceClassBased.extension.IncrementalDecisionRECBasedStrategy;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.RoughEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.EquivalenceClassDecMapXtension;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalDecision.Shrink4RECBasedDecisionMapExt;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision.procedure.SigLoopPreprocessProcedureContainer;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision.procedure.SignificantAttributeSeekingLoopProcedureContainer;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision.procedure.core.ClassicCoreProcedureContainer;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision.procedure.core.ClassicImprovedCoreProcedureContainer;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision.procedure.inspect.ClassicImprovedReductInspectionProcedureContainer;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision.procedure.inspect.ClassicReductInspectionProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Incremental Decision Rough Equivalent Classes based (ID-REC)</strong> 
 * Feature Selection.
 * <p>
 * <strong>Notice</strong>: 
 * Incremental Decision Rough Equivalent Classes based (ID-REC) has been changed into <strong>
 * fine-Classified Nested Equivalent Class (C-NEC)</strong>
 * <p>
 * This is a {@link SelectiveComponentsProcedureContainer}. Procedure contains 6 {@link ProcedureComponent}s, 
 * refer to steps: 
 * <ul>
 * 	<li>
 * 		<strong>Get the Equivalence Class</strong>:
 * 		<p>Obtain the global equivalence classes induced by all attributes.
 * 	</li>
 * 	<li>
 * 		<strong>Initiate</strong>: 
 * 		<p>Initiate {@link Shrink4RECBasedDecisionMapExt}, {@link Calculation} instances, and
 * 	    	calculate the Global significance.
 * 	</li>
 * 	<li>
 * 		<strong>Obtain Core</strong>:
 * 		<p>Calculate Core. 
 * 		<p><code>ClassicCoreProcedureContainer</code>
 * 		<p><code>ClassicImprovedCoreProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>After Core / Init reduct list</strong>: 
 * 		<p>Calculate Core. 
 * 		<p><code>SigLoopPreprocessProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Sig loop</strong>: 
 * 		<p>Loop and search for the most significant attribute and add as an attribute of the reduct until 
 *	 		reaching exit condition.
 * 		<p><code>SignificantAttributeSeekingLoopProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Reduct Inspection</strong>: 
 * 		<p>Inspect the reduct and remove redundant attributes.
 * 		<p><code>ClassicReductInspectionProcedureContainer</code>
 * 		<p><code>ClassicImprovedReductInspectionProcedureContainer</code>
 * 	</li>
 * </ul>
 * 
 * @see RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalDecision
 * @see ClassicCoreProcedureContainer
 * @see ClassicImprovedCoreProcedureContainer
 * @see SigLoopPreprocessProcedureContainer
 * @see SignificantAttributeSeekingLoopProcedureContainer
 * @see ClassicReductInspectionProcedureContainer
 * @see ClassicImprovedReductInspectionProcedureContainer
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
class RoughEquivalenceClassBasedAlgorithmIncrementalDecisionHeuristicQRTester<Sig extends Number>
	extends SelectiveComponentsProcedureContainer<Collection<Integer>>
	implements TimeSum,
				StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>,
				HashSearchStrategy,
				IncrementalDecisionRECBasedStrategy,
				QuickReductHeuristicReductStrategy
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;

	private String[] componentExecOrder;
	
	public RoughEquivalenceClassBasedAlgorithmIncrementalDecisionHeuristicQRTester(ProcedureParameters paramaters, boolean logOn) {
		super(logOn, paramaters);
		statistics = new Statistics();
		report = new HashMap<>();
	}

	@Override
	public String shortName() {
		String corer;
		Collection<ProcedureContainer<?>> subCons = getComponentMap().get("Obtain Core").getSubProcedureContainers().values();
		if (subCons!=null)	corer = subCons.iterator().next().shortName();
		else				corer = "UNKNOWN";

		String inspector;
		if (this.getComponentMap().containsKey("Reduct Inspection"))
			subCons = getComponentMap().get("Reduct Inspection").getSubProcedureContainers().values();
		if (subCons!=null)	inspector = subCons.iterator().next().shortName();
		else				inspector = "UNKNOWN";
		
		return "QR-REC(Ext.ID)" +
				"(" + ProcedureUtils.ShortName.calculation(getParameters()) + ")" +
				"(" + ProcedureUtils.ShortName.byCore(getParameters())+")" +
				"(" + corer + ")"+
				"(" + inspector + ")";
	}
	
	@Override
	public String staticsName() {
		return shortName();
	}

	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public void initDefaultComponents(boolean logOn) {
		ProcedureComponent<?>[] componentArray = new ProcedureComponent<?>[] {
			// 1. Get the Equivalence Class.
			new TimeCountedProcedureComponent<Collection<EquivalenceClassDecMapXtension<Sig>>>(
					ComponentTags.TAG_COMPACT,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("1. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES)
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
						return RoughEquivalenceClassBasedExtensionAlgorithm
								.IncrementalDecision
								.Basic
								.equivalenceClass(instances, new IntegerArrayIterator(attributes));
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot("equClasses", result);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> Instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder()
								.loadCurrentInfo(Instances, false)
								.setCompressedInstanceNumber(result.size())
								.setExecutedRecordNumberNumber(result.size(), EquivalenceClass.class)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								result.size(), 
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
				}.setDescription("Get the Equivalence Class"),
			// 2. Initiate.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("equClasses"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<EquivalenceClassDecMapXtension<Sig>> equClasses =
								(Collection<EquivalenceClassDecMapXtension<Sig>>)
								parameters[p++];
						Class<? extends RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig>> calculationClass =
								(Class<? extends RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig>>)
								parameters[p++];
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation =
								calculationClass.newInstance();
						Sig globalSig = RoughEquivalenceClassBasedExtensionAlgorithm
											.IncrementalDecision
											.Basic
											.globalSignificance(
												equClasses, 
												instances.size(),
												attributes.length,
												calculation
											);
						Shrink4RECBasedDecisionMapExt<Sig> shrinkInstance =
								new Shrink4RECBasedDecisionMapExt<>();
						shrinkInstance.setCalculation(calculation);
						shrinkInstance.setUniverseSize(instances.size());
						return new Object[] {
								globalSig, 
								shrinkInstance,
								calculation
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						getParameters().setNonRoot("globalSig", result[r++]);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SHRINK_INSTANCE_INSTANCE, result[r++]);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, result[r++]);
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "global Sig = {}"), result[0]);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						//	[globalSignificance()]
						ProcedureUtils.Statistics.countInt(
								statistics.getData(),
								"RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalDecision.Basic.globalSignificance()",
								1
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
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
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate"),
			// 3. Obtain Core.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
						component.setLocalParameters(new Object[] {
							getParameters().get(ParameterConstants.PARAMETER_QR_EXEC_CORE),
						});
					},
					(component, parameters) -> {
						return parameters[0]!=null && ((Boolean) parameters[0])?
								(Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec():
								new LinkedList<>();
					}, 
					(component, result) -> {
						this.getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result);
						// Report
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								equClasses.size(), 
								result.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Obtain Core")
				.setSubProcedureContainer(
					"CoreProcedureContainer", 
					new ClassicCoreProcedureContainer<Sig>(getParameters(), logOn)
				),
			// 4. After Core / Init reduct list.
			new ProcedureComponent<Collection<RoughEquivalenceClass<EquivalenceClass>>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("4. "+component.getDescription());
					},
					(component, parameters) -> {
						return (Collection<RoughEquivalenceClass<EquivalenceClass>>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, result) -> {
						this.getParameters().setNonRoot("roughClasses", result);
						// Report
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								equClasses.size(), 
								result.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
					}
				){
					@Override public void init() {}
							
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setSubProcedureContainer(
					"SigLoopPreprocessProcedureContainer", 
					new SigLoopPreprocessProcedureContainer<Sig>(getParameters(), logOn)
				).setDescription("After Core / Init reduct list"),
			// 5. Sig loop.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					getParameters(), 
					(component)->{
						if (logOn)	log.info("5. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								component.getSubProcedureContainers().values().iterator().next()
						});
					}, 
					(component, parameters)->{
						return (Collection<Integer>) ((ProcedureContainer<?>) parameters[0]).exec();
					}, 
					(component, result)->{
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result);
						if (logOn)	log.info(LoggerUtil.spaceFormat(2, "|Reduct Candidate| = {}"), result.size());
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT, result);
						// Report
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								equClasses.size(), 
								result.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
					}
				) {
					@Override public void init() {}
						
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+".";
					}
				}.setSubProcedureContainer(
					"SignificantAttributeSeekingLoopProcedureContainer", 
					new SignificantAttributeSeekingLoopProcedureContainer<Sig>(getParameters(), logOn)
				).setDescription("Sig loop"),
			// 6. Reduct Inspection.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					getParameters(), 
					(component)->{
						if (logOn)	log.info("6. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								component.getSubProcedureContainers().values().iterator().next()
						});
					}, 
					(component, parameters)->{
						return (Collection<Integer>) ((ProcedureContainer<?>) parameters[0]).exec();
					}, 
					(component, result)->{
						// Statistics
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, result);
						if (logOn)	log.info(LoggerUtil.spaceFormat(2, "|Reduct Finally| = {}"), result.size());
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT, result);
						// Report
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								equClasses.size(), 
								result.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
					}
				) {
					@Override public void init() {}
						
					@Override public String staticsName() {
						return shortName()+" | 6. of "+getComponents().size()+".";
					}
				}.setSubProcedureContainer(
					"ReductInspectionProcedureContainer", 
					new ClassicReductInspectionProcedureContainer<Sig>(getParameters(), logOn)
				).setDescription("Reduct Inspection"),
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
		return componentsExecOrder();
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
}