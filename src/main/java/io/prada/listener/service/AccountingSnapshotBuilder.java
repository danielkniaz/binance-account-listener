package io.prada.listener.service;

import static java.math.BigDecimal.ZERO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.prada.listener.dto.accounting.AccountingConsolidatedSymbolInfo;
import io.prada.listener.dto.accounting.AccountingOrder;
import io.prada.listener.dto.accounting.AccountingPosition;
import io.prada.listener.dto.accounting.AccountingSnapshot;
import io.prada.listener.dto.accounting.AccountingSnapshot.Balances;
import io.prada.listener.service.request.RequestType;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountingSnapshotBuilder {
    private final ObjectMapper mapper;
    private final MathContext ctx;

    public AccountingSnapshot build(List<Pair<RequestType, ObjectNode>> pairs) {
        return new AccountingSnapshot()
            .setBalances(retrieveBalances(pairs))
            .setSymbolSnapShots(retrieveSymbolInfo(pairs));
    }

    public AccountingSnapshot copy(AccountingSnapshot from) {
        Map<String, AccountingConsolidatedSymbolInfo> symbolMap = new HashMap<>();
        for (Map.Entry<String,AccountingConsolidatedSymbolInfo> entry : from.getSymbolSnapShots().entrySet()) {
            symbolMap.put(entry.getKey(), new AccountingConsolidatedSymbolInfo(entry.getValue()));
        }
        Balances fromBalance = from.getBalances();
        return new AccountingSnapshot()
            .setBalances(new Balances(fromBalance.balance().add(ZERO), fromBalance.equity().add(ZERO), fromBalance.freeMargin().add(ZERO)))
            .setSymbolSnapShots(symbolMap);
    }

    private Balances retrieveBalances(List<Pair<RequestType, ObjectNode>> pairs) {
        JsonNode node = pairs.stream()
            .filter(pair -> RequestType.BALANCE == pair.getFirst())
            .map(Pair::getSecond)
            .map(elem -> elem.get(RequestType.BALANCE.name()))
            .findAny().orElseThrow();
        return new Balances(from(node, "totalWalletBalance"), from(node, "totalMarginBalance"), from(node, "maxWithdrawAmount"));
    }

    private BigDecimal from(JsonNode node, String value) {
        return new BigDecimal(node.get(value).asText(), ctx);
    }

    private Map<String, AccountingConsolidatedSymbolInfo> retrieveSymbolInfo(
        List<Pair<RequestType, ObjectNode>> pairs) {
        Map<String, AccountingConsolidatedSymbolInfo> result = new HashMap<>();
        Optional<JsonNode> positionsFromBalance = pairs.stream().filter(pair -> RequestType.BALANCE == pair.getFirst())
            .map(Pair::getSecond)
            .map(elem -> elem.get(RequestType.BALANCE.name()))
            .map(node -> node.get("positions")).findAny();
        retrievePositions(positionsFromBalance, result);
        Optional<JsonNode> orders = pairs.stream().filter(pair -> RequestType.ORDER == pair.getFirst())
            .map(Pair::getSecond).map(node -> node.get(RequestType.ORDER.name())).findAny();
        retrieveOrders(orders, result);
        return result;
    }

    @SneakyThrows
    private void retrievePositions(Optional<JsonNode> positionsFromBalance, Map<String, AccountingConsolidatedSymbolInfo> result) {
        if (positionsFromBalance.isEmpty()) {
            throw new IllegalArgumentException("balance -> must exist");
        }
        if (!positionsFromBalance.get().isArray()) {
            throw new IllegalArgumentException("balance -> positions must be array!");
        }
        for (JsonNode node : positionsFromBalance.get()) {
            String symbol = node.get("symbol").asText();
            result.computeIfAbsent(symbol, s -> new AccountingConsolidatedSymbolInfo().setSymbol(s))
                .setPosition(mapper.treeToValue(node, AccountingPosition.class));
        }
    }

    @SneakyThrows
    private void retrieveOrders(Optional<JsonNode> orders, Map<String, AccountingConsolidatedSymbolInfo> result) {
        if (orders.isEmpty()) {
            throw new IllegalArgumentException("orders must exist");
        }
        if (!orders.get().isArray()) {
            throw new IllegalArgumentException("orders must be array!");
        }
        for (JsonNode node : orders.get()) {
            String symbol = node.get("symbol").asText();
            result.computeIfAbsent(symbol, s -> new AccountingConsolidatedSymbolInfo().setSymbol(s))
                .getOrders().add(mapper.treeToValue(node, AccountingOrder.class));
        }
    }
}
