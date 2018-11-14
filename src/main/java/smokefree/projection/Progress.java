package smokefree.projection;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Progress {
    int total;
    int remaining;
    Stat smokeFree = new Stat();
    Stat workingOnIt = new Stat();
    Stat smoking = new Stat();

    private void recalculatePercentages() {
        smokeFree.recalculatePercentage();
        workingOnIt.recalculatePercentage();
        smoking.recalculatePercentage();
    }

    private void incrementStat(Stat stat) {
        stat.inc();
        recalculatePercentages();
    }

    void incrementSmokeFree() {
        incrementStat(smokeFree);
    }

    void incrementWorkingOnIt() {
        incrementStat(workingOnIt);
        remaining++;
    }

    void incrementSmoking() {
        incrementStat(smoking);
        remaining++;
    }

    @Getter
    class Stat {
        int count = 0;
        int percentage = 0;

        void inc() {
            total++;
            count++;
        }

        void recalculatePercentage() {
            final double fraction = (double) count / total;
            percentage = (int)(fraction * 100);
        }
    }
}
