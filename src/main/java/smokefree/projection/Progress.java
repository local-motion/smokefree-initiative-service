package smokefree.projection;

import lombok.Getter;
import lombok.NoArgsConstructor;
import smokefree.domain.Status;

import static smokefree.domain.Status.finished;
import static smokefree.domain.Status.in_progress;

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

    void increment(Status status) {
        if (status == finished) {
            smokeFree.inc();
        } else if (status == in_progress) {
            workingOnIt.inc();
            remaining++;
        } else {
            smoking.inc();
            remaining++;
        }
        recalculatePercentages();
    }

    void change(Status before, Status after) {
        if (before == finished) {
            smokeFree.dec();
        } else if (before == in_progress) {
            workingOnIt.dec();
            remaining--;
        } else {
            smoking.dec();
            remaining--;
        }
        increment(after);
    }

    @Getter
    class Stat {
        int count = 0;
        int percentage = 0;

        void inc() {
            total++;
            count++;
        }

        void dec() {
            total--;
            count--;
        }

        void recalculatePercentage() {
            final double fraction = (double) count / total;
            percentage = (int)(fraction * 100);
        }
    }
}
