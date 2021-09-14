package featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component.dynamicUpdateStrategies;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.DynamicUpdateInfoPack;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.DynamicUpdateResult;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.PreviousInfoPack;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.objectRelatedUpdate.AlteredInstanceItem;
import featureSelection.repository.support.calculation.alg.xieDynamicIncrementalDSReduction.FeatureImportance4XieDynamicIncompleteDSReduction;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Update tolerance classes and feature importance <code>sig(C)</code> & <code>sig(red)</code> for 
 * <strong>Xie Dynamic In-complete Decision System Reduction(DIDS)</strong> Feature Selection.
 * <p>
 * For <strong>object-related update strategy</strong>
 * <p>
 * The main idea for <strong>object-related update strategy</strong> in the original paper could be 
 * summarised as the following:
 * <ul>
 * 	<li>For <i>un-altered {@link Instance}s</i>, remove all altered {@link Instance}s from
 * 		the tolerance classes;</li>
 * 	<li>For <i>altered {@link Instance}s</i>, initialise their tolerance classes with themselves;</li>
 * 	<li>For all {@link Instance}, add into each other's tolerance classes if 2
 * 		{@link Instance} are tolerant with each other. <i>Since altered/un-altered</i> ones have
 * 		been initialised earlier, they don't need to check {@link Instance}s that are both within
 * 		themselves.</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
public class TolerancesUpdateProcedureContainer4ObjectRelated<Sig extends Number>
	extends AbstractTolerancesUpdateProcedureContainer
{
	public TolerancesUpdateProcedureContainer4ObjectRelated(ProcedureParameters parameters, boolean logOn) {
		super(parameters, logOn);
	}

	@Override
	public String shortName() {
		return "DIDS(object-related)";
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Controller
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get("updateInfo"),
								getParameters().get("previousInfos"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						int[] attributes =
								(int[]) parameters[p++];
						DynamicUpdateInfoPack updateInfo =
								(DynamicUpdateInfoPack) parameters[p++];
						PreviousInfoPack previousInfos =
								(PreviousInfoPack) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> comp1 = this.getComponents().get(1);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						DynamicUpdateResult<Sig> updateResultOfReduct, updateResultOfAttributes;

						// sig(red)
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("report target sig attributes", "red");
						localParameters.put("originalInstances", previousInfos.getInstances());
						localParameters.put("previousTolerancesOfCondAttrs", previousInfos.getTolerancesOfReduct());
						localParameters.put("previousTolerancesOfCondAttrsNDecAttr", previousInfos.getTolerancesOfReductNDecAttrs());
						localParameters.put("previousToleranceCondAttrs", previousInfos.getReduct());
						localParameters.put("alterInstanceItems", updateInfo.getAlteredObjects());
						updateResultOfReduct = (DynamicUpdateResult<Sig>) comp1.exec();
						TimerUtils.timeContinue((TimeCounted) component);
						
						Sig redSig = updateResultOfReduct.getSignificance();
						
						// sig(C)
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("report target sig attributes", "C");
						localParameters.put("previousTolerancesOfCondAttrs", previousInfos.getTolerancesOfCondAttrs());
						localParameters.put("previousTolerancesOfCondAttrsNDecAttr", previousInfos.getTolerancesOfCondAttrsNDecAttrs());
						localParameters.put("previousToleranceCondAttrs", Arrays.stream(attributes).boxed().collect(Collectors.toList()));
						localParameters.put("alterInstanceItems", updateInfo.getAlteredObjects());
						updateResultOfAttributes = (DynamicUpdateResult<Sig>) comp1.exec();
						TimerUtils.timeContinue((TimeCounted) component);
						
						Sig globalSig = updateResultOfAttributes.getSignificance();
						
						return new Object[] {redSig, globalSig};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Sig redSig = (Sig) result[r++];
						Sig globalSig = (Sig) result [r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("redSig", redSig);
						getParameters().setNonRoot("globalSig", globalSig);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						//	[DatasetRealTimeInfo]
						Collection<Integer> reduct = getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								reduct.size()
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
				}.setDescription("Core procedure controller"),
			// 2. Update tolerance class and significance
			new TimeCountedProcedureComponent<DynamicUpdateResult<Sig>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								localParameters.get("originalInstances"),
								localParameters.get("previousTolerancesOfCondAttrs"),
								localParameters.get("previousTolerancesOfCondAttrsNDecAttr"),
								localParameters.get("previousToleranceCondAttrs"),
								localParameters.get("alterInstanceItems"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation =
								(FeatureImportance4XieDynamicIncompleteDSReduction<Sig>) 
								parameters[p++];
						Collection<Instance> originalInstances =
								(Collection<Instance>) parameters[p++];
						Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrs =
								(Map<Instance, Collection<Instance>>) parameters[p++];
						Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrsNDecAttr =
								(Map<Instance, Collection<Instance>>) parameters[p++];
						Collection<Integer> previousToleranceCondAttrs = 
								(Collection<Integer>) parameters[p++];
						Map<Instance, AlteredInstanceItem> alterInstanceItems =
								(Map<Instance, AlteredInstanceItem>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<Map<Instance, Collection<Instance>>> comp2 =
								(ProcedureComponent<Map<Instance, Collection<Instance>>>)
								this.getComponents().get(2);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Obtain updated Tolerance classes: T<sub>B</sub>
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("report mark tolerance with dec", false);
						localParameters.put("originalInstances", originalInstances);
						localParameters.put("previousTolerances", previousTolerancesOfCondAttrs);
						localParameters.put("previousToleranceAttributes", new IntegerCollectionIterator(previousToleranceCondAttrs));
						localParameters.put("alterInstanceItems", alterInstanceItems);
						Map<Instance, Collection<Instance>> latestTolerancesOfCondAttrs = comp2.exec();
						TimerUtils.timeContinue((TimeCounted) component);
								
						// Obtain updated Tolerance classes with decision attribute: T<sub>B∪D</sub>
						Collection<Integer> previousToleranceCondAttrsNDecAttr = 
								generateWithDecisionAttribute(previousToleranceCondAttrs);

						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("report mark tolerance with dec", false);
						localParameters.put("originalInstances", originalInstances);
						localParameters.put("previousTolerances", previousTolerancesOfCondAttrsNDecAttr);
						localParameters.put("previousToleranceAttributes", new IntegerCollectionIterator(previousToleranceCondAttrsNDecAttr));
						localParameters.put("alterInstanceItems", alterInstanceItems);
						Map<Instance, Collection<Instance>> latestTolerancesWithDecAttr = comp2.exec();
						TimerUtils.timeContinue((TimeCounted) component);
						
						// Calculate feature significance.
						calculation.calculate(latestTolerancesOfCondAttrs, latestTolerancesWithDecAttr);
						Sig sig = calculation.getResult();
						
						return new DynamicUpdateResult<>(sig, latestTolerancesOfCondAttrs, latestTolerancesWithDecAttr);
					}, 
					(component, updateResult) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						reportKeys.add(reportMark);
						//	[DatasetRealTimeInfo]
						Collection<Integer> reduct = getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								reduct.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report,
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update tolerance class and significance"),
			// 3. Update tolerance class
			new TimeCountedProcedureComponent<Map<Instance, Collection<Instance>>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								localParameters.get("originalInstances"),
								localParameters.get("previousTolerances"),
								localParameters.get("previousToleranceAttributes"),
								localParameters.get("alterInstanceItems"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation = 
								(FeatureImportance4XieDynamicIncompleteDSReduction<Sig>) parameters[p++];
						Collection<Instance> originalInstances =
								(Collection<Instance>) parameters[p++];
						Map<Instance, Collection<Instance>> previousTolerances =
								(Map<Instance, Collection<Instance>>) parameters[p++];
						IntegerIterator previousToleranceAttributes =
								(IntegerIterator) parameters[p++];
						Map<Instance, AlteredInstanceItem> alterInstanceItems =
								(Map<Instance, AlteredInstanceItem>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return calculation.updateToleranceClass4ObjectRelatedUpdate(
									originalInstances,
									previousTolerances, 
									previousToleranceAttributes, 
									alterInstanceItems
								);
					}, 
					(component, tolerances) -> {
						/* ------------------------------------------------------------------------------ */
						// Validations
						/*Collection<Instance> up2DateInstances =
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						
						IntegerIterator previousToleranceAttributes = 
								(IntegerIterator) 
								localParameters.get("previousToleranceAttributes");
						
						ToleranceClassObtainer toleranceClassObtainer = 
								getParameters().get("toleranceClassObtainer");
						
						PreviousInfoPack previousInfos = getParameters().get("previousInfos");
						
						validateToleranceClassUpdate(
								tolerances, previousInfos.getInstances(), up2DateInstances, 
								previousToleranceAttributes,
								toleranceClassObtainer
						);//*/
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						boolean reportMarkToleranceWithDec = (boolean) localParameters.get("report mark tolerance with dec");
						String reportMark = reportMark(reportMarkToleranceWithDec);
						reportKeys.add(reportMark);
						//	[DatasetRealTimeInfo]
						Collection<Integer> reduct = getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								reduct.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report,
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update tolerance class"),
		};	
	}
}