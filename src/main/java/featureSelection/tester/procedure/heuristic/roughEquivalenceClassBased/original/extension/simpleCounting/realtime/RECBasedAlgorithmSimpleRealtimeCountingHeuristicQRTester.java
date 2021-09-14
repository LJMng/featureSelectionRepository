package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.simpleCounting.realtime;

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
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.container.SelectiveComponentsProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.roughEquivalenceClassBased.extension.SimpleCountingRECBasedStrategy;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.simpleCounting.realtime.procedure.SigLoopPreprocessProcedureContainer;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.simpleCounting.realtime.procedure.SignificantAttributeSeekingLoopProcedureContainer;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.simpleCounting.realtime.procedure.core.ClassicCoreProcedureContainer;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.simpleCounting.realtime.procedure.inspect.ClassicReductInspectionProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Quick Reduct - Rough Equivalence Class based extension: Simple
 * Counting(Real-time)</strong> Feature Selection.
 * <p>
 * Published paper:
 * <a href="https://www.sciencedirect.com/science/article/pii/S0020025520302723">NEC: A nested
 * equivalence class-based dependency calculation approach for fast feature selection using
 * rough set theory</a>. In the paper, it has been re-named as Nested Equivalence Class(NEC).
 * <p>
 * This is a {@link DefaultProcedureContainer}. Procedure contains 8 {@link ProcedureComponent}s,
 * refer to steps:
 * <ul>
 * 	<li>
 * 		<strong>Get the Equivalence Class</strong>:
 * 		<p>Obtain Equivalence Classes induced by C: U/C.
 * 	</li>
 * 	<li>
 * 		<strong>Initiate</strong>:
 * 		<p>Calculate the global Significance (<i>sig(U/C)</i>).
 * 	</li>
 * 	<li>
 * 		<strong>Obtain Core</strong>:
 * 		<p>Calculate Core.
 * 		<p><code>ClassicCoreProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>After Core / Init reduct list</strong>:
 * 		<p>Calculate the significance of current reduct(i.e. Core). Or, initiate
 * 			an empty reduct list whose sig(reduct)==0.
 * 		<p><code>SigLoopPreprocessProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Sig loop</strong>:
 * 		<p>Loop and search for the most significant attribute and add as an attribute
 * 			of the reduct until reaching exit condition.
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
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_QR_EXEC_CORE}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_REDUCT_LIST}</li>
 * </ul>
 * 
 * @see RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.SimpleCounting.RealTimeCounting
 * @see ClassicCoreProcedureContainer
 * @see SigLoopPreprocessProcedureContainer
 * @see SignificantAttributeSeekingLoopProcedureContainer
 * @see ClassicReductInspectionProcedureContainer
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class RECBasedAlgorithmSimpleRealtimeCountingHeuristicQRTester<Sig extends Number>
	extends SelectiveComponentsProcedureContainer<Collection<Integer>>
	implements TimeSum,
				StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>,
				HashSearchStrategy,
				SimpleCountingRECBasedStrategy,
				QuickReductHeuristicReductStrategy
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	private String[] componentExecOrder;
	
	public RECBasedAlgorithmSimpleRealtimeCountingHeuristicQRTester(
			ProcedureParameters paramaters, boolean logOn
	) {
		super(logOn, paramaters);
		statistics = new Statistics();
		report = new HashMap<>();
	}

	@Override
	public String shortName() {
		String corer;
		Collection<ProcedureContainer<?>> subCons =
				this.getComponentMap()
						.get("Obtain Core")
						.getSubProcedureContainers()
						.values();
		if (subCons!=null)	corer = subCons.iterator().next().shortName();
		else				corer = "UNKNOWN";

		String inspector;
		if (this.getComponentMap().containsKey("Reduct Inspection"))
			subCons = this.getComponentMap()
						.get("Reduct Inspection")
					.getSubProcedureContainers()
					.values();
		if (subCons!=null)	inspector = subCons.iterator().next().shortName();
		else				inspector = "UNKNOWN";
		
		return String.format("QR-REC(Ext.S)(%s)(%s)(%s)(%s)",
				ProcedureUtils.ShortName.calculation(getParameters()),
				ProcedureUtils.ShortName.byCore(getParameters()),
				corer,
				inspector
		);
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
			// 1. Obtain the Equivalence Class.
			new TimeCountedProcedureComponent<Collection<EquivalenceClass>>(
					ComponentTags.TAG_COMPACT,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("1. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES)
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
						return RoughEquivalenceClassBasedExtensionAlgorithm
								.Basic
								.equivalenceClass(
										instances,
										new IntegerArrayIterator(attributes)
								);
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("equClasses", result);
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
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Obtain the Equivalence Class"),
			// 2. Initiate.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("2. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get("equClasses"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>)
								parameters[p++];
						Class<RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig>> calculationClass =
								(Class<RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig>>)
								parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation =
								calculationClass.newInstance();
						Sig globalSig = calculation.calculate(equClasses, new IntegerArrayIterator(attributes), instances.size())
													.getResult();
						return new Object[] {
								globalSig, 
								calculation
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Sig globalSig = (Sig) result[r++];
						RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation =
								(RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig>)
								result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("globalSig", globalSig);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "global Sig = {}"),
									globalSig
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
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
						if (logOn){
							log.info("3. "+component.getDescription());
						}
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
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result);
						/* ------------------------------------------------------------------------------ */
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
					new ClassicCoreProcedureContainer<>(getParameters(), logOn)
				),
			// 4. After Core / Init reduct list.
			new ProcedureComponent<Sig>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("4. "+component.getDescription());
						}
					},
					(component, parameters) -> {
						return (Sig)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						// Report
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								equClasses.size(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
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
					new SigLoopPreprocessProcedureContainer<>(getParameters(), logOn)
				).setDescription("After Core / Init reduct list"),
			// 5. Sig loop.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					getParameters(), 
					(component)->{
						if (logOn){
							log.info("5. "+component.getDescription());
						}
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
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "|Reduct Candidate| = {}"),
									result.size()
							);
						}
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								result
						);
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
					new SignificantAttributeSeekingLoopProcedureContainer<>(getParameters(), logOn)
				).setDescription("Sig loop"),
			// 6. Reduct Inspection.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					getParameters(), 
					(component)->{
						if (logOn){
							log.info("6. "+component.getDescription());
						}
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
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "|Reduct Finally| = {}"),
									result.size()
							);
						}
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT,
								result
						);
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
					new ClassicReductInspectionProcedureContainer<>(getParameters(), logOn)
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