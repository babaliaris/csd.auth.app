package csd.auth.app.utils;

import androidx.annotation.NonNull;
import csd.auth.app.api.models.ExchangeModel;
import java.util.List;

/**
 * @author Nikolaos Bampaliaris
 * @version 1.0
 *
 * This is the ExchangeCalculations class.
 * It's a simple-to-use class that provides
 * static methods to calculate various financial
 * metrics.
 */
public class ExchangeCalculations
{
    /**
     * @author Nikolaos Bampaliaris
     * Calcluates the total value of incomes.
     * @param exchanges A list of ExchangeModel objects.
     * @return The total value of incomes.
     */
    public static double calculateTotalIncomes(@NonNull List<ExchangeModel> exchanges)
    {
        double total = 0;
        for (ExchangeModel exchange : exchanges)
        {
            if (exchange.isIncome())
            {
                total += exchange.value;
            }
        }
        return total;
    }

    /**
     * @author Nikolaos Bampaliaris
     * Calcluates the total value of expenses.
     * @param exchanges A list of ExchangeModel objects.
     * @return The total value of expenses.
     */
    public static double calculateTotalExpenses(@NonNull List<ExchangeModel> exchanges)
    {
        double total = 0;
        for (ExchangeModel exchange : exchanges)
        {
            if (!exchange.isIncome())
            {
                total += exchange.value;
            }
        }
        return total;
    }

    /**
     * @author Nikolaos Bampaliaris
     * Calcluates the net balance.
     * @param exchanges A list of ExchangeModel objects.
     * @return The total value of incomes minus the total value of expenses.
     */
    public static double calculateNetBalance(@NonNull List<ExchangeModel> exchanges)
    {
        return calculateTotalIncomes(exchanges) - calculateTotalExpenses(exchanges);
    }

    /**
     * @author Nikolaos Bampaliaris
     * Calcluates the total value of what I owe.
     * @param exchangesIOwe A list of ExchangeModel objects.
     * @return The total value of what I owe.
     */
    public static double calculateTotalIOwe(@NonNull List<ExchangeModel> exchangesIOwe)
    {
        double total = 0;
        for (ExchangeModel exchange : exchangesIOwe)
        {
            total += exchange.value;
        }
        return total;
    }

    /**
     * @author Nikolaos Bampaliaris
     * Calcluates the total value of what is owed to me.
     * @param exchanges A list of ExchangeModel objects.
     * @return The total value of what is owed to me.
     */
    public static double calculateTotalOwedToMe(@NonNull List<ExchangeModel> exchanges)
    {
        double total = 0;
        for (ExchangeModel exchange : exchanges)
        {
            if (exchange.is_shared && exchange.debt_user_uuid != null)
            {
                total += exchange.value;
            }
        }
        return total;
    }
}
