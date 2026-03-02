package de.svenjerie.eva.service;

import de.svenjerie.eva.domain.Deal;

public interface RiskRule {
    boolean applies(Deal deal);
    String getRiskLevel();
}