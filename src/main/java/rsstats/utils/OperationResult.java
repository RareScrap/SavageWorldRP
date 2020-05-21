package rsstats.utils;

import net.minecraft.util.StatCollector;

public class OperationResult {
    public final boolean isSuccess;
    public final String explanation;

    public OperationResult(boolean isSuccess, String explanation) {
        this.isSuccess = isSuccess;
        if (!StatCollector.canTranslate(explanation))
            throw new IllegalArgumentException("Explanation must be translatable. Cant find translation for " + explanation);
        this.explanation = explanation;
    }
}
