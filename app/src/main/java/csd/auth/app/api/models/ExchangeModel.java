package csd.auth.app.api.models;
import androidx.annotation.NonNull;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

/**
 * @author Nikolaos Bampaliaris
 * @version 1.0
 *
 * This is the Exchange model class.
 */
public class ExchangeModel
{
    /**The side of the exchange Enum.*/
    public enum SideE { INCOME, EXPENSE }

    /**The side of the exchange.*/
    @PropertyName("side")
    public  String side;

    /**The amount of the exchange.*/
    @PropertyName("value")
    public double value;

    /** The date and time of the exchange.*/
    @PropertyName("date_time")
    public Timestamp date_time;

    /** The UUID of the user who owns the exchange.*/
    @PropertyName("owner_user_uuid")
    public String owner_user_uuid;

    /** The UUID of the user who owes the exchange.*/
    @PropertyName("debt_user_uuid")
    public String debt_user_uuid;

    /**
     * Required default constructor for Firestore.
     */
    public ExchangeModel() {}

    /**
     * Constructor for a new exchange.
     * @param side The side of the exchange.
     * @param value The amount of the exchange.
     * @param date_time The date and time of the exchange.
     * @param owner_user_uuid The UUID of the user who owns the exchange.
     * @param debt_user_uuid The UUID of the user who owes the exchange.
     */
    public ExchangeModel(
            @NonNull SideE side,
            double value,
            @NonNull Timestamp date_time,
            @NonNull String owner_user_uuid,
            String debt_user_uuid)
    {
        this.side               = side.name();
        this.value              = value;
        this.date_time          = date_time;
        this.owner_user_uuid    = owner_user_uuid;
        this.debt_user_uuid     = debt_user_uuid;
    }

    /**
     * Checks if the exchange is an income.
     *
     * @return True if the exchange is an income, false otherwise.
     */
    public boolean isIncome()
    {
        return this.side.equals("INCOME");
    }

    /**
     * Checks if the provided UUID matches the debtor of this exchange.
     * @param user_uuid The UUID to check against the debt_user_uuid field.
     *
     * @return True if the user is the debtor; false if not or if it's a personal exchange.
     */
    public boolean isOwe(
            @NonNull String user_uuid
    )
    {
        return this.debt_user_uuid != null && this.debt_user_uuid.equals(user_uuid);
    }
}
