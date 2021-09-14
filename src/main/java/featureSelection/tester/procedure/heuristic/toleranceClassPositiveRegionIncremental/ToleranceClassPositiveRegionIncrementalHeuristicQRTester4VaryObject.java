package featureSelection.tester.procedure.heuristic.toleranceClassPositiveRegionIncremental;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
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
import featureSelection.basic.support.alg.ToleranceClassPositiveRegionStrategy;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.repository.algorithm.alg.toleranceClassPositiveRegionIncremental.ToleranceClassPositiveRegionAlgorithm;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.CombTClassesResult4VariantObjs;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesGroup4Empty;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.support.calculation.alg.FeatureImportance4ToleranceClassPositiveRegionIncremental;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.toleranceClassPositiveRegionIncremental.component.ReductInspectionProcedureContainer;
import featureSelection.tester.procedure.heuristic.toleranceClassPositiveRegionIncremental.component.ReductUpdateProcedureContainer4VaryObject;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Quick Reduct - Tolerance Class Positive Region Incremental
 * reduction</strong> for dynamic data multiple object varying feature values(<strong>FSMV</strong>)
 * processing.
 * <p>
 * This is a {@link SelectiveComponentsProcedureContainer}. Procedure contains 6
 * {@link ProcedureComponent}s, refer to steps:
 * <ul>
 * 	<li><strong>Initialise</strong>: 
 * 		<p>Initialise B=Red<sub>U</sub>; U' = U - U<sub>x</sub> ∪ U<sub>x'</sub> (i.e. U' is the up-to-date
 * 			{@link Instance} set)
 * 	</li>
 * 	<li><strong>Extract invariant instances</strong>: 
 * 		<p>Obtain <i>invariant</i> {@link Instance}s from <i>U'</i>.
 * 	</li>
 * 	<li><strong>Cache complete instances</strong>: 
 * 		<p>Use {@link ToleranceClassObtainer#getCacheInstanceGroups(Collection)} to obtain instance groups
 * 			for global usages in obtaining Tolerance classes.
 * 		<p>Cache <code>in-variant instances</code>, <code>(latest) variant instances</code>, 
 * 			<code>all (up-to-date) instances</code>.
 * 	</li>
 * 	<li><strong>Calculate tolerance classes of up-to-date instances</strong>: 
 * 		<p>Calculate Tolerance Classes of the variant {@link Instance}s (i.e. U<sub>x'</sub>/TR(B),
 * 			and U<sub>x'</sub>/TR(C)).
 * 		<p>Calculate Tolerance Classes of the up-to-date {@link Instance}s (i.e. U'/TR(B), and
 * 			U'/TR(C)) by combining tolerance classes of U'-U<sub>x'</sub> and U<sub>x'</sub>
 * 	</li>
 * 	<li><strong>Update reduct if needed</strong>: 
 * 		<p>Check if any update in U'/TR(B) after U<sub>x</sub> ==> U<sub>x'</sub> and 
 * 			POS<sub>B</sub><sup>U<sub>x'</sub></sup>(D) == POS<sub>C</sub><sup>U<sub>x'</sub></sup>(D).
 * 		<p>Update if any update in U'/TR(B) or the above POSs aren't equal.
 * 		<p>Otherwise, ok to return the previous reduct as final reduct.
 * 		<p><code>ReductUpdateProcedureContainer4VaryObject</code>
 * 	</li>
 * 	<li><strong>Inspect if updated</strong>: 
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
 * 	<li>{@link ParameterConstants#PARAMETER_PREVIOUS_REDUCT}</li>
 * 	<li>toleranceClassObtainerClass</li>
 * 	<li>changedFromInstances</li>
 * 	<li>changedToInstances</li>
 * </ul>
 * 
 * @see ReductUpdateProcedureContainer4VaryObject
 * @see ReductInspectionProcedureContainer
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class ToleranceClassPositiveRegionIncrementalHeuristicQRTester4VaryObject
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
	
	public ToleranceClassPositiveRegionIncrementalHeuristicQRTester4VaryObject(ProcedureParameters parameters, boolean logOn) {
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
		
		return "QR-TCPR-FSMV (multi object vary feature values)"+
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
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get("changedFromInstances"),
								getParameters().get("changedToInstances"),
								getParameters().get("toleranceClassObtainerClass"),
							});
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Class<? extends Calculation<?>> calculationClass =
								(Class<? extends Calculation<?>>) parameters[p++];
						Collection<Instance> Instances =
								(Collection<Instance>) parameters[p++];
						Collection<Instance> changedFromInstances =
								(Collection<Instance>) parameters[p++];
						Collection<Instance> changedToInstances =
								(Collection<Instance>) parameters[p++];
						Class<? extends ToleranceClassObtainer> toleranceClassObtainerClass = 
								(Class<? extends ToleranceClassObtainer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						// Initialise B=Red<sub>U</sub>; U' = U - U<sub>x</sub> ∪ U<sub>x'</sub>
						Collection<Instance> newInstances = new HashSet<>(Instances);
						newInstances.removeAll(changedFromInstances);
						newInstances.addAll(changedToInstances);
						
						return new Object[] {
							calculationClass.newInstance(), 
							toleranceClassObtainerClass.newInstance(),
							newInstances
						};
					}, 
					(component, results) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Calculation<?> calculation = (Calculation<?>) results[r++];
						ToleranceClassObtainer toleranceClassObtainer = (ToleranceClassObtainer) results[r++];
						Collection<Instance> newInstances = (Collection<Instance>) results[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
						getParameters().setNonRoot("toleranceClassObtainer", toleranceClassObtainer);
						getParameters().setNonRoot("newUniverseInstances", newInstances);
						getParameters().setNonRoot("newReduct", getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT));
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> Instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder().loadCurrentInfo(Instances)
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
						/* ------------------------------------------------------------------------------ */
						warnReportMemory();
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initialise"),
			// 2. Extract invariant instances
			new TimeCountedProcedureComponent<Collection<Instance>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("newUniverseInstances"),
								getParameters().get("changedToInstances"),
							});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> newInstances = (Collection<Instance>) parameters[p++];
						Collection<Instance> changedToInstances = (Collection<Instance>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Collection<Instance> invariants = new HashSet<>(newInstances);
						invariants.removeAll(changedToInstances);
						
						return invariants;
					}, 
					(component, invariants) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("invariants", invariants);
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
						warnReportMemory();
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Extract invariant instances"),
			// 3. Cache complete instances.
			new TimeCountedProcedureComponent<InstancesCollector[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("invariants"),
								getParameters().get("changedToInstances"),
								getParameters().get("newUniverseInstances"),
								getParameters().get("toleranceClassObtainer"),
							});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> invariants =
								(Collection<Instance>) parameters[p++];
						Collection<Instance> changedToInstances =
								(Collection<Instance>) parameters[p++];
						Collection<Instance> newInstances =
								(Collection<Instance>) parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer =
								(ToleranceClassObtainer) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						InstancesCollector completeData4AttributesOfInvariances = 
								invariants.isEmpty()?
									new InstancesGroup4Empty():
									toleranceClassObtainer.getCacheInstanceGroups(invariants);
						InstancesCollector completeData4AttributesOfChangedToInstances = 
								changedToInstances.isEmpty()?
									new InstancesGroup4Empty():
									toleranceClassObtainer.getCacheInstanceGroups(changedToInstances);
						InstancesCollector completeData4AttributesOfNewInstances =
								toleranceClassObtainer.getCacheInstanceGroups(newInstances);
						
						return new InstancesCollector[] {
								completeData4AttributesOfInvariances,
								completeData4AttributesOfChangedToInstances,
								completeData4AttributesOfNewInstances
						};
					}, 
					(component, completeDataCollectors) -> {
						/* ------------------------------------------------------------------------------ */
						InstancesCollector completeData4AttributesOfInvariances = 
								completeDataCollectors[0];
						InstancesCollector completeData4AttributesOfChangedToInstances = 
								completeDataCollectors[1];
						InstancesCollector completeData4AttributesOfNewInstances =
								completeDataCollectors[2];
						/* ------------------------------------------------------------------------------ */
						if (completeData4AttributesOfInvariances!=null)
							getParameters().setNonRoot("completeData4AttributesOfInvariances", completeData4AttributesOfInvariances);
						if (completeData4AttributesOfChangedToInstances!=null)
							getParameters().setNonRoot("completeData4AttributesOfChangedToInstances", completeData4AttributesOfChangedToInstances);
						if (completeData4AttributesOfNewInstances!=null)
							getParameters().setNonRoot("completeData4AttributesOfNewInstances", completeData4AttributesOfNewInstances);
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
						/* ------------------------------------------------------------------------------ */
						warnReportMemory();
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Cache complete instances"),
			// 4. Calculate tolerance classes of up-to-date instances.
			new TimeCountedProcedureComponent<CombTClassesResult4VariantObjs[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("4. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("invariants"),
								getParameters().get("changedToInstances"),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
								getParameters().get("toleranceClassObtainer"),
								getParameters().get("completeData4AttributesOfInvariances"),
								getParameters().get("completeData4AttributesOfChangedToInstances"),
							});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> invariants = (Collection<Instance>) parameters[p++];
						Collection<Instance> changedToInstances = (Collection<Instance>) parameters[p++];
						int[] attributes = (int[]) parameters[p++];
						Collection<Integer> previousReduct = (Collection<Integer>) parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer = 
								(ToleranceClassObtainer) parameters[p++];
						InstancesCollector completeData4AttributesOfInvariances = 
								(InstancesCollector) parameters[p++];
						InstancesCollector completeData4AttributesOfChangedToInstances = 
								(InstancesCollector) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Calculate U<sub>x'</sub>/TR(B), and U<sub>x'</sub>/TR(C)
						// Calculate U'/TR(B), and U'/TR(C) by combining tolerance classes of 
						//	U'-U<sub>x'</sub> and U<sub>x'</sub>
						CombTClassesResult4VariantObjs combinedTolerancesByAttributes = 
							ToleranceClassPositiveRegionAlgorithm
								.VariantObjects
								.toleranceClasses(
									invariants, changedToInstances, 
									new IntegerArrayIterator(attributes),
									toleranceClassObtainer,
									completeData4AttributesOfInvariances,
									completeData4AttributesOfChangedToInstances
								);
						CombTClassesResult4VariantObjs combinedTolerancesByPreviousReduct = 
							ToleranceClassPositiveRegionAlgorithm
								.VariantObjects
								.toleranceClasses(
									invariants, changedToInstances, 
									new IntegerCollectionIterator(previousReduct),
									toleranceClassObtainer,
									completeData4AttributesOfInvariances,
									completeData4AttributesOfChangedToInstances
								);
						
						return new CombTClassesResult4VariantObjs[] {
								combinedTolerancesByAttributes,
								combinedTolerancesByPreviousReduct
						};
					}, 
					(component, results) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						CombTClassesResult4VariantObjs combinedTolerancesByAttributes = results[r++];
						CombTClassesResult4VariantObjs combinedTolerancesByPreviousReduct = results[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("combinedTolerancesByAttributes", combinedTolerancesByAttributes);
						/* ------------------------------------------------------------------------------ */
						int[] attributes = getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES);
						Collection<Instance> newInstances = getParameters().get("newUniverseInstances");
						ToleranceClassObtainer toleranceClassObtainer = getParameters().get("toleranceClassObtainer");
						InstancesCollector completeData4AttributesOfNewInstances =
								getParameters().get("completeData4AttributesOfNewInstances");
						
						verifyToleranceClasses(
								newInstances, new IntegerArrayIterator(attributes), 
								combinedTolerancesByAttributes, 
								toleranceClassObtainer, completeData4AttributesOfNewInstances
						);
						Collection<Integer> previousReduct = getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT);
						verifyToleranceClasses(
								newInstances, new IntegerCollectionIterator(previousReduct), 
								combinedTolerancesByPreviousReduct, 
								toleranceClassObtainer, completeData4AttributesOfNewInstances
						);
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("combinedTolerancesByAttributes", combinedTolerancesByAttributes);
						getParameters().setNonRoot("combinedTolerancesByPreviousReduct", combinedTolerancesByPreviousReduct);
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
						warnReportMemory();
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Calculate tolerance classes of up-to-date instances"),
			// 5. Update reduct if needed
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("5. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("combinedTolerancesByAttributes"),
								getParameters().get("combinedTolerancesByPreviousReduct"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
							});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						CombTClassesResult4VariantObjs combinedTolerancesByAttributes = 
								(CombTClassesResult4VariantObjs) parameters[p++];
						CombTClassesResult4VariantObjs combinedTolerancesByPreviousReduct = 
								(CombTClassesResult4VariantObjs) parameters[p++];
						FeatureImportance4ToleranceClassPositiveRegionIncremental calculation = 
								(FeatureImportance4ToleranceClassPositiveRegionIncremental)
								parameters[p++];
						Collection<Integer> previousReduct = (Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureContainer<?> subContainer =
								component.getSubProcedureContainers()
										.values()
										.iterator()
										.next();
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Integer posOfChangedToInsByReduct = null, posOfChangedToInsByAttributes = null;
						
						boolean toleranceChanged = 
							combinedTolerancesByAttributes.anyUpdate() ||
							combinedTolerancesByPreviousReduct.anyUpdate();
						if (!toleranceChanged) {
							// POS<sub>B</sub><sup>U<sub>x'</sub></sup>(D)
							posOfChangedToInsByReduct = 
									calculation.calculate(combinedTolerancesByPreviousReduct.getTolerancesOfVariances().entrySet())
												.getResult();
							// POS<sub>C</sub><sup>U<sub>x'</sub></sup>(D)
							posOfChangedToInsByAttributes = 
									calculation.calculate(combinedTolerancesByAttributes.getTolerancesOfVariances().entrySet())
												.getResult();
							if (posOfChangedToInsByReduct==posOfChangedToInsByAttributes) {
								return new Object[] {
										false, previousReduct,
										posOfChangedToInsByReduct,
										posOfChangedToInsByAttributes
								};
							}
						}
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timePause((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							if (posOfChangedToInsByReduct!=null && posOfChangedToInsByAttributes!=null) {
								log.info(LoggerUtil.spaceFormat(1, "Pos(Red)={}"), posOfChangedToInsByReduct);
								log.info(LoggerUtil.spaceFormat(1, "Pos( C )={}"), posOfChangedToInsByAttributes);
							}
						}
						/* ------------------------------------------------------------------------------ */
						Collection<Integer> newReduct = (Collection<Integer>) subContainer.exec();
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeContinue((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return new Object[] {
								true, newReduct, 
								posOfChangedToInsByReduct,
								posOfChangedToInsByAttributes
						};
					}, 
					(component, results) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						boolean updated = (boolean) results[r++];
						Collection<Integer> reduct = (Collection<Integer>) results[r++]; 
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("updated", updated);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, reduct);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								new ArrayList<>(reduct)
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
						/* ------------------------------------------------------------------------------ */
						warnReportMemory();
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update reduct if needed")
				.setSubProcedureContainer(
					"ReductUpdateProcedureContainer4VaryObject", 
					new ReductUpdateProcedureContainer4VaryObject<>(getParameters(), logOn)
				),
			// 6. Inspect if updated
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("6. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get("updated"),
								getParameters().get("completeData4AttributesOfNewInstances"),
							});
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> reduct = (Collection<Integer>) parameters[p++];
						boolean updated = (boolean) parameters[p++];
						InstancesCollector completeData4AttributesOfNewInstances = (InstancesCollector) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						Collection<Instance> newInstances = getParameters().get("newUniverseInstances");
						getParameters().setNonRoot(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, newInstances);
						
						if (completeData4AttributesOfNewInstances!=null) {
							getParameters().setNonRoot("completeData4Attributes", completeData4AttributesOfNewInstances);
						}
						
						return updated? 
								(Collection<Integer>) component.getSubProcedureContainers().values().iterator().next().exec():
								reduct;
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "|Reduct Finally| = {}"), red.size());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_AFTER_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT,
								red
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
						/* ------------------------------------------------------------------------------ */
						warnReportMemory();
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 6. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspect if updated")
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

	private void verifyToleranceClasses(
			Collection<Instance> newInstances, IntegerIterator attributes,
			CombTClassesResult4VariantObjs combinedToleranceClasses,
			ToleranceClassObtainer toleranceClassObtainer,
			InstancesCollector completeData4AttributesOfNewInstances
	) {
		Map<Instance, Collection<Instance>> correctToleranceClasses =
			toleranceClassObtainer.obtain(
				newInstances, newInstances, attributes,
				completeData4AttributesOfNewInstances
			);

		Map<Instance, Collection<Instance>> vertifiedToleranceClassess =
			combinedToleranceClasses.getTolerancesOfCombined();
		
		
		boolean error = false;
		for (Instance ins: newInstances) {
			Collection<Instance> correct = correctToleranceClasses.get(ins);
			Collection<Instance> vertify = vertifiedToleranceClassess.get(ins);
			
			if (correct.size()!=vertify.size()) {
				error = true;
			}else if (!vertify.containsAll(correct)) {
				error = true;
			}
			
			if (error) {
				System.out.println("Attributes: "+attributes);
				System.out.println("correct:");
				correct.forEach(each->System.out.println("	"+each));				
				System.out.println("vertify:");
				vertify.forEach(each->System.out.println("	"+each));			
				
				throw new IllegalStateException("In-correct tolerance classes.");
			}
		}
	}

	private void warnReportMemory() {
//		if (logOn) {
//			log.warn(LoggerUtil.spaceFormat(1, "Memory: used={}, max={}, committed={}."), 
//					NumberUtils.ComputerSizes.fromByte(
//						JavaVirtualMachineUtils.Memory.usedInByte()
//					),
//					NumberUtils.ComputerSizes.fromByte(
//						JavaVirtualMachineUtils.Memory.maxInByte()
//					),
//					NumberUtils.ComputerSizes.fromByte(
//						JavaVirtualMachineUtils.Memory.committedInByte()
//					)
//			);
//		}
	}
}