import net.adamsanchez.seriousvote.Data.Database;
import net.adamsanchez.seriousvote.Data.PlayerRecord;
import net.adamsanchez.seriousvote.utils.CC;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static sun.misc.Version.print;

/**
 * Created by Adam Sanchez on 4/4/2018.
 */
public class Test {
    List<Integer> milestoneValues = new LinkedList<>();
    List<Integer> recurringMilestoneValues = new LinkedList<>();

    public static void main(String[] args) {
        //System.out.println(CC.logo());
        Test t = new Test();
        t.milestoneValues.add(5);
        t.milestoneValues.add(51);
        t.milestoneValues.add(17);
        t.milestoneValues.add(13);
        t.milestoneValues.add(169);

        t.recurringMilestoneValues.add(25);
        t.recurringMilestoneValues.add(150);

        Collections.sort(t.recurringMilestoneValues);
        Collections.sort(t.milestoneValues);

        System.out.println(t.getRemainingMilestoneVotes(169));

    }

    public int getRemainingMilestoneVotes(int currentVotes) {
        List<Integer> mathList = new LinkedList<>();
        for (Integer ix : recurringMilestoneValues) {
            // Quotient plus 1 times the divisor to get the next least multiple
            mathList.add(((int) Math.floor((currentVotes / ix)) + 1) * ix);
        }
        mathList.addAll(milestoneValues);
        Collections.sort(mathList);
        for (Integer ix : mathList) {
            if (currentVotes < ix) {
                return ix - currentVotes;
            }
        }
        return -1;
    }


}
