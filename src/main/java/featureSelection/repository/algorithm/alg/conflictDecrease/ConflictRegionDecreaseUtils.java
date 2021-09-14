package featureSelection.repository.algorithm.alg.conflictDecrease;

import featureSelection.basic.model.universe.instance.Instance;
import lombok.experimental.UtilityClass;

import java.util.Iterator;
import java.util.Set;

@UtilityClass
public class ConflictRegionDecreaseUtils {

    public static <Ins extends Instance> int instanceSize(Set<Set<Ins>> equClasses) {
        if (equClasses.size()==0)	return 0;
        int counter = 0;	for (Set<Ins> e : equClasses)	counter += e.size();
        return counter;
    }

    public static <Ins extends Instance> boolean decisionsEqual(Set<Ins> instances) {
        if (instances.size()<=1)	return true;
        int dec;
        Ins pointer;
        Iterator<Ins> iterator = instances.iterator();
        dec = iterator.next().getAttributeValue(0);
        while (iterator.hasNext()) {
            pointer = iterator.next();
            if (pointer.getAttributeValue(0)!=dec){
                return false;
            }
        }
        return true;
    }
}
