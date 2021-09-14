package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalPartition.procedure.inspect;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.incrementalPartition.RoughEquivalenceClassDummy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.core.attributeCombination.CapacityCalculator;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reduct inspection for <strong>Quick Reduct - Rough Equivalent Class based extension: Incremental
 * Partition</strong> Feature Selection.
 * <p>
 * This procedure contains 3 {@link ProcedureComponent}s:
 * <ul>
 *  <li>
 *      <strong>Initiate parameters</strong>
 *      <p>Initiate parameters for IP-REC core. Using {@link AttrProcessStrategyParams} to initiate
 *          {@link AttrProcessStrategy4Comb} for simple attribute grouping.
 *  </li>
 * 	<li>
 * 		<strong>Inspection controller</strong>
 * 		<p>Use {@link RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition.Inspection#
 * 		    compute(AttrProcessStrategy4Comb, Collection)} directly to inspect reduct.
 * 	</li>
 * </ul>
 *
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition.Inspection#compute(
 *      AttrProcessStrategy4Comb, Collection)
 * @see RecursionBasedInspectionProcedureContainer4IPREC
 *
 * @author Benjamin_L
 */
@Slf4j
public class InspectionProcedureContainer4IPREC
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	
	public InspectionProcedureContainer4IPREC(ProcedureParameters parameters, boolean logOn) {
		super(logOn? null: log, parameters);
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Inspect(IP-REC)";
	}

	@Override
	public String staticsName() {
		return shortName();
	}

	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Initiate parameters.
			new TimeCountedProcedureComponent<AttrProcessStrategy4Comb>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get("inspectAttributeProcessCapacityCalculator"),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
						});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						CapacityCalculator inspectAttributeProcessCapacityCalculator =
								(CapacityCalculator) parameters[p++];
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						AttrProcessStrategyParams inspectAttributeProcessStrategyParams = 
							new AttrProcessStrategyParams()
								.set(AttrProcessStrategy4Comb.PARAMETER_EXAM_CAPACITY_CALCULATOR, 
										inspectAttributeProcessCapacityCalculator);
						return new AttrProcessStrategy4Comb(inspectAttributeProcessStrategyParams)
									.initiate(new IntegerCollectionIterator(reduct));
					}, 
					(component, inspectAttributeProcessStrategy) -> {
						/* ------------------------------------------------------------------------------ */
						localParameters.put("inspectAttributeProcessStrategy", inspectAttributeProcessStrategy);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = component.getDescription();
						reportKeys.add(reportMark);
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>) getParameters().get("equClasses")).size(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
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
				}.setDescription("Initiate parameters"),
			// 2. Inspection controller.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component) -> {
						component.setLocalParameters(new Object[] {
								localParameters.get("inspectAttributeProcessStrategy"),
								getParameters().get("equClasses"),
						});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p = 0;
						AttrProcessStrategy4Comb inspectAttributeProcessStrategy = 
								(AttrProcessStrategy4Comb) 
								parameters[p++];
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return RoughEquivalenceClassBasedExtensionAlgorithm
								.IncrementalPartition
								.Inspection
								.compute( 
									inspectAttributeProcessStrategy, 
									equClasses
								);
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, red);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						//	[DatasetRealTimeInfo]
						int instanceSize = 0, equClassSize = 0;
						Collection<RoughEquivalenceClassDummy> roughClasses = getParameters().get("roughClasses");
						for (RoughEquivalenceClassDummy roughClass: roughClasses) {
							instanceSize+= roughClass.getInstanceSize();
							equClassSize+= roughClass.getItemSize();
						}
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								instanceSize,
								equClassSize, 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
														
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate controller"),
		};
	}

	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] components = initComponents();
		for (ProcedureComponent<?> each : components)	this.getComponents().add(each);
		for (int i=0; i<2; i++)							components[i].exec();
		return getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION);
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

}