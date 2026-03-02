package de.svenjerie.eva.service;

import de.svenjerie.eva.domain.Deal;

public interface ValidationRule {
    boolean isValid(Deal deal);
    String getErrorMessage();
}