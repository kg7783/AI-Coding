package de.einmaleins.trainer;

import java.util.HashSet;
import java.util.Set;

public class EinmaleinsConfig {
    public Set<Integer> baseNumbers;
    public HashSet<Integer>[] multipliers;

    @SuppressWarnings("unchecked")
    public EinmaleinsConfig() {
        baseNumbers = new HashSet<>();
        multipliers = new HashSet[11];
        for (int i = 1; i <= 10; i++) {
            multipliers[i] = new HashSet<>();
        }
    }

    public EinmaleinsConfig copy() {
        EinmaleinsConfig copy = new EinmaleinsConfig();
        copy.baseNumbers.addAll(this.baseNumbers);
        for (int i = 1; i <= 10; i++) {
            copy.multipliers[i].addAll(this.multipliers[i]);
        }
        return copy;
    }
}
