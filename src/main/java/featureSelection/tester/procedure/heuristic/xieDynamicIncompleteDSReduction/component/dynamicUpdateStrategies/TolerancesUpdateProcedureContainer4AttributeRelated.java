package featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component.dynamicUpdateStrategies;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.algorithm.alg.xieDynamicIncompleteDSReduction.DynamicIncompleteDecisionSystemReductionAlgorithm;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.DynamicUpdateInfoPack;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.DynamicUpdateResult;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.PreviousInfoPack;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.attributeRelatedUpdate.AlteredAttributeValues;
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
 * For <strong>attribute-related update strategy</strong>
 * <p>
 * The main idea for <strong>attribute-related update strategy</strong> in the original paper could be 
 * summarised as the following:
 * <ul>
 * 	<li>Check if reduct(<strong>B</strong>) is affected by altered attributes, update tolerance classes
 * 	    if only it does;</li>
 * 	<li>Extract affected altered attributes in reduct(<strong>B</strong>) as the new 
 * 		<strong>C<sub>ALT</sub></strong>;</li>
 * 	<li>Obtain tolerance classes by considering un-altered attributes in reduct(<strong>B</strong>)
 * 	    only; (By doing so, tolerance classes is bigger than that when also considering altered
 * 	    attributes)</li>
 * 	<li>Update tolerance classes when considering altered attributes in reduct(<strong>B</strong>) 
 * 		additionally by <strong>removing</strong> tolerance relations(e.g. in the form of (x[m], x[n])) 
 * 		that don't tolerant with each other when considering altered attributes.</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
public class TolerancesUpdateProcedureContainer4AttributeRelated<Sig extends Number>
	extends AbstractTolerancesUpdateProcedureContainer
{
	public TolerancesUpdateProcedureContainer4AttributeRelated(ProcedureParameters parameters, boolean logOn) {
		super(parameters, logOn);
	}

	@Override
	public String shortName() {
		return "DIDS(attribute-related)";
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
						localParameters.put("selectedUnalteredInPreviousToleranceAttributes", updateInfo.getAlteredAttributeValues().getSelectedUnalteredAttributes());
						localParameters.put("alteredAttributeValues", updateInfo.getAlteredAttributeValues());
						updateResultOfReduct = (DynamicUpdateResult<Sig>) comp1.exec();
						TimerUtils.timeContinue((TimeCounted) component);
						
						Sig redSig = updateResultOfReduct.getSignificance();
						
						// sig(C)
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("report target sig attributes", "C");
						localParameters.put("originalInstances", previousInfos.getInstances());
						localParameters.put("previousTolerancesOfCondAttrs", previousInfos.getTolerancesOfCondAttrs());
						localParameters.put("previousTolerancesOfCondAttrsNDecAttr", previousInfos.getTolerancesOfCondAttrsNDecAttrs());
						localParameters.put("previousToleranceCondAttrs", Arrays.stream(attributes).boxed().collect(Collectors.toList()));
						localParameters.put("selectedUnalteredInPreviousToleranceAttributes", updateInfo.getAlteredAttributeValues().getSelectedUnalteredAttributes());
						localParameters.put("alteredAttributeValues", updateInfo.getAlteredAttributeValues());
						updateResultOfAttributes = (DynamicUpdateResult<Sig>) comp1.exec();
						TimerUtils.timeContinue((TimeCounted) component);
						
						Sig globalSig = updateResultOfAttributes.getSignificance();
						
						return new Object[] {
								redSig, globalSig
						};
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
								localParameters.get("selectedUnalteredInPreviousToleranceAttributes"),
								localParameters.get("alteredAttributeValues"),
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
						Collection<Integer> selectedUnalteredInPreviousToleranceAttributes = 
								(Collection<Integer>) parameters[p++];
						AlteredAttributeValues alteredAttributeValues =
								(AlteredAttributeValues) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<Map<Instance, Collection<Instance>>> comp2 =
								(ProcedureComponent<Map<Instance, Collection<Instance>>>)
								getComponents().get(2);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Obtain updated Tolerance classes: T<sub>B</sub>
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("report mark tolerance with dec", false);
						localParameters.put("originalInstances", originalInstances);
						localParameters.put("previousTolerances", previousTolerancesOfCondAttrs);
						localParameters.put("previousToleranceAttributes", previousToleranceCondAttrs);
						localParameters.put("selectedUnalteredInPreviousToleranceAttributes", selectedUnalteredInPreviousToleranceAttributes);
						localParameters.put("alteredAttributeValues", alteredAttributeValues);
						Map<Instance, Collection<Instance>> latestTolerancesOfReduct =
								comp2.exec();
						TimerUtils.timeContinue((TimeCounted) component);
								
						// Obtain updated Tolerance classes with decision attribute: T<sub>B∪D</sub>
						Collection<Integer> previousToleranceCondAttrsNDecAttr = 
								this.generateWithDecisionAttribute(previousToleranceCondAttrs);

						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("report mark tolerance with dec", true);
						localParameters.put("originalInstances", originalInstances);
						localParameters.put("previousTolerances", previousTolerancesOfCondAttrsNDecAttr);
						localParameters.put("previousToleranceAttributes", previousToleranceCondAttrsNDecAttr);
						localParameters.put("selectedUnalteredInPreviousToleranceAttributes", selectedUnalteredInPreviousToleranceAttributes);
						localParameters.put("alteredAttributeValues", alteredAttributeValues);
						Map<Instance, Collection<Instance>> latestTolerancesWithDecAttr =
							comp2.exec();
						TimerUtils.timeContinue((TimeCounted) component);
						
						// Calculate feature significance.
						calculation.calculate(latestTolerancesOfReduct, latestTolerancesWithDecAttr);
						Sig sig = calculation.getResult();
						
						return new DynamicUpdateResult<>(sig, latestTolerancesOfReduct, latestTolerancesWithDecAttr);
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
								getParameters().get("updateInfo"),
								getParameters().get("toleranceClassObtainer"),
								localParameters.get("previousTolerances"),
								localParameters.get("previousToleranceAttributes"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						DynamicUpdateInfoPack updateInfo =
								(DynamicUpdateInfoPack) parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer =
								(ToleranceClassObtainer) parameters[p++];
						Map<Instance, Collection<Instance>> previousTolerances =
								(Map<Instance, Collection<Instance>>) parameters[p++];
						Collection<Integer> previousToleranceAttributes = 
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Map<Instance, Collection<Instance>> tolerances =
							DynamicIncompleteDecisionSystemReductionAlgorithm
								.Dynamic
								.updateToleranceClass4AttributeRelatedUpdate(
										updateInfo,
										previousToleranceAttributes, 
										previousTolerances,
										false,
										toleranceClassObtainer
								);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timePause((TimeCounted) component);
						tolerances = DynamicIncompleteDecisionSystemReductionAlgorithm
										.Dynamic
										.Common
										.TransformPrevious2Latest.exec(tolerances, updateInfo);
						TimerUtils.timeContinue((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return tolerances;
					}, 
					(component, tolerances) -> {
						/* ------------------------------------------------------------------------------ */
						// Validations
						/*DynamicUpdateInfoPack updateInfo = getParameters().get("updateInfo");
						
						Collection<Instance> up2DateInstances = updateInfo.getLatestInstances();
						
						Collection<Integer> condAttributes = 
								(Collection<Integer>) 
								localParameters.get("previousToleranceAttributes");
						
						ToleranceClassObtainer toleranceClassObtainer = 
								getParameters().get("toleranceClassObtainer");

						PreviousInfoPack previousInfos = getParameters().get("previousInfos");
						
						validateToleranceClassUpdate(
								tolerances, previousInfos.getInstances(), up2DateInstances, 
								new IntegerCollectionIterator(condAttributes),
								toleranceClassObtainer
						);
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