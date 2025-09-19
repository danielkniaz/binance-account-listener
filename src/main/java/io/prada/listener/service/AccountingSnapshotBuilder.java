package io.prada.listener.service;

import io.prada.listener.dto.AccountResponse;
import io.prada.listener.dto.accounting.AccountingConsolidatedSymbolInfo;
import io.prada.listener.dto.accounting.AccountingOrder;
import io.prada.listener.dto.accounting.AccountingPosition;
import io.prada.listener.dto.accounting.AccountingSnapshot;
import io.prada.listener.dto.accounting.AccountingSnapshot.Balances;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountingSnapshotBuilder {
    private final MathContext ctx;

    public AccountingSnapshot build(AccountResponse response) {
        AccountingSnapshot result = new AccountingSnapshot().setBalances(new Balances(
            new BigDecimal(response.accountInfo().getTotalWalletBalance(), ctx),
            new BigDecimal(response.accountInfo().getTotalMarginBalance(), ctx),
            new BigDecimal(response.accountInfo().getMaxWithdrawAmount(), ctx)
        ));
        response.positions().stream().filter(v -> new BigDecimal(v.getPositionAmt(), ctx).compareTo(BigDecimal.ZERO) != 0)
            .forEach(pos -> result.getSymbolSnapShots().computeIfAbsent(pos.getSymbol(), AccountingConsolidatedSymbolInfo::new)
                .setPosition(new AccountingPosition(pos, ctx)));
        response.orders().stream().forEach(ord -> result.getSymbolSnapShots().computeIfAbsent(ord.getSymbol(), AccountingConsolidatedSymbolInfo::new)
                .getOrders().add(new AccountingOrder(ord, ctx)));
        return result;
    }

    public AccountingSnapshot copy(AccountingSnapshot from) {
        Map<String, AccountingConsolidatedSymbolInfo> symbolMap = new HashMap<>();
        for (Map.Entry<String,AccountingConsolidatedSymbolInfo> entry : from.getSymbolSnapShots().entrySet()) {
            symbolMap.put(entry.getKey(), new AccountingConsolidatedSymbolInfo(entry.getValue()));
        }
        return new AccountingSnapshot().setBalances(Balances.copy(from.getBalances())).setSymbolSnapShots(symbolMap);
    }
}
