package de.svenjerie.eva.service;

import de.svenjerie.eva.domain.Deal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PositiveAmountRule implements ValidationRule {

    @Override
    public boolean isValid(Deal deal) {
        return deal.getAmount() != null && deal.getAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String getErrorMessage() {
        return "Amount must be positive";
    }
}