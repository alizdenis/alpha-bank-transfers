package com.bank.alpha.processor.async;

import com.bank.alpha.model.Account;
import com.bank.alpha.model.Transfer;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.util.List;
import java.util.function.Function;

@Getter
@Builder(toBuilder = true)
@ToString
class MultiStepTx {

    @Getter
    @Builder
    static class Step {
        private final Integer id;
        private final Function<Account, Account> action;
    }

    private final Transfer parent;

    @Singular
    private final List<Step> steps;

    private final Step onFail;

    private final boolean failed;

    private final int currentStepIdx;

    Step getCurrentStep() {
        if (failed) {
            return onFail;
        }
        return steps.get(currentStepIdx);
    }

    boolean hasNextStep() {
        return steps.size() != currentStepIdx + 1;
    }

    MultiStepTx stepComplete() {
        return this.toBuilder()
            .currentStepIdx(currentStepIdx + 1)
            .build();
    }

    MultiStepTx toFailed() {
        return this.toBuilder()
            .failed(true)
            .build();
    }

}
