package com.ebtedge.service.flow.cardsummary;

import com.ebtedge.service.flow.domain.common.CardInfoResult;
import com.ebtedge.service.flow.domain.common.ClientInfoResult;
import com.ebtedge.service.flow.domain.common.BaseResponseMetadata;

import java.util.List;

public record CardholderSummaryResult(BaseResponseMetadata baseResponse,
                                      String clientId, String cardNumber,
                                      List<CardInfoResult> cards, List<ClientInfoResult> clients) {
}
