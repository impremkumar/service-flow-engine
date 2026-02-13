package com.ebtedge.service.flow.cardsummary;

import com.ebtedge.service.flow.core.ResponseWrapper;

public interface CardService {

    ResponseWrapper<CardholderSummaryResult> getCardholderSummary(CardholderSummaryContext cardholderSummaryContext);

}
