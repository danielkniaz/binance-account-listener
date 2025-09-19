package io.prada.listener.processor;

import io.prada.listener.dto.Signal;
import io.prada.listener.dto.accounting.AccountingConsolidatedSymbolInfo;
import io.prada.listener.dto.accounting.AccountingOrder;
import io.prada.listener.dto.accounting.AccountingPosition;
import io.prada.listener.dto.accounting.AccountingSnapshot;
import io.prada.listener.dto.enums.Action;
import io.prada.listener.dto.enums.OrderType;
import io.prada.listener.service.RiskCalculationService;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountDiffProcessor {
    private final RiskCalculationService riskService;
    private final MathContext ctx;

    public List<Signal> diff(AccountingSnapshot now, AccountingSnapshot old) {
        List<Signal> pendingDiffs = findPendingDiffs(now, old);
        List<Signal> marketDiffs = findMarketDiffs(now, old);
        return Stream.of(pendingDiffs, marketDiffs).flatMap(Collection::stream).filter(Objects::nonNull).toList();
    }

    public List<Signal> suppress(List<Signal> signals) {
        Map<Boolean, List<Signal>> isMkt = signals.stream().collect(Collectors.partitioningBy(
            signal -> OrderType.MARKET == signal.getType()));
        Map<Boolean, List<Signal>> limits = isMkt.get(false).stream().collect(Collectors.partitioningBy(
            signal -> signal.getType() == OrderType.LIMIT));
        List<Signal> cleanSltp = suppressByMkt(isMkt.get(true).stream().filter(Signal::isOut).toList(), limits.get(false));
        List<Signal> cleanLmt = suppressByMkt(isMkt.get(true).stream().filter(Signal::isIn).toList(), limits.get(true));
        return Stream.of(isMkt.get(true), cleanLmt, cleanSltp).flatMap(Collection::stream).filter(Objects::nonNull).toList();
    }

    private List<Signal> findPendingDiffs(AccountingSnapshot now, AccountingSnapshot old) {
        List<Signal> result = new ArrayList<>();
        Set<String> symbols = mergeSymbols(now, old);
        for (String symbol : symbols) {
            var newSymbols = now.getSymbolSnapShots().getOrDefault(symbol, new AccountingConsolidatedSymbolInfo(symbol)).getOrders();
            var oldSymbols = old.getSymbolSnapShots().getOrDefault(symbol, new AccountingConsolidatedSymbolInfo(symbol)).getOrders();

            checkPending(
                newSymbols.stream().filter(AccountingOrder::isExit).filter(AccountingOrder::isStop).toList(),
                oldSymbols.stream().filter(AccountingOrder::isExit).filter(AccountingOrder::isStop).toList(),
                new Signal().setType(OrderType.STOP).setOut(true), now
            ).ifPresent(result::add);
            checkPending(
                newSymbols.stream().filter(AccountingOrder::isExit).filter(AccountingOrder::isLimit).toList(),
                oldSymbols.stream().filter(AccountingOrder::isExit).filter(AccountingOrder::isLimit).toList(),
                new Signal().setType(OrderType.LIMIT).setOut(true), now
            ).ifPresent(result::add);
            checkPending(
                newSymbols.stream().filter(AccountingOrder::isEntry).filter(AccountingOrder::isLimit).toList(),
                oldSymbols.stream().filter(AccountingOrder::isEntry).filter(AccountingOrder::isLimit).toList(),
                new Signal().setType(OrderType.LIMIT).setIn(true), now
            ).ifPresent(result::add);
            checkPending(
                newSymbols.stream().filter(AccountingOrder::isEntry).filter(AccountingOrder::isStop).toList(),
                oldSymbols.stream().filter(AccountingOrder::isEntry).filter(AccountingOrder::isStop).toList(),
                new Signal().setType(OrderType.STOP).setIn(true), now
            ).ifPresent(result::add);
        }
        return result;
    }

    private Optional<Signal> checkPending(List<AccountingOrder> now, List<AccountingOrder> old, Signal template, AccountingSnapshot snap) {
        Optional<Signal> signal = standardFlowPending(now, old, template);
        if (signal.isPresent()) {
            Stream.of(now, old).flatMap(Collection::stream).findFirst().ifPresent(order -> {
                signal.get().setSymbol(order.getSymbol())
                    .setPrice(pendingPrice(template, order))
                    .setDirection(pendingDirection(template, order))
                    .setType(template.getType())
                    .setIn(template.isIn())
                    .setOut(template.isOut());
                if (template.isIn()  && signal.get().getAction() == Action.NEW) {
                    signal.get().setRisk(riskService.computeEntryRisk(order, snap));
                }
            });
        }
        return signal;
    }

    private Optional<Signal> standardFlowPending(List<AccountingOrder> now, List<AccountingOrder> old, Signal signal) {
        if (now.isEmpty() && old.isEmpty()) {
            return Optional.empty();
        }
        validate(now, "new list of %s: in(%b)/out(%b)".formatted(signal.getType(), signal.isIn(), signal.isOut()));
        validate(old, "new list of %s: in(%b)/out(%b)".formatted(signal.getType(), signal.isIn(), signal.isOut()));
        if (now.size() > old.size()) {
            return Optional.of(signal.setAction(Action.NEW));
        } else if (now.size() < old.size()) {
            return Optional.of(signal.setAction(Action.KILL));
        } else if (pendingPrice(signal, now.get(0)).compareTo(pendingPrice(signal, old.get(0))) != 0) {
            return Optional.of(signal.setAction(Action.MOVE));
        }
        return Optional.empty();
    }

    private BigDecimal pendingPrice(Signal signal, AccountingOrder order) {
        if (signal.isIn() && signal.getType() == OrderType.LIMIT) {
            return order.getPrice();
        }
        return order.getStopPrice();
    }

    private int pendingDirection(Signal signal, AccountingOrder order) {
        return signal.isIn() ? order.direction() : 0;
    }

    private List<Signal> findMarketDiffs(AccountingSnapshot now, AccountingSnapshot old) {
        List<Signal> result = new ArrayList<>();
        Set<String> symbols = mergeSymbols(now, old);
        for (String symbol : symbols) {
            checkMarketPos(
                Optional.ofNullable(now.getSymbolSnapShots().get(symbol))
                    .map(AccountingConsolidatedSymbolInfo::getPosition).orElse(null),
                Optional.ofNullable(old.getSymbolSnapShots().get(symbol))
                    .map(AccountingConsolidatedSymbolInfo::getPosition).orElse(null)
                , now
            ).ifPresent(result::add);
        }
        return result;
    }

    private Optional<Signal> checkMarketPos(AccountingPosition now, AccountingPosition old, AccountingSnapshot snap) {
        if (old == null && now == null) {
            return Optional.empty();
        }
        AccountingPosition pos = Stream.of(now, old).filter(Objects::nonNull).findFirst().orElseThrow();
        Signal signal = new Signal().setType(OrderType.MARKET).setDirection(pos.direction()).setSymbol(pos.getSymbol());
        if (now == null) {
            return Optional.of(signal.setAction(Action.KILL).setOut(true));
        }
        if (old == null) {
            return Optional.of(signal.setAction(Action.NEW).setIn(true).setPrice(now.getEntryPrice())
                .setRisk(riskService.computeEntryRisk(now, snap)));
        }
        BigDecimal diffVolume = now.getPositionAmt().abs().subtract(old.getPositionAmt().abs(), ctx);
        switch (diffVolume.compareTo(BigDecimal.ZERO)) {
            default:
            case 0: return Optional.empty();
            case 1: {
                return Optional.of(signal.setAction(Action.VOLUME).setIn(true)
                    .setPrice(riskService.computeEntryAdd(now, old))
                    .setRisk(riskService.computeSizePercentAdd(now, old))
                );
            }
            case-1: {
                return Optional.of(signal.setAction(Action.VOLUME).setOut(true)
                    .setPrice(riskService.computeCurrentPrice(now)).setDirection(old.direction())
                    .setRisk(riskService.computeSizePartial(now, old))
                );
            }
        }
    }

    private List<Signal> suppressByMkt(List<Signal> market, List<Signal> pending) {
        List<String> existSymbols = market.stream().map(Signal::getSymbol).toList();
        return pending.stream()
            .filter(signal -> !existSymbols.contains(signal.getSymbol()))
            .toList();
    }

    private static Set<String> mergeSymbols(AccountingSnapshot one, AccountingSnapshot two) {
        return Stream.of(one.getSymbolSnapShots().keySet(), two.getSymbolSnapShots().keySet())
            .flatMap(Collection::stream).collect(Collectors.toSet());
    }

    private static void validate(List<?> list, String message) {
        if (list.size() > 1) {
            log.error("size of list > 1, list={}", message);
        }
    }
}
