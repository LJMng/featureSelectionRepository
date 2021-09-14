package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.simpleCounting.realtime.procedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
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
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation;
import featureSelection.repository.support.calculation.positiveRegion.DefaultPositiveRegionCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Most significant attribute searching pre-processing for <strong>Quick Reduct - Rough Equivalence
 * Class based extension: Simple Counting(Real-time)</strong> Feature Selection. This procedure
 * contains 2 {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Seek significant attributes pre-process procedure controller</strong>
 * 		<p>If reduct is empty, sig(reduct)=0, else reduct is initiated using core
 * 			which is not empty. sig(reduct) is calculated using the 2nd
 * 			{@link ProcedureComponent}.
 * 	</li>
 * 	<li>
 * 		<strong>Calculate current reduct significance</strong>
 * 		<p>Calculate the significance of the current reduct which is not empty.
 * 	</li>
 * </ul>
 * <p>
 *
 * @param <Sig>
 *     Type of feature (subset) significance.
 *
 * @author Benjamin_L
 */
@Slf4j
public class SigLoopPreprocessProcedureContainer<Sig extends Number>
	extends DefaultProcedureContainer<Sig>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	public SigLoopPreprocessProcedureContainer(
			ProcedureParameters parameters, boolean logOn
	) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new ArrayList<>(1);
	}

	@Override
	public String staticsName() {
		return shortName();
	}

	@Override
	public String shortName() {
		return "Sig loop pre-process.";
	}

	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// *. Seek significant attributes pre-process procedure controller.
			new ProcedureComponent<Sig>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "*. {}"),
									component.getDescription()
							);
						}
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						Collection<Integer> red = (Collection<Integer>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						/* ------------------------------------------------------------------------------ */
						if (red.isEmpty()) {
							// The significance of the reduct is 0.
							FeatureImportance<Sig> calculation = getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE);
							if (calculation instanceof DefaultPositiveRegionCalculation) {
								return (Sig) new Integer(0);
							}else {
								return (Sig) new Double(0);
							}
						}else {
							// Calculate significance.
							return (Sig) ((ProcedureComponent<Integer>) getComponents().get(1)).exec();
						}
					}, 
					(component, subSig) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("subSig", subSig);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | *. "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Seek significant attributes pre-process procedure controller"),
			// 1. Calculate current reduct significance.
			new TimeCountedProcedureComponent<Sig>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "1. "),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get("equClasses"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation =
								(RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig>)
								parameters[p++];
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return calculation.calculate(equClasses, new IntegerCollectionIterator(reduct), instances.size())
											.getResult();
					}, 
					(component, redSig) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("redSig", redSig);
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "Red sig = {}"),
									redSig
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						reportKeys.add(component.getDescription());
						//	[STATISTIC_POS_HISTORY]
						List<Sig> posHistory =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (posHistory==null){
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY,
									posHistory=new LinkedList<>()
							);
						}
						posHistory.add(redSig);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<Instance>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(),
								((Collection<Instance>) getParameters().get("equClasses")).size(),
								((Collection<Integer>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						//	[REPORT_POSITIVE_INCREMENT_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, component.getDescription(),
								ReportConstants.Procedure.REPORT_SIG_HISTORY,
								redSig
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Calculate current reduct significance"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Sig exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray)		this.getComponents().add(each);
		return (Sig) componentArray[0].exec();
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}
