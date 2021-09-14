package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalPartition;

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
import featureSelection.basic.procedure.container.SelectiveComponentsProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.roughEquivalenceClassBased.extension.IncrementalPartitionRECBasedStrategy;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.basic.support.shrink.ShrinkInstance;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedAlgorithm;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.incrementalPartition.RoughEquivalenceClassDummy;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalPartition.Shrink4RECBoundaryClassSetStays;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalPartition.procedure.SigLoopPreprocessProcedureContainer;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalPartition.procedure.SignificantAttributeSeekingLoopProcedureContainer;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalPartition.procedure.core.CoreProcedureContainer4IPREC;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalPartition.procedure.inspect.InspectionProcedureContainer4IPREC;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalPartition.procedure.inspect.RecursionBasedInspectionProcedureContainer4IPREC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Quick Reduct - Rough Equivalent Class based extension: Incremental
 * Partition</strong> Feature Selection.
 * <p>
 * This is a {@link SelectiveComponentsProcedureContainer}. Procedure contains 8 {@link ProcedureComponent}s,
 * refer to steps:
 * <ul>
 * 	<li>
 * 		<strong>Get the Equivalence Classes</strong>:
 * 		<p>Obtain Equivalence Classes induced by C: U/C.
 * 	</li>
 * 	<li>
 * 		<strong>Get Core</strong>:
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
 * @see RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition
 * @see CoreProcedureContainer4IPREC
 * @see SigLoopPreprocessProcedureContainer
 * @see SignificantAttributeSeekingLoopProcedureContainer
 * @see RecursionBasedInspectionProcedureContainer4IPREC
 * @see InspectionProcedureContainer4IPREC
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class RoughEquivalentClassBasedAlgorithmIncrementalPartitionHeuristicQRTester
	extends SelectiveComponentsProcedureContainer<Collection<Integer>>
	implements TimeSum,
				StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>,
				HashSearchStrategy,
				IncrementalPartitionRECBasedStrategy,
				QuickReductHeuristicReductStrategy
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	private String[] componentExecOrder;
	
	public RoughEquivalentClassBasedAlgorithmIncrementalPartitionHeuristicQRTester(ProcedureParameters parameters, boolean logOn) {
		super(logOn, parameters);
		statistics = new Statistics();
		report = new HashMap<>();
	}

	@Override
	public String shortName() {
		String corer;
		Collection<ProcedureContainer<?>> subCons = this.getComponentMap().get("Get Core").getSubProcedureContainers().values();
		if (subCons!=null)	corer = subCons.iterator().next().shortName();
		else				corer = "UNKNOWN";

		String inspector;
		if (this.getComponentMap().containsKey("Reduct Inspectation"))
			subCons = this.getComponentMap().get("Reduct Inspectation").getSubProcedureContainers().values();
		if (subCons!=null)	inspector = subCons.iterator().next().shortName();
		else				inspector = "UNKNOWN";
		
		return "QR-REC(Ext.IP)"+
				"("+ ProcedureUtils.ShortName.calculation(getParameters())+")"+
				"("+ProcedureUtils.ShortName.byCore(getParameters())+")"+
				"("+corer+")"+
				"("+inspector+")";
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
			// 1. Get the Equivalence Classes.
			new TimeCountedProcedureComponent<Object[]>(
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
						Collection<Instance> instances = (Collection<Instance>) parameters[0];
						int[] attributes = (int[]) parameters[1];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Collection<EquivalenceClass> equClasses =
								RoughEquivalenceClassBasedAlgorithm
									.Basic
									.equivalenceClass(instances, new IntegerArrayIterator(attributes));
						return new Object[] {
								equClasses,
								new Shrink4RECBoundaryClassSetStays()
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<EquivalenceClass> equClasses = (Collection<EquivalenceClass>) result[r++];
						ShrinkInstance<?,?,?> shrinkInstance = (ShrinkInstance<?, ?, ?>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("equClasses", equClasses);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SHRINK_INSTANCE_INSTANCE, shrinkInstance);
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
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								equClasses.size(), 
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
				}.setDescription("Get the Equivalence Classes"),
			// 2. Get Core.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
						component.setLocalParameters(new Object[] {
							getParameters().get(ParameterConstants.PARAMETER_QR_EXEC_CORE),
						});
					},
					(component, parameters) -> {
						return parameters[0]!=null && ((Boolean) parameters[0])?
								(Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec():
								new HashSet<>();
					}, 
					(component, core) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, core);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>) getParameters().get("equClasses")).size(), 
								core.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Get Core")
				.setSubProcedureContainer(
					"CoreProcedureContainer", 
					new CoreProcedureContainer4IPREC(getParameters(), logOn)
				),
			// 3. After Core / Init reduct list.
			new ProcedureComponent<Collection<RoughEquivalenceClassDummy>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
					},
					(component, parameters) -> {
						return (Collection<RoughEquivalenceClassDummy>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
										.exec();
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot("roughClasses", result);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						Collection<RoughEquivalenceClassDummy> roughClasses =
								getParameters().get("roughClasses");
						int instanceSize = 0, equClassSize = 0;
						for (RoughEquivalenceClassDummy roughClass: roughClasses) {
							equClassSize+=roughClass.getItemSize();
							instanceSize+=roughClass.getInstanceSize();
						}
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								instanceSize,
								equClassSize, 
								result.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
							
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setSubProcedureContainer(
					"SigLoopPreprocessProcedureContainer", 
					new SigLoopPreprocessProcedureContainer(getParameters(), logOn)
				).setDescription("After Core / Init reduct list"),
			// 4. Sig loop.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					getParameters(), 
					(component)->{
						if (logOn)	log.info("4. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								component.getSubProcedureContainers().values().iterator().next()
						});
					}, 
					(component, parameters)->{
						return (Collection<Integer>) ((ProcedureContainer<?>) parameters[0]).exec();
					}, 
					(component, result)->{
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result);
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(2, "|Reduct Candidate| = {}"), result.size());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT, result);
						/* ------------------------------------------------------------------------------ */
						// Report
						Collection<RoughEquivalenceClassDummy> roughClasses = getParameters().get("roughClasses");
						int instanceSize = 0, equClassSize = 0;
						for (RoughEquivalenceClassDummy roughClass: roughClasses) {
							equClassSize+=roughClass.getItemSize();
							instanceSize+=roughClass.getInstanceSize();
						}
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								instanceSize,
								equClassSize, 
								result.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
						
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+".";
					}
				}.setSubProcedureContainer(
					"SignificantAttributeSeekingLoopProcedureContainer", 
					new SignificantAttributeSeekingLoopProcedureContainer(getParameters(), logOn)
				).setDescription("Sig loop"),
			// 5. Reduct Inspection.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
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
						/* ------------------------------------------------------------------------------ */
						// Statistics
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, result);
						if (logOn)	log.info(LoggerUtil.spaceFormat(2, "|Reduct Finally| = {}"), result.size());
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT, result);
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
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
						
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+".";
					}
				}.setSubProcedureContainer(
					"ReductInspectionProcedureContainer", 
					//new RecursionBasedInspectionProcedureContainer4IPREC(getParameters(), logOn)
					new InspectionProcedureContainer4IPREC(getParameters(), logOn)
				).setDescription("Reduct Inspection"),//*/
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