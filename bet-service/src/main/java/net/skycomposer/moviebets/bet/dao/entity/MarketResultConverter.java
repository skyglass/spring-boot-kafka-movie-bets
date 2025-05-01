package net.skycomposer.moviebets.bet.dao.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.skycomposer.moviebets.common.dto.market.MarketResult;

@Converter(autoApply = false)
public class MarketResultConverter implements AttributeConverter<MarketResult, Integer> {

    @Override
    public Integer convertToDatabaseColumn(MarketResult result) {
        return result != null ? result.getValue() : null;
    }

    @Override
    public MarketResult convertToEntityAttribute(Integer dbValue) {
        return dbValue != null ? MarketResult.fromValue(dbValue) : null;
    }
}
