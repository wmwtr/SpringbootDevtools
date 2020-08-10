package com.github.wmwtr.springbootdevtools.AutoConfigure;

import com.github.wmwtr.springbootdevtools.Restart.Restarter;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

class OnInitializedRestarterCondition extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		ConditionMessage.Builder message = ConditionMessage.forCondition("Initialized Restarter Condition");
		Restarter restarter = getRestarter();
		if (restarter == null) {
			return ConditionOutcome.noMatch(message.because("unavailable"));
		}
		if (restarter.getBaseUrls() == null) {
			return ConditionOutcome.noMatch(message.because("initialized without URLs"));
		}
		return ConditionOutcome.match(message.because("available and initialized"));
	}

	private Restarter getRestarter() {
		try {
			return Restarter.getInstance();
		}
		catch (Exception ex) {
			return null;
		}
	}

}

