package com.github.standobyte.jojo.action;

import javax.annotation.Nullable;

import net.minecraft.util.text.ITextComponent;

public class ActionConditionResult {
    private final boolean positive;
    private final boolean stopHeldAction;
    private final ITextComponent warning;
    
    public static final ActionConditionResult POSITIVE = new ActionConditionResult(true, false, null);
    public static final ActionConditionResult NEGATIVE = new ActionConditionResult(false, true, null);
    public static final ActionConditionResult NEGATIVE_CONTINUE_HOLD = new ActionConditionResult(false, false, null);
    
    public static ActionConditionResult createNegative(ITextComponent warning) {
        return new ActionConditionResult(false, true, warning);
    }
    
    public static ActionConditionResult createNegativeContinueHold(ITextComponent warning) {
        return new ActionConditionResult(false, false, warning);
    }
    
    private ActionConditionResult(boolean positive, boolean stopHeldAction, ITextComponent warning) {
        this.positive = positive;
        this.stopHeldAction = stopHeldAction;
        this.warning = warning;
    }
    
    public boolean isPositive() {
        return positive;
    }
    
    public boolean shouldStopHeldAction() {
        return !isPositive() && stopHeldAction;
    }
    
    @Nullable
    public ITextComponent getWarning() {
        return warning;
    }
}
