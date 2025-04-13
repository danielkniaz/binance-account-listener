package io.prada.listener.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BigDecimalMathContextDeserializer extends JsonDeserializer<BigDecimal> {
    private final MathContext ctx = new MathContext(8, RoundingMode.HALF_EVEN);

    @Override
    public BigDecimal deserialize(JsonParser parser, DeserializationContext _i) throws IOException {
        return new BigDecimal(parser.getText(), ctx);
    }
}
