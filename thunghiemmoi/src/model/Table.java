package model;

public class Table {
    private String tableID;
    private String tableType;
    private int seatCount;
    private String status;

    public Table() {}

    public Table(String tableID, String tableType, int seatCount, String status) {
        this.tableID = tableID;
        this.tableType = tableType;
        this.seatCount = seatCount;
        this.status = status;
    }

    public String getTableID() { return tableID; }
    public void setTableID(String tableID) { this.tableID = tableID; }

    public String getTableType() { return tableType; }
    public void setTableType(String tableType) { this.tableType = tableType; }

    public int getSeatCount() { return seatCount; }
    public void setSeatCount(int seatCount) { this.seatCount = seatCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}