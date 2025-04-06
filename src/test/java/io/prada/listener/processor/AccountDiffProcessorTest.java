package io.prada.listener.processor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.prada.listener.dto.Signal;
import io.prada.listener.dto.accounting.AccountingSnapshot;
import io.prada.listener.dto.enums.Action;
import io.prada.listener.dto.enums.BalanceType;
import io.prada.listener.dto.enums.OrderType;
import io.prada.listener.dto.enums.SideType;
import io.prada.listener.service.AccountingSnapshotBuilder;
import io.prada.listener.service.RiskCalculationService;
import io.prada.listener.service.request.RequestType;
import io.prada.listener.testUtils.TestFileUtils;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

class AccountDiffProcessorTest {
    private static final String PREFIX = "data/account-diff/";

    final MathContext ctx = new MathContext(8, RoundingMode.HALF_EVEN);
    final ObjectMapper mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    final AccountDiffProcessor unit = new AccountDiffProcessor(
        new RiskCalculationService(ctx, TestFileUtils.appSettings(BalanceType.BALANCE)), ctx);
    final AccountingSnapshotBuilder builder = new AccountingSnapshotBuilder(mapper, ctx);

    @Nested class MarketAccDiffTest {
        @Test void testAddToPosition() {
            AccountingSnapshot old = build("mkt-diff/add/balance0.json", "mkt-diff/add/order.json");
            AccountingSnapshot now = build("mkt-diff/add/balance1.json", "mkt-diff/add/order.json");
            List<Signal> result = unit.diff(now, old);

            Assertions.assertEquals(1, result.size());
            Assertions.assertEquals(Action.VOLUME, result.get(0).getAction());
            Assertions.assertEquals(OrderType.MARKET, result.get(0).getType());
            Assertions.assertTrue(result.get(0).isIn());
            Assertions.assertEquals(0, new BigDecimal("2.0971").compareTo(result.get(0).getPrice()));
            Assertions.assertTrue(190 < result.get(0).getRisk().intValue());
            Assertions.assertTrue(210 > result.get(0).getRisk().intValue());
            Assertions.assertEquals(SideType.BUY.getDir(), result.get(0).getDirection());
            Assertions.assertEquals("PHBUSDT", result.get(0).getSymbol());
        }

        @Test void testToNewPosition() {
            AccountingSnapshot old = build("mkt-diff/open/balance0.json", "mkt-diff/open/order.json");
            AccountingSnapshot now = build("mkt-diff/open/balance1.json", "mkt-diff/open/order.json");
            List<Signal> result = unit.diff(now, old);

            Assertions.assertEquals(1, result.size());
            Assertions.assertEquals(Action.NEW, result.get(0).getAction());
            Assertions.assertEquals(OrderType.MARKET, result.get(0).getType());
            Assertions.assertTrue(result.get(0).isIn());
            Assertions.assertEquals(0, new BigDecimal("0.06088").compareTo(result.get(0).getPrice()));
            Assertions.assertEquals(SideType.BUY.getDir(), result.get(0).getDirection());
            Assertions.assertEquals(0, new BigDecimal("0.37").compareTo(result.get(0).getRisk()));
            Assertions.assertEquals("ARPAUSDT", result.get(0).getSymbol());
        }

        @Test void testToPartialClose() {
            AccountingSnapshot old = build("mkt-diff/partial/balance0.json", "mkt-diff/partial/order.json");
            AccountingSnapshot now = build("mkt-diff/partial/balance1.json", "mkt-diff/partial/order.json");
            List<Signal> result = unit.diff(now, old);

            Assertions.assertEquals(1, result.size());
            Assertions.assertEquals(Action.VOLUME, result.get(0).getAction());
            Assertions.assertEquals(OrderType.MARKET, result.get(0).getType());
            Assertions.assertTrue(result.get(0).isOut());
            Assertions.assertTrue(new BigDecimal("0.0624").compareTo(result.get(0).getPrice()) > 0);
            Assertions.assertTrue(new BigDecimal("0.0622").compareTo(result.get(0).getPrice()) < 0);
            Assertions.assertTrue(45 < result.get(0).getRisk().intValue());
            Assertions.assertTrue(55 > result.get(0).getRisk().intValue());
            Assertions.assertEquals(SideType.BUY.getDir(), result.get(0).getDirection());
            Assertions.assertEquals("ARPAUSDT", result.get(0).getSymbol());
        }

        @Test //no exit price
        void testToClose() {
            AccountingSnapshot old = build("mkt-diff/close/balance0.json", "mkt-diff/close/order0.json");
            AccountingSnapshot now = build("mkt-diff/close/balance1.json", "mkt-diff/close/order1.json");
            List<Signal> result = unit.diff(now, old);

            Assertions.assertEquals(2, result.size());
            Assertions.assertEquals(Action.KILL, result.get(1).getAction());
            Assertions.assertEquals(OrderType.MARKET, result.get(1).getType());
            Assertions.assertTrue(result.get(1).isOut());
            Assertions.assertEquals(SideType.BUY.getDir(), result.get(1).getDirection());
            Assertions.assertEquals("TURBOUSDT", result.get(1).getSymbol());
            //TODO: do not have exit price, probably need events
        }
    }

    @Nested class PendingAccDiffTest {

        @Nested class StopLossAccDiffTest {
            @Test void newStopLoss() {
                AccountingSnapshot old = build("pend/sl/balance.json", "pend/sl/0order0.json");
                AccountingSnapshot now = build("pend/sl/balance.json", "pend/sl/0order1.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(1, result.size());
                Assertions.assertEquals(Action.NEW, result.get(0).getAction());
                Assertions.assertEquals(OrderType.STOP, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isOut());
                Assertions.assertEquals(0, new BigDecimal("1.331").compareTo(result.get(0).getPrice()));
                Assertions.assertEquals("POPCATUSDT", result.get(0).getSymbol());
            }

            @Test void moveStopLoss() {
                AccountingSnapshot old = build("pend/sl/balance.json", "pend/sl/1order0.json");
                AccountingSnapshot now = build("pend/sl/balance.json", "pend/sl/1order1.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(Action.MOVE, result.get(0).getAction());
                Assertions.assertEquals(OrderType.STOP, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isOut());
                Assertions.assertEquals(0, new BigDecimal("2.00").compareTo(result.get(0).getPrice()));
                Assertions.assertEquals("LDOUSDT", result.get(0).getSymbol());
            }

            @Test void killStopLoss() {
                AccountingSnapshot old = build("pend/sl/balance.json", "pend/sl/2order0.json");
                AccountingSnapshot now = build("pend/sl/balance.json", "pend/sl/2order1.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(1, result.size());
                Assertions.assertEquals(Action.KILL, result.get(0).getAction());
                Assertions.assertEquals(OrderType.STOP, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isOut());
                Assertions.assertEquals(0, new BigDecimal("2.00").compareTo(result.get(0).getPrice()));
                Assertions.assertEquals("LDOUSDT", result.get(0).getSymbol());
            }

            @Test void closeAndKillStoploss() {
                AccountingSnapshot old = build("mkt-diff/close/balance0.json", "mkt-diff/close/order0.json");
                AccountingSnapshot now = build("mkt-diff/close/balance1.json", "mkt-diff/close/order1.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(2, result.size());
                Assertions.assertEquals(Action.KILL, result.get(0).getAction());
                Assertions.assertEquals(OrderType.STOP, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isOut());
                Assertions.assertEquals(0, new BigDecimal("0.0086").compareTo(result.get(0).getPrice()));
                Assertions.assertEquals("TURBOUSDT", result.get(1).getSymbol());
            }
        }

        @Nested class TakeProfitAccDiffTest {
            @Test void newTp() {
                AccountingSnapshot old = build("pend/tp/balance.json", "pend/tp/0order0.json");
                AccountingSnapshot now = build("pend/tp/balance.json", "pend/tp/0order1.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(1, result.size());
                Assertions.assertEquals(Action.NEW, result.get(0).getAction());
                Assertions.assertEquals(OrderType.LIMIT, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isOut());
                Assertions.assertEquals(0, new BigDecimal("0.1230").compareTo(result.get(0).getPrice()));
                Assertions.assertEquals("MYROUSDT", result.get(0).getSymbol());
            }

            @Test void moveTp() {
                //TODO:
            }
            @Test void killTp() {
                //TODO:
            }
            @Test void closeAndKillTp() {
                //TODO:
            }
        }
        @Nested class LimitAccDiffTest {
            @Test void newLimit() {
                AccountingSnapshot old = build("pend/lmt/0balance.json", "pend/lmt/0order0.json");
                AccountingSnapshot now = build("pend/lmt/0balance.json", "pend/lmt/0order1.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(1, result.size());
                Assertions.assertEquals(Action.NEW, result.get(0).getAction());
                Assertions.assertEquals(OrderType.LIMIT, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isIn());
                Assertions.assertEquals(0, new BigDecimal("6.00").compareTo(result.get(0).getPrice()));
                Assertions.assertEquals(SideType.BUY.getDir(), result.get(0).getDirection());
                Assertions.assertEquals(0, new BigDecimal("0.17").compareTo(result.get(0).getRisk()));
                Assertions.assertEquals("TONUSDT", result.get(0).getSymbol());
            }

            @Test void moveLimit() {
                AccountingSnapshot old = build("pend/lmt/1balance.json", "pend/lmt/1order0.json");
                AccountingSnapshot now = build("pend/lmt/1balance.json", "pend/lmt/1order1.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(Action.MOVE, result.get(0).getAction());
                Assertions.assertEquals(OrderType.LIMIT, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isIn());
                Assertions.assertEquals(SideType.BUY.getDir(), result.get(0).getDirection());
                Assertions.assertEquals("TONUSDT", result.get(0).getSymbol());
            }

            @Test void killLimit() {
                AccountingSnapshot old = build("pend/lmt/1balance.json", "pend/lmt/2order0.json");
                AccountingSnapshot now = build("pend/lmt/1balance.json", "pend/lmt/2order1.json");
                List<Signal> result = unit.diff(now, old);
                System.out.println(result);
                Assertions.assertEquals(Action.KILL, result.get(0).getAction());
                Assertions.assertEquals(OrderType.LIMIT, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isIn());
                Assertions.assertEquals(SideType.BUY.getDir(), result.get(0).getDirection());
                Assertions.assertEquals("TONUSDT", result.get(0).getSymbol());
            }

            @Test void openAndKillLimit() {
                AccountingSnapshot old = build("pend/lmt/3balance0.json", "pend/lmt/3order0.json");
                AccountingSnapshot now = build("pend/lmt/3balance1.json", "pend/lmt/3order1.json");
                List<Signal> result = unit.diff(now, old);
                Assertions.assertEquals(2, result.size());
                Assertions.assertEquals(Action.KILL, result.get(0).getAction());
                Assertions.assertEquals(OrderType.LIMIT, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isIn());
                Assertions.assertEquals(SideType.BUY.getDir(), result.get(0).getDirection());
                Assertions.assertEquals("NEIROUSDT", result.get(0).getSymbol());
            }
        }
    }

    @Nested class SuppressAccDiffTest {
        @Test void closeWithoutKillingSl() {
            AccountingSnapshot old = build("mkt-diff/close/balance0.json", "mkt-diff/close/order0.json");
            AccountingSnapshot now = build("mkt-diff/close/balance1.json", "mkt-diff/close/order1.json");
            List<Signal> result = unit.diff(now, old);
            result = unit.suppress(result);

            Assertions.assertEquals(Action.KILL, result.get(0).getAction());
            Assertions.assertEquals(OrderType.MARKET, result.get(0).getType());
            Assertions.assertTrue(result.get(0).isOut());
            Assertions.assertEquals(SideType.BUY.getDir(), result.get(0).getDirection());
            Assertions.assertEquals("TURBOUSDT", result.get(0).getSymbol());
        }

        @Test void closeWithoutKillingTp() {
            //TODO:
            Assertions.assertTrue(true);
        }

        @Test void openWithoutKillingLmt() {
            AccountingSnapshot old = build("pend/lmt/3balance0.json", "pend/lmt/3order0.json");
            AccountingSnapshot now = build("pend/lmt/3balance1.json", "pend/lmt/3order1.json");
            List<Signal> result = unit.diff(now, old);
            result = unit.suppress(result);

            Assertions.assertEquals(1, result.size());
            Assertions.assertEquals(Action.NEW, result.get(0).getAction());
            Assertions.assertEquals(OrderType.MARKET, result.get(0).getType());
            Assertions.assertTrue(result.get(0).isIn());
            Assertions.assertEquals(0, new BigDecimal("0.0015475").compareTo(result.get(0).getPrice()));
            Assertions.assertTrue(0.17 < result.get(0).getRisk().doubleValue());
            Assertions.assertTrue(0.20 > result.get(0).getRisk().doubleValue());
            Assertions.assertEquals(SideType.BUY.getDir(), result.get(0).getDirection());
            Assertions.assertEquals("NEIROUSDT", result.get(0).getSymbol());
        }
    }

    @SneakyThrows
    private AccountingSnapshot build(String balance, String orders) {
        var r1 = Pair.of(RequestType.BALANCE, (ObjectNode) mapper.readTree(
            TestFileUtils.load(PREFIX + balance)));
        var r2 = Pair.of(RequestType.ORDER, (ObjectNode) mapper
            .createObjectNode().set(RequestType.ORDER.name(), mapper.readTree(
                TestFileUtils.load(PREFIX + orders)).get(RequestType.ORDER.name())));
        return builder.build(List.of(r1, r2));
    }
}