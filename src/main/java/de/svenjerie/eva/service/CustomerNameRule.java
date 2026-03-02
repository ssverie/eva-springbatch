package de.svenjerie.eva.service;

import de.svenjerie.eva.domain.Deal;
import org.springframework.stereotype.Component;

@Component
public class CustomerNameRule implements ValidationRule {

    @Override
    public boolean isValid(Deal deal) {
        return deal.getCustomerName() != null && !deal.getCustomerName().isBlank();
    }

    @Override
    public String getErrorMessage() {
        return "Customer name is missing";
    }
}