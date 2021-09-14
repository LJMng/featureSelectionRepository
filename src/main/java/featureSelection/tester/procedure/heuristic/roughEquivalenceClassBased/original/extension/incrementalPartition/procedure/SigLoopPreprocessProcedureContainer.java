package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalPartition.procedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
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
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.Shrink4RECBoundaryClassSetStays;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Most significant attribute searching pre-processing for <strong>Quick Reduct - Rough Equivalent
 * Class based extension: Incremental Partition</strong> Feature Selection. This procedure
 * contains 2 {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Seek significant attributes pre-process procedure controller</strong>
 * 		<p>If reduct is empty, sig(reduct)=0, else reduct is initiated using core
 * 			which is not empty. sig(reduct) is calculated using the 2nd
 * 			{@link ProcedureComponent}.
 * 	</li>
 * 	<li>
 * 		<strong>Obtain Rough Equivalence Classes using core</strong>
 * 		<p>Obtain the rough equivalence classes induced by core.
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SHRINK_INSTANCE_INSTANCE}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_REDUCT_LIST}</li>
 * 	<li>equClasses</li>
 * </ul>
 *
 * @author Benjamin_L
 */
@Slf4j
public class SigLoopPreprocessProcedureContainer
	extends DefaultProcedureContainer<Collection<RoughEquivalenceClassDummy>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	public SigLoopPreprocessProcedureContainer(ProcedureParameters parameters, boolean logOn) {
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
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// *. Seek significant attributes pre-process procedure controller.
			new ProcedureComponent<Collection<RoughEquivalenceClassDummy>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "*. "+component.getDescription()));
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						/* ------------------------------------------------------------------------------ */
						Collection<Integer> reduct =
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						ProcedureComponent<Collection<RoughEquivalenceClassDummy>> component1 =
								(ProcedureComponent<Collection<RoughEquivalenceClassDummy>>) getComponents().get(1);
						/* ------------------------------------------------------------------------------ */
						return reduct.isEmpty()?
								RoughEquivalenceClassBasedExtensionAlgorithm
									.IncrementalPartition
									.Basic
									.wrapEquivalenceClasses(equClasses):
								component1.exec();
					}, 
					(component, roughClasses) -> {
						this.getParameters().setNonRoot("roughClasses", roughClasses);
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | *. "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Seek significant attributes pre-process procedure controller"),
			// 1. Obtain Rough Equivalence Class using core.
			new TimeCountedProcedureComponent<Collection<RoughEquivalenceClassDummy>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get("equClasses"),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_SHRINK_INSTANCE_INSTANCE),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						Shrink4RECBoundaryClassSetStays shrinkInstance =
								(Shrink4RECBoundaryClassSetStays)
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Collection<RoughEquivalenceClassDummy> roughClasses =
								RoughEquivalenceClassBasedExtensionAlgorithm
									.IncrementalPartition
									.Basic
									.calculateEquivalenceClassPositiveRegionAfterPartition(
										equClasses, 
										new IntegerCollectionIterator(red)
									).getRecord();
						shrinkInstance.shrink(roughClasses);
						return roughClasses;
					}, 
					(component, roughClasses) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot("roughClasses", roughClasses);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						reportKeys.add(component.getDescription());
						/* ------------------------------------------------------------------------------ */
						// Report
						int currentEquClassSize=0, currentUniverseSize=0;	
						for (RoughEquivalenceClassDummy roughClass: roughClasses) {
							currentEquClassSize+=roughClass.getItemSize();
							currentUniverseSize+=roughClass.getInstanceSize();
						}
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								currentUniverseSize, 
								currentEquClassSize, 
								((Collection<Integer>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
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
				}.setDescription("Obtain Rough Equivalence Classes using core"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<RoughEquivalenceClassDummy> exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray){
			this.getComponents().add(each);
		}
		return (Collection<RoughEquivalenceClassDummy>) componentArray[0].exec();
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}
