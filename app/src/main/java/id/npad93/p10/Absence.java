package id.npad93.p10;

import java.util.Arrays;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

public class Absence {

    public int getRowId() {
        return rowid;
    }

    public Absence setRowId(int id) {
        rowid = id;
        return this;
    }

    public String getName() {
        return name.get();
    }

    public Absence setName(String name) {
        this.name.set(name);
        return this;
    }

    public String getStudentId() {
        return studentId.get();
    }

    public Absence setStudentId(String stdId) {
        studentId.set(stdId);
        return this;
    }

    public char getClassLocation() {
        switch (whereClass.get()) {
            case 1:
                return 'A';
            case 2:
                return 'B';
            case 3:
                return 'C';
            case 4:
                return 'D';
            default:
                return ' ';
        }
    }

    public Absence setClassLocation(char classLocation) {
        switch (classLocation) {
            case 'A':
                whereClass.set(1);
                break;
            case 'B':
                whereClass.set(2);
                break;
            case 'C':
                whereClass.set(3);
                break;
            case 'D':
                whereClass.set(4);
                break;
            default:
                throw new IllegalArgumentException("classLocation not ABCD");
        }

        return this;
    }

    public int getStatusId() {
        return status.get().ordinal();
    }

    public String getStatusString() {
        int s = status.get().ordinal();
        return (s >= 1 && s <= 4) ? ABSENCE_LIST[s - 1].name() : "";
    }

    public ObservableValue<AbsenceStatus> getObservableStatus() {
        return status;
    }

    public Absence setStatus(AbsenceStatus status) {
        this.status.set(status);
        return this;
    }

    public Absence setStatusId(int id) {
        if (id >= 1 && id <= 4) {
            return setStatus(ABSENCE_LIST[id - 1]);
        }

        throw new IllegalArgumentException("id out of range");
    }

    public String getAdditionalReason() {
        String r = additionalReason.get();
        return r != null ? r : "";
    }

    public ObservableValue<String> getObservableAdditionalReason() {
        return additionalReason;
    }

    public Absence setAdditionalReason(String r) {
        additionalReason.set(r != null ? r : "");
        return this;
    }

    public String emitCSVRow() {
        // Append NIM
        StringBuilder result = new StringBuilder(getStudentId());
        result.append(',');

        // Append Nama
        result.append(Util.escapeCSV(getName()));
        result.append(',');

        // Append Kelas
        result.append(getClassLocation());
        result.append(',');

        // Append Kehadiran
        result.append(getStatusId());
        result.append(',');

        // Append alasan (jika ada)
        result.append(Util.escapeCSV(getAdditionalReason()));

        return result.toString();
    }

    private int rowid;
    private SimpleStringProperty name = new SimpleStringProperty();
    private SimpleStringProperty studentId = new SimpleStringProperty();
    private SimpleIntegerProperty whereClass = new SimpleIntegerProperty();
    private SimpleObjectProperty<AbsenceStatus> status = new SimpleObjectProperty<>();
    private SimpleStringProperty additionalReason = new SimpleStringProperty();

    public static AbsenceStatus[] ABSENCE_LIST;
    static {
        AbsenceStatus[] temp = AbsenceStatus.values();
        ABSENCE_LIST = Arrays.copyOfRange(temp, 1, temp.length);
    }
}
