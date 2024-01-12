import org.bidribidi.currency.model.Currency;
import org.bidribidi.currency.model.ExchangeRate;
import org.bidribidi.currency.service.CurrencyService;
import org.bidribidi.currency.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateTest {
    private final static Currency USD = new Currency("USD", "US Dollar", "$");
    private final static Currency EUR = new Currency("EUR", "Euro", "€");
    private final static Currency test1 = new Currency("test1", "test name1", "1");
    private final static Currency test2 = new Currency("test2", "test name2", "2");
    private final static Currency test3 = new Currency("test3", "test name3", "3");
    private final static Currency test4 = new Currency("test4", "test name4", "4");
    private final static Currency test5 = new Currency("test5", "test name5", "5");
    private final static Currency test6 = new Currency("test6", "test name6", "6");
    private final static List<ExchangeRate> TEST_EXCHANGE_RATES = new ArrayList<>();
    @Mock
    ExchangeRateService exchangeRateServiceMock;
    @Mock
    CurrencyService currencyServiceMock;

    @BeforeEach
    public void init() throws SQLException, IllegalAccessException {
        TEST_EXCHANGE_RATES.addAll(List.of(
                new ExchangeRate(USD, test1, 2),
                new ExchangeRate(test1, test2, 3),
                new ExchangeRate(test2, EUR, 4)
        ));
        ExchangeRateService exchangeRateService = new ExchangeRateService();
        exchangeRateServiceMock = Mockito.spy(exchangeRateService);

        Field field = ReflectionUtils
                .findFields(ExchangeRateService.class, f -> f.getName().equals("currencyService"),
                        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .get(0);

        field.setAccessible(true);
        field.set(exchangeRateServiceMock, currencyServiceMock);
        when(currencyServiceMock.getCurrencyByCode("USD")).thenReturn(USD);
        when(currencyServiceMock.getCurrencyByCode("EUR")).thenReturn(EUR);
    }
    @Test
    public void getUndirectExchangeRateByCodesTest_simple() {
        try {
            Mockito.doReturn(TEST_EXCHANGE_RATES).when(exchangeRateServiceMock).getAllExchangeRates();
            ExchangeRate er = exchangeRateServiceMock.getIndirectExchangeRateByCodes("USD", "EUR");
            assertEquals(24.0, er.getRate());
        } catch (SQLException e) {
            //
        } catch (NoSuchElementException e2) {
            e2.printStackTrace();
        }
    }

    @Test
    public void getUndirectExchangeRateByCodesTest_complex1() {
        TEST_EXCHANGE_RATES.add(new ExchangeRate(test3, test2, 0.5));
        TEST_EXCHANGE_RATES.add(new ExchangeRate(EUR, test3, 0.25));
        TEST_EXCHANGE_RATES.add(new ExchangeRate(EUR, test1, 2));
        TEST_EXCHANGE_RATES.add(new ExchangeRate(USD, EUR, 0.1));
        try {
            Mockito.doReturn(TEST_EXCHANGE_RATES).when(exchangeRateServiceMock).getAllExchangeRates();
            ExchangeRate er = exchangeRateServiceMock.getIndirectExchangeRateByCodes("USD", "EUR");
            assertEquals(24.0, er.getRate());
        } catch (SQLException e) {
            //
        } catch (NoSuchElementException e2) {
            e2.printStackTrace();
        }
    }

    @Test
    public void getUndirectExchangeRateByCodesTest_complex2() {
        TEST_EXCHANGE_RATES.add(new ExchangeRate(test3, test2, 0.5));
        TEST_EXCHANGE_RATES.add(new ExchangeRate(EUR, test3, 0.25));
        TEST_EXCHANGE_RATES.add(new ExchangeRate(EUR, test1, 2));
        try {
            Mockito.doReturn(TEST_EXCHANGE_RATES).when(exchangeRateServiceMock).getAllExchangeRates();
            ExchangeRate er = exchangeRateServiceMock.getIndirectExchangeRateByCodes("USD", "EUR");
            assertEquals(1.0, er.getRate());
        } catch (SQLException e) {
            //
        } catch (NoSuchElementException e2) {
            e2.printStackTrace();
        }
    }
}
