package featureSelection.repository.algorithm.alg.xu;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.model.universe.instance.Instance;
import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class XuUtils {

    /**
     * A filter for extracting legal attributes and skip illegal ones(value=-1).
     *
     * @param ins
     * 		An {@link Instance}.
     * @param attribute
     * 		Attributes to be examined. An int array.(Starts from 1) /
     * 		<code>null</code> to use all legal ones.
     * @return an int array of the legal attributes./<code>null</code> if no attribute is legal.
     */
    public static int[] filterAttributes(Instance ins, int[] attribute) {
        if (attribute==null) {
            attribute = new int[ins.getValueLength()-1];
            for (int i=0; i<attribute.length; i++){
                attribute[i]=i+1;
            }
            return attribute;
        }else if (attribute.length==0){
            return attribute;
        }else {
            List<Integer> legal = new LinkedList<>();
            for (int attr : attribute){
                if (ins.getAttributeValue(attr)!=-1){
                    legal.add(attr);
                }
            }
            return legal.size()==0 ? null : ArrayCollectionUtils.getIntArrayByCollection(legal);
        }
    }

    /**
     * A filter for extracting legal attributes and skip illegal ones(value=-1).
     *
     * @param ins
     * 		An {@link Instance}.
     * @param attributes
     * 		Attributes to be examined. An int array.(Starts from 1) /
     * 		<code>null</code> to use all legal ones.
     * @return an int array of the legal attributes./<code>null</code> if no attribute is legal.
     */
    public static List<Integer> filterAttributes(Instance ins, List<Integer> attributes) {
        if (attributes==null) {
            attributes = new ArrayList<>(ins.getValueLength()-1);
            for (int i=0; i<attributes.size(); i++){
                attributes.add(i+1);
            }
            return attributes;
        }else if (attributes.size()==0){
            return attributes;
        }else {
            int attr;
            Iterator<Integer> iterator = attributes.iterator();
            while (iterator.hasNext()) {
                attr = iterator.next();
                if (ins.getAttributeValue(attr)==-1) {
                    iterator.remove();
                }
            }
            return attributes.size()==0 ? null : attributes;
        }
    }

    /**
     * Remove items from the {@link Collection} in <code>sets</code>>.
     *
     * @param instances
     * 		{@link Instance}s.
     * @param removes
     * 		Elements to remove
     * @return An item-removed set.
     */
    @SafeVarargs
    public static Collection<Instance> removeItemsFromUniverses(
            Collection<Instance> instances, Collection<Instance>...removes
    ) {
        boolean empty = Arrays.stream(removes).allMatch(Collection::isEmpty);
        if (empty){
            return instances;
        }

        Instance uPointer;
        Iterator<Instance> iterator = instances.iterator();
        while (iterator.hasNext()) {
            uPointer = iterator.next();
            for (Collection<Instance> set : removes) {
                if (set.contains(uPointer)) {
                    iterator.remove();
                    break;
                }
            }
        }
        return instances;
    }
}
