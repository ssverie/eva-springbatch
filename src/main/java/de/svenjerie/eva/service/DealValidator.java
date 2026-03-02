package de.svenjerie.eva.service;

import de.svenjerie.eva.domain.Deal;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DealValidator {

    private final List<ValidationRule> rules;

    public DealValidator(List<ValidationRule> rules) {
        this.rules = rules;
    }

    public Deal validate(Deal deal) {
        List<String> errors = rules.stream()
                .filter(rule -> !rule.isValid(deal))
                .map(ValidationRule::getErrorMessage)
                .collect(Collectors.toList());

        if (errors.isEmpty()) {
            deal.setStatus("VALID");
        } else {
            deal.setStatus("INVALID");
            deal.setErrorMessage(String.join("; ", errors));
        }
        return deal;
    }
}