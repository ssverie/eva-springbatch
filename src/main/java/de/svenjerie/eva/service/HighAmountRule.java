package de.svenjerie.eva.service;

import de.svenjerie.eva.domain.Deal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountRule implements RiskRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("10000");

    @Override
    public boolean applies(Deal deal) {
        return deal.getAmount().compareTo(THRESHOLD) > 0;
    }

    @Override
    public String getRiskLevel() {
        return "HIGH";
    }
}