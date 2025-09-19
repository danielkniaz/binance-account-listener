package io.prada.listener.processor;

import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.AccountInformationV2Response;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.AllOrdersResponseInner;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.PositionInformationV2ResponseInner;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.prada.listener.dto.AccountResponse;
import io.prada.listener.dto.Signal;
import io.prada.listener.dto.accounting.AccountingSnapshot;
import io.prada.listener.dto.enums.Action;
import io.prada.listener.dto.enums.BalanceType;
import io.prada.listener.dto.enums.OrderType;
import io.prada.listener.dto.enums.SideType;
import io.prada.listener.service.AccountingSnapshotBuilder;
import io.prada.listener.service.RiskCalculationService;
import io.prada.listener.testUtils.TestFileUtils;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AccountDiffProcessorTest {
    private static final String PREFIX = "data/acc-diff-new/";

    final MathContext ctx = new MathContext(8, RoundingMode.HALF_EVEN);
    final ObjectMapper mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    final AccountDiffProcessor unit = new AccountDiffProcessor(
        new RiskCalculationService(ctx, TestFileUtils.appSettings(BalanceType.BALANCE)), ctx);
    final AccountingSnapshotBuilder builder = new AccountingSnapshotBuilder(ctx);


    @Nested class NewFormatAccDiffMarketTest {
        @Test void testAddToPositionWithNewFormat() {
            AccountingSnapshot old = buildFromApiResponses(
                "mkt/add/balance0.json",
                "mkt/add/pos0.json",
                "mkt/add/order.json");

            AccountingSnapshot now = buildFromApiResponses(
                "mkt/add/balance1.json",
                "mkt/add/pos1.json",
                "mkt/add/order.json");
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

        @Test void testClose() {
            AccountingSnapshot old = buildFromApiResponses(
                "mkt/close/balance0.json",
                "mkt/close/pos0.json",
                "mkt/close/order0.json");

            AccountingSnapshot now = buildFromApiResponses(
                "mkt/close/balance1.json",
                "mkt/close/pos1.json",
                "mkt/close/order1.json");
            List<Signal> result = unit.diff(now, old);

            Assertions.assertEquals(2, result.size());
            Assertions.assertEquals(Action.KILL, result.get(1).getAction());
            Assertions.assertEquals(OrderType.MARKET, result.get(1).getType());
            Assertions.assertTrue(result.get(1).isOut());
            Assertions.assertEquals(SideType.BUY.getDir(), result.get(1).getDirection());
            Assertions.assertEquals("TURBOUSDT", result.get(1).getSymbol());
        }

        @Test
        void testToNewPosition() {
            AccountingSnapshot old = buildFromApiResponses(
                "mkt/open/balance0.json",
                "mkt/open/pos0.json",
                "mkt/open/order.json");

            AccountingSnapshot now = buildFromApiResponses(
                "mkt/open/balance1.json",
                "mkt/open/pos1.json",
                "mkt/open/order.json");
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
            AccountingSnapshot old = buildFromApiResponses(
                "mkt/partial/balance0.json",
                "mkt/partial/pos0.json",
                "mkt/partial/order.json");

            AccountingSnapshot now = buildFromApiResponses(
                "mkt/partial/balance1.json",
                "mkt/partial/pos1.json",
                "mkt/partial/order.json");
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
    }

    @Nested class NewFormatAccDiffPendingTest {

        @Nested class StopLossAccDiffTest {
            @Test void newStopLoss() {
                AccountingSnapshot old = buildFromApiResponses(
                    "pend/sl/balance.json",
                    "pend/sl/pos.json",
                    "pend/sl/order0_0.json");

                AccountingSnapshot now = buildFromApiResponses(
                    "pend/sl/balance.json",
                    "pend/sl/pos.json",
                    "pend/sl/order0_1.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(1, result.size());
                Assertions.assertEquals(Action.NEW, result.get(0).getAction());
                Assertions.assertEquals(OrderType.STOP, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isOut());
                Assertions.assertEquals(0, new BigDecimal("1.331").compareTo(result.get(0).getPrice()));
                Assertions.assertEquals("POPCATUSDT", result.get(0).getSymbol());
            }

            @Test void moveStopLoss() {
                AccountingSnapshot old = buildFromApiResponses(
                    "pend/sl/balance.json",
                    "pend/sl/pos.json",
                    "pend/sl/order1_0.json");

                AccountingSnapshot now = buildFromApiResponses(
                    "pend/sl/balance.json",
                    "pend/sl/pos.json",
                    "pend/sl/order1_1.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(Action.MOVE, result.get(0).getAction());
                Assertions.assertEquals(OrderType.STOP, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isOut());
                Assertions.assertEquals(0, new BigDecimal("2.00").compareTo(result.get(0).getPrice()));
                Assertions.assertEquals("LDOUSDT", result.get(0).getSymbol());
            }

            @Test void killStopLoss() {
                AccountingSnapshot old = buildFromApiResponses(
                    "pend/sl/balance.json",
                    "pend/sl/pos.json",
                    "pend/sl/order2_0.json");

                AccountingSnapshot now = buildFromApiResponses(
                    "pend/sl/balance.json",
                    "pend/sl/pos.json",
                    "pend/sl/order2_1.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(1, result.size());
                Assertions.assertEquals(Action.KILL, result.get(0).getAction());
                Assertions.assertEquals(OrderType.STOP, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isOut());
                Assertions.assertEquals(0, new BigDecimal("2.00").compareTo(result.get(0).getPrice()));
                Assertions.assertEquals("LDOUSDT", result.get(0).getSymbol());
            }

            @Test void closeAndKillStoploss() {
                AccountingSnapshot old = buildFromApiResponses(
                    "mkt/close/balance0.json",
                    "mkt/close/pos0.json",
                    "mkt/close/order0.json");

                AccountingSnapshot now = buildFromApiResponses(
                    "mkt/close/balance1.json",
                    "mkt/close/pos1.json",
                    "mkt/close/order1.json");
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
                AccountingSnapshot old = buildFromApiResponses(
                    "pend/tp/balance.json",
                    "pend/tp/pos.json",
                    "pend/tp/order0.json");

                AccountingSnapshot now = buildFromApiResponses(
                    "pend/tp/balance.json",
                    "pend/tp/pos.json",
                    "pend/tp/order1.json");
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
                AccountingSnapshot old = buildFromApiResponses(
                    "pend/lmt/balance0.json",
                    "pend/lmt/pos0.json",
                    "pend/lmt/order0_0.json");

                AccountingSnapshot now = buildFromApiResponses(
                    "pend/lmt/balance0.json",
                    "pend/lmt/pos0.json",
                    "pend/lmt/order0_1.json");
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
                AccountingSnapshot old = buildFromApiResponses(
                    "pend/lmt/balance1.json",
                    "pend/lmt/pos0.json",
                    "pend/lmt/order1_0.json");

                AccountingSnapshot now = buildFromApiResponses(
                    "pend/lmt/balance1.json",
                    "pend/lmt/pos0.json",
                    "pend/lmt/order1_1.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(Action.MOVE, result.get(0).getAction());
                Assertions.assertEquals(OrderType.LIMIT, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isIn());
                Assertions.assertEquals(SideType.BUY.getDir(), result.get(0).getDirection());
                Assertions.assertEquals("TONUSDT", result.get(0).getSymbol());
            }

            @Test void killLimit() {
                AccountingSnapshot old = buildFromApiResponses(
                    "pend/lmt/balance1.json",
                    "pend/lmt/pos0.json",
                    "pend/lmt/order2_0.json");

                AccountingSnapshot now = buildFromApiResponses(
                    "pend/lmt/balance1.json",
                    "pend/lmt/pos0.json",
                    "pend/lmt/order2_1.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(Action.KILL, result.get(0).getAction());
                Assertions.assertEquals(OrderType.LIMIT, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isIn());
                Assertions.assertEquals(SideType.BUY.getDir(), result.get(0).getDirection());
                Assertions.assertEquals("TONUSDT", result.get(0).getSymbol());
            }

            @Test void openAndKillLimit() {
                AccountingSnapshot old = buildFromApiResponses(
                    "pend/lmt/balance3.json",
                    "pend/lmt/pos0.json",
                    "pend/lmt/order3_0.json");

                AccountingSnapshot now = buildFromApiResponses(
                    "pend/lmt/balance3.json",
                    "pend/lmt/pos0.json",
                    "pend/lmt/order3_1.json");
                List<Signal> result = unit.diff(now, old);
                Assertions.assertEquals(1, result.size());
                Assertions.assertEquals(Action.KILL, result.get(0).getAction());
                Assertions.assertEquals(OrderType.LIMIT, result.get(0).getType());
                Assertions.assertTrue(result.get(0).isIn());
                Assertions.assertEquals(SideType.BUY.getDir(), result.get(0).getDirection());
                Assertions.assertEquals("NEIROUSDT", result.get(0).getSymbol());
            }
        }

        @Nested class StopOrderAccDiffTest {
            @Test void newStopOrder() {
                AccountingSnapshot old = buildFromApiResponses(
                    "pend/stp/balance.json",
                    "pend/stp/pos.json",
                    "pend/stp/order0.json");

                AccountingSnapshot now = buildFromApiResponses(
                    "pend/stp/balance.json",
                    "pend/stp/pos.json",
                    "pend/stp/order1.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(1, result.size());
                Signal signal = result.get(0);
                Assertions.assertEquals(Action.NEW, signal.getAction());
                Assertions.assertEquals(OrderType.STOP, signal.getType());
                Assertions.assertEquals(1, signal.getDirection());
                Assertions.assertTrue(signal.isIn());
                Assertions.assertTrue(0 == new BigDecimal("14.").compareTo(result.get(0).getPrice()));
                Assertions.assertTrue(0 == new BigDecimal("0.15").compareTo(result.get(0).getRisk()));
                Assertions.assertEquals("TRUMPUSDT", result.get(0).getSymbol());
            }
            @Test void killStopOrder() {
                AccountingSnapshot old = buildFromApiResponses(
                    "pend/stp/balance.json",
                    "pend/stp/pos.json",
                    "pend/stp/order1.json");

                AccountingSnapshot now = buildFromApiResponses(
                    "pend/stp/balance.json",
                    "pend/stp/pos.json",
                    "pend/stp/order0.json");
                List<Signal> result = unit.diff(now, old);

                Assertions.assertEquals(1, result.size());
                Signal signal = result.get(0);
                Assertions.assertEquals(Action.KILL, signal.getAction());
                Assertions.assertEquals(OrderType.STOP, signal.getType());
                Assertions.assertTrue(signal.isIn());
                Assertions.assertEquals(0, new BigDecimal("14.00").compareTo(signal.getPrice()));
                Assertions.assertEquals("TRUMPUSDT", signal.getSymbol());
            }
        }
    }

    @Nested class NewFormatSuppressAccDiffTest {
        @Test void closeWithoutKillingSl() {
            AccountingSnapshot old = buildFromApiResponses(
                "mkt/close/balance0.json",
                "mkt/close/pos0.json",
                "mkt/close/order0.json");

            AccountingSnapshot now = buildFromApiResponses(
                "mkt/close/balance1.json",
                "mkt/close/pos1.json",
                "mkt/close/order1.json");
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
            AccountingSnapshot old = buildFromApiResponses(
                "pend/lmt/balance3.json",
                "pend/lmt/pos0.json",
                "pend/lmt/order3_0.json");
            AccountingSnapshot now = buildFromApiResponses(
                "pend/lmt/balance3.json",
                "pend/lmt/pos1.json",
                "pend/lmt/order3_1.json");
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
    private AccountingSnapshot buildFromApiResponses(String accountInfoFile, String positionsFile, String ordersFile) {
        AccountInformationV2Response accountInfo = mapper.readValue(
            TestFileUtils.load(PREFIX + accountInfoFile), AccountInformationV2Response.class);
        PositionInformationV2ResponseInner[] positions = mapper.readValue(
            TestFileUtils.load(PREFIX + positionsFile), PositionInformationV2ResponseInner[].class);
        AllOrdersResponseInner[] orders = mapper.readValue(
            TestFileUtils.load(PREFIX + ordersFile), AllOrdersResponseInner[].class);
        AccountResponse response = new AccountResponse(accountInfo, List.of(positions), List.of(orders));
        return builder.build(response);
    }
}