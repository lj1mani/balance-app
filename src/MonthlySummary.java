import java.time.YearMonth;

public class MonthlySummary {

    private YearMonth month;
    private double totalRevenue;
    private double totalExpense;
    private double totalProfit;
    private double carryOver;

    public MonthlySummary(YearMonth month, double carryOver) {
        this.month = month;
        this.carryOver = carryOver;
    }

    public void addDailyEntry(DailyEntry entry) {
        totalRevenue += entry.getRevenue();
        totalExpense += entry.getExpense();
        totalProfit = totalRevenue - totalExpense + carryOver;
    }
}
