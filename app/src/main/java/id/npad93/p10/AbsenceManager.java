package id.npad93.p10;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AbsenceManager implements AutoCloseable {
    public AbsenceManager(String path) {
        // Class.forName("org.sqlite.JDBC");
        try {
            // Buat koneksi database.
            conn = DriverManager.getConnection("jdbc:sqlite:" + path);

            // Bangun database jika belum ada.
            build();
        } catch (SQLException e) {
            // Oh no
            e.printStackTrace();
        }
    }

    private void build() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Eksekusi kueri untuk menge-cek apakah tabel "absen" sudah tersedia.
            // Tabel "sqlite_master" adalah tabel khusus pada SQLite3 yang berisi
            // semua tabel dan hal-hal lain yang ada pada sebuah berkas database.
            ResultSet result = stmt
                    .executeQuery("SELECT * FROM `sqlite_master` WHERE `type` = 'table' AND `name` = 'absen';");
            if (result.next()) {
                // Tabel sudah ada.
                result.close();
            } else {
                // Matikan autocommit untuk area kode ini.
                try (NoAutoCommit nac = new NoAutoCommit(conn)) {
                    // Jalankan kueri untuk membuat tabel.
                    stmt.execute(CREATE_TABLE);
                    stmt.execute(CREATE_INDEX);
                    conn.commit();
                }
            }
        }
    }

    public ObservableList<Absence> get() {
        if (list == null) {
            // Buat ObservableList baru.
            list = FXCollections.<Absence>observableArrayList();
            studentIdMap = new HashMap<>();

            try (Statement stmt = conn.createStatement()) {
                // Melakukan kueri terhadap tabel "absen"
                ResultSet result = stmt.executeQuery("SELECT * FROM `absen`;");

                while (result.next()) {
                    // Buat objek "Absence"
                    Absence data = new Absence()
                            // Isi data
                            .setRowId(result.getInt("id"))
                            .setName(result.getString("name"))
                            .setStudentId(result.getString("student_id"))
                            .setClassLocation((char) ('A' + result.getShort("class") - 1))
                            .setStatusId(result.getInt("status"))
                            .setAdditionalReason(result.getString("reason"));

                    // Masukkan ke list dan ke mapping "NIM"
                    list.add(data);
                    studentIdMap.put(data.getStudentId(), data);

                    // Atur observable supaya perubahan data otomatis masuk ke database.
                    setupObserver(data);
                }
            } catch (SQLException e) {
                // Oh no
                e.printStackTrace();
            }
        }

        return list;
    }

    public Absence insert(String name, String studentId, char classLocation) {
        ObservableList<Absence> absenceList = get();

        Absence data = new Absence()
                .setName(name)
                .setStudentId(studentId)
                .setClassLocation(classLocation);

        // Memasukkan data ke tabel "absen" dengan "prepared statement"
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO `absen` (`name`, `student_id`, `class`, `status`, `reason`) VALUES(?, ?, ?, 4, NULL);",
                new String[] { "id" })) {
            // Atur parameter "INSERT" yang dibutuhkan. Indexnya selalu mulai dari 1.
            stmt.setString(1, name);
            stmt.setString(2, studentId);
            stmt.setInt(3, 'A' - classLocation + 1);
            // Jalankan!
            stmt.executeUpdate();

            // Ambil "id" dari data yang baru dimasukkan.
            ResultSet result = stmt.getGeneratedKeys();
            if (!result.next()) {
                throw new RuntimeException("generated keys is unknown!");
            }
            data.setRowId(result.getInt(1));
        } catch (SQLException e) {
            // Oh no
            e.printStackTrace();
        }

        // Masukkan ke ObservableList dan mapping NIM.
        absenceList.add(data);
        studentIdMap.put(studentId, data);
        // Atur observable supaya perubahan data otomatis masuk ke database.
        setupObserver(data);

        return data;
    }

    public Absence get(String name) {
        get(); // Ensure populated
        return studentIdMap.get(name);
    }

    @Override
    public void close() {
        try {
            if (!conn.getAutoCommit()) {
                conn.rollback();
            }
        } catch (SQLException e) {
            // Oh no
            e.printStackTrace();
        }

        try {
            conn.close();
        } catch (SQLException e) {
            // Oh no
            e.printStackTrace();
        }
    }

    private void setupObserver(Absence absence) {
        absence.getObservableStatus().addListener((ev, oldv, newv) -> {
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE `absen` SET `status` = ? WHERE `id` = ?;")) {
                // Perbarui status absen.
                stmt.setInt(1, newv.ordinal());
                stmt.setInt(2, absence.getRowId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Oh no
                e.printStackTrace();
            }
        });
        absence.getObservableAdditionalReason().addListener((ev, oldv, newv) -> {
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE `absen` SET `reason` = ? WHERE `id` = ?;")) {
                // Perbarui alasan tidak hadir.
                stmt.setString(1, newv);
                stmt.setInt(2, absence.getRowId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Oh no
                e.printStackTrace();
            }
        });
    }

    private Connection conn;
    private ObservableList<Absence> list;
    private HashMap<String, Absence> studentIdMap;

    private static String CREATE_TABLE = """
                CREATE TABLE `absen` (
                    `id` INTEGER PRIMARY KEY,
                    `name` TEXT NOT NULL,
                    `student_id` TEXT NOT NULL UNIQUE, -- NIM
                    `class` INTEGER NOT NULL,          -- 1 = A, 2 = B, 3 = C, 4 = D
                    `status` INTEGER NOT NULL,         -- 1 = Hadir, 2 = Sakit, 3 = Izin, 4 = Alfa
                    `reason` TEXT
                );
            """;
    private static String CREATE_INDEX = "CREATE INDEX `absen_student_id` ON `absen` (`student_id`);";
}
