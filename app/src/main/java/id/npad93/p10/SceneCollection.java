package id.npad93.p10;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Pattern;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

// Class pembantu untuk memanage scene-scene yang digunakan.
public class SceneCollection {
    static final int TITLE_SIZE = 48;
    static final int ALL_TEXT_SIZE = 28;

    public SceneCollection(Stage stage, AbsenceManager db, int width, int height) {
        mainScene = createMainScene(stage, db);
        absenceScene = createAbsenceScene(stage, db);
        base = new Scene(mainScene, width, height);
    }

    public Scene getMainScene() {
        return base;
    }

    // Fungsi pembantu untuk membuat scene tampilan awal
    BorderPane createMainScene(Stage stage, AbsenceManager db) {
        BorderPane root = new BorderPane();

        // Judul aplikasi
        StackPane titleContainer = new StackPane();
        titleContainer.setPadding(new Insets(8));
        Text title = new Text("Absen Kelas D");
        title.setFont(getFont(TITLE_SIZE));
        titleContainer.getChildren().add(title);

        // Tombol Mulai Absen
        Button startButton = new Button("Mulai Absen");
        startButton.setFont(getFont(ALL_TEXT_SIZE));
        startButton.setOnAction((e) -> {
            base.setRoot(absenceScene);
        });

        // "FileChooser" untuk opsi "Ekspor Absen"
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Ekspor Absen");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Comma-Separated Values", "*.csv"));

        // Tombol "Ekspor Absen"
        Button exportButton = new Button("Ekspor Absen");
        exportButton.setFont(getFont(ALL_TEXT_SIZE));
        exportButton.setOnAction((ev) -> {
            File result = chooser.showSaveDialog(stage);

            if (result != null) {
                try (FileWriter out = new FileWriter(result, StandardCharsets.UTF_8)) {
                    out.write("NIM,Nama,Kelas,Kehadiran,Alasan\n");

                    for (Absence absence : db.get()) {
                        out.write(absence.emitCSVRow());
                        out.write("\n");
                    }
                } catch (IOException e) {
                    // Oh no
                    e.printStackTrace();
                }
            }
        });

        // Grid untuk menampung tombol
        GridPane grid = new GridPane();
        grid.setVgap(4);
        GridPane.setHalignment(startButton, HPos.CENTER);
        GridPane.setHalignment(exportButton, HPos.CENTER);
        grid.add(startButton, 0, 0);
        grid.add(exportButton, 0, 1);
        grid.setAlignment(Pos.CENTER);

        root.setTop(titleContainer);
        root.setCenter(grid);

        return root;
    }

    // Fungsi pembantu untuk membuat scene tampilan absensi
    BorderPane createAbsenceScene(Stage stage, AbsenceManager db) {
        BorderPane root = new BorderPane();

        // Tombol "Kembali"
        Button backButton = new Button("Kembali");
        backButton.setFont(getFont(ALL_TEXT_SIZE));
        backButton.setOnAction((e) -> {
            base.setRoot(mainScene);
        });

        // Tombol "Tambah"
        Button addButton = new Button("Tambah");
        addButton.setFont(getFont(ALL_TEXT_SIZE));
        addButton.setOnAction((e) -> {
            Stage otherStage = createInsertWindow(stage, db);
            otherStage.showAndWait();
        });

        // Container tombol
        HBox buttonContainer = new HBox(backButton, addButton);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(4);
        buttonContainer.setPadding(new Insets(8));

        // TableView untuk daftar absen
        TableView<Absence> tableView = new TableView<>();
        Text emptyPlaceholder = new Text("Tidak ada mahasiswa");
        emptyPlaceholder.setFont(getFont(ALL_TEXT_SIZE));
        tableView.setPlaceholder(emptyPlaceholder);

        // Kolom "Nama"
        TableColumn<Absence, String> tableColumn1 = new TableColumn<>("Nama");
        tableColumn1.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getName()));

        // Kolom "NIM"
        TableColumn<Absence, String> tableColumn2 = new TableColumn<>("NIM");
        tableColumn2.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getStudentId()));

        // Kolom "Kelas"
        TableColumn<Absence, Character> tableColumn3 = new TableColumn<>("Kelas");
        tableColumn3.setCellValueFactory(
                param -> new SimpleObjectProperty<>(param.getValue().getClassLocation()));

        // Kolom "Kehadiran"
        TableColumn<Absence, AbsenceStatus> tableColumn4 = new TableColumn<>("Kehadiran");
        tableColumn4.setCellValueFactory(param -> param.getValue().getObservableStatus());
        tableColumn4.setCellFactory(ComboBoxTableCell.forTableColumn(Absence.ABSENCE_LIST));
        tableColumn4.addEventHandler(TableColumn.<Absence, AbsenceStatus>editCommitEvent(), (e) -> {
            Absence data = e.getRowValue();
            data.setStatus(e.getNewValue());
        });
        tableColumn4.setEditable(true);

        // Kolom "Alasan"
        TableColumn<Absence, String> tableColumn5 = new TableColumn<>("Alasan Lainnya");
        tableColumn5.setCellValueFactory(param -> param.getValue().getObservableAdditionalReason());
        tableColumn5.setCellFactory(TextFieldTableCell.forTableColumn());
        tableColumn5.addEventHandler(TableColumn.<Absence, String>editCommitEvent(), (e) -> {
            Absence data = e.getRowValue();
            data.setAdditionalReason(e.getNewValue());
        });
        tableColumn5.setEditable(true);

        // Masukkan kolom
        ObservableList<TableColumn<Absence, ?>> columns = tableView.getColumns();
        columns.add(tableColumn1);
        columns.add(tableColumn2);
        columns.add(tableColumn3);
        columns.add(tableColumn4);
        columns.add(tableColumn5);
        // Masukkan data
        tableView.setItems(db.get());
        // Biarkan tabel dapat diubah
        tableView.setEditable(true);
        // Atur ukuran font
        tableView.setStyle("-fx-font-size: " + ALL_TEXT_SIZE + ";"); // uh

        root.setTop(buttonContainer);
        root.setCenter(tableView);

        return root;
    }

    // Fungsi yang membuat window baru untuk tombol "Tambah"
    Stage createInsertWindow(Stage parent, AbsenceManager db) {
        // Buat Stage
        Stage result = new Stage();
        result.initModality(Modality.WINDOW_MODAL);
        result.initOwner(parent);

        // Buat GridPane
        GridPane root = new GridPane();
        root.setHgap(4);
        root.setVgap(4);
        root.setAlignment(Pos.CENTER);

        // Input "NIM"
        Text studentIdLabel = new Text("NIM");
        studentIdLabel.setFont(getFont(ALL_TEXT_SIZE));
        TextField studentIdText = new TextField();
        studentIdText.setPromptText("H071yy1xxx");
        studentIdText.setFont(getFont(ALL_TEXT_SIZE));

        // Input "Nama"
        Text nameLabel = new Text("Nama");
        nameLabel.setFont(getFont(ALL_TEXT_SIZE));
        TextField nameText = new TextField();
        nameText.setPromptText("Nama");
        nameText.setFont(getFont(ALL_TEXT_SIZE));

        // Input "Kelas"
        Text classLabel = new Text("Kelas");
        classLabel.setFont(getFont(ALL_TEXT_SIZE));
        ComboBox<Character> classList = new ComboBox<>();
        classList.getItems().addAll('A', 'B', 'C', 'D');
        classList.getSelectionModel().selectFirst();
        classList.setStyle("-fx-font-size: " + ALL_TEXT_SIZE + ";"); // uh

        // Tombol "Tambah"
        Button addButton = new Button("Tambah");
        addButton.setFont(getFont(ALL_TEXT_SIZE));
        addButton.setOnAction((e) -> {
            // Coba masukkan data.
            if (tryInsertData(result, db, nameText.getText(), studentIdText.getText(),
                    classList.getSelectionModel().getSelectedItem())) {
                // Tutup stage jika berhasil
                result.close();
            }
        });

        // Tombol "Batal"
        Button cancelButton = new Button("Batal");
        cancelButton.setFont(getFont(ALL_TEXT_SIZE));
        cancelButton.setOnAction((e) -> {
            // Tutup Stage sekarang.
            result.close();
        });

        // Masukkan
        root.addRow(0, studentIdLabel, studentIdText);
        root.addRow(1, nameLabel, nameText);
        root.addRow(2, classLabel, classList);
        root.addRow(3, addButton, cancelButton);

        // Buat Scene
        result.setScene(new Scene(root, 480, 640));
        return result;
    }

    // Fungsi pembantu untuk mencoba memasukkan data.
    private static boolean tryInsertData(Stage stage, AbsenceManager db, String name, String studentId,
            char classLocation) {
        // Validasi NIM
        if (studentId == null || studentId.isEmpty()) {
            showErrorMessageBox(stage, "NIM Bermasalah", "NIM kosong!");
            return false;
        }

        if (!STDID_MATCHER.matcher(studentId).matches()) {
            showErrorMessageBox(stage, "NIM Bermasalah", "NIM tidak valid untuk mata kuliah ini!\nNIM: " + studentId);
            return false;
        }

        if (name == null || name.isEmpty()) {
            showErrorMessageBox(stage, "Nama Bermasalah", "Nama kosong!");
            return false;
        }

        return db.insert(name, studentId, classLocation) != null;
    }

    // Fungsi pembantu untuk memunculkan Message Box jenis "Error"
    private static void showErrorMessageBox(Stage stage, String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Fungsi pembantu untuk memuat Font yang ada pada resources.
    private static Font getFont(double size) {
        if (fonts == null) {
            fonts = new HashMap<>();
        }

        Font f = fonts.get(size);
        if (f == null) {
            InputStream is = SceneCollection.class.getResourceAsStream("/Roboto-Regular.ttf");
            f = Font.loadFont(is, size);
            fonts.put(size, f);
        }

        return f;
    }

    private static HashMap<Double, Font> fonts;
    private Scene base;
    private BorderPane mainScene;
    private BorderPane absenceScene;
    private static Pattern STDID_MATCHER = Pattern.compile("H071\\d{2}1\\d{3}");
}
