package com.ebtedge.service.flow.opa;

import com.ebtedge.service.flow.core.ResponseWrapper;
import com.ebtedge.service.flow.domain.CardholderSearchCriteria;
import com.ebtedge.service.flow.domain.CardholderSearchResult;

public interface OpaService {

    ResponseWrapper<CardholderSearchResult> cardholderSearch(CardholderSearchCriteria searchCriteria);
}
