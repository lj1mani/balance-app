import java.time.LocalDate;

public class DailyEntry {
    private int id;
    private LocalDate date;
    private double revenue;
    private double expense;
    private double profit;

    public DailyEntry(LocalDate date, double revenue, double expense) {
        this.date = date;
        this.revenue = revenue;
        this.expense = expense;
        this.profit = revenue - expense;
    }
    public DailyEntry() {
        this.date = date;
        this.revenue = revenue;
        this.expense = expense;
        this.profit = revenue - expense;
    }


    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setExpense(double expense) {
        this.expense = expense;
    }

    public double getExpense() {
        return expense;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getProfit() {
        return profit;
    }
}



