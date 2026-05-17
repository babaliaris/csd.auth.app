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

    /**The title of the exchange.*/
    @PropertyName("title")
    public String title;

    /**The side of the exchange.*/
    @PropertyName("side")
    public  String side;

    /**The amount of the exchange.*/
    @PropertyName("value")
    public double value;

    /** The date and time of the exchange.*/
    @PropertyName("date_time")
    public Timestamp date_time;

    /** Value to mark an exchange as shared.*/
    @PropertyName("is_shared")
    public boolean is_shared;

    /** The service name that participates in the non-shared exchange*/
    @PropertyName("service_name")
    public String service_name;

    /** Value to mark an exchange as recurring.*/
    @PropertyName("is_recurring")
    public boolean is_recurring;

    /** Value to determine how often an exchange repeats.*/
    @PropertyName("recurring_type")
    public String recurring_type;

    /** The UUID of the user who owns the exchange.*/
    @PropertyName("owner_user_uuid")
    public String owner_user_uuid;

    /** The UUID of the user who owes the exchange.*/
    @PropertyName("debt_user_uuid")
    public String debt_user_uuid;

    /** The Firestore document ID of the exchange. */
    public String id;

    /**
     * Required default constructor for Firestore.
     */
    public ExchangeModel() {}

    /**
     * Constructor for a new exchange.
     *
     * @param title The title of the exchange.
     * @param side The side of the exchange (INCOME or EXPENSE).
     * @param value The amount of the exchange.
     * @param date_time The date and time of the exchange.
     * @param owner_user_uuid The UUID of the user who owns the exchange.
     * @param is_shared Whether the exchange is shared with another user.
     * @param debt_user_uuid The UUID of the user who owes the exchange (if shared).
     * @param is_recurring Whether the exchange is recurring.
     * @param recurring_type The recurrence type (e.g. weekly, monthly).
     * @param service_name The name of the service (if not shared).
     */
    public ExchangeModel(
            @NonNull String title,
            @NonNull SideE side,
            double value,
            @NonNull Timestamp date_time,
            @NonNull String owner_user_uuid,
            boolean is_shared,
            String debt_user_uuid,
            boolean is_recurring,
            String recurring_type,
            String service_name)

    {
        this.title              = title;
        this.side               = side.name();
        this.value              = value;
        this.date_time          = date_time;
        this.owner_user_uuid    = owner_user_uuid;
        this.debt_user_uuid     = debt_user_uuid;
        this.is_shared          = is_shared;
        this.service_name       = service_name;
        this.is_recurring       = is_recurring;
        this.recurring_type     = recurring_type;
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
