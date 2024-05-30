import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class PembelianGUI extends JFrame {
    private JTextField kodePembelianField;
    private JTextField namaLengkapField;
    private JTextField jenisBarangField;
    private JTextField totalBarangField;
    private JTextField hargaBarangField;
    private DefaultTableModel tableModel;
    private JTable table;
    private Connection connection;

    public PembelianGUI() {
        // Menginisiasi komponen GUI
        setTitle("Pendataan Pembelian");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2, 10, 10));

        panel.add(new JLabel("Kode Pembelian:"));
        kodePembelianField = new JTextField();
        panel.add(kodePembelianField);

        panel.add(new JLabel("Nama Lengkap:"));
        namaLengkapField = new JTextField();
        panel.add(namaLengkapField);

        panel.add(new JLabel("Jenis Barang:"));
        jenisBarangField = new JTextField();
        panel.add(jenisBarangField);

        panel.add(new JLabel("Total Barang:"));
        totalBarangField = new JTextField();
        panel.add(totalBarangField);

        panel.add(new JLabel("Harga Barang:"));
        hargaBarangField = new JTextField();
        panel.add(hargaBarangField);

        JButton addButton = new JButton("Tambah Data");
        panel.add(addButton);

        JButton deleteButton = new JButton("Hapus Data");
        panel.add(deleteButton);

        getContentPane().add(panel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Kode Pembelian", "Nama Lengkap", "Jenis Barang", "Total Barang", "Harga Barang", "Total Harga"}, 0);
        table = new JTable(tableModel);
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        // Menyambungkan ke Database SQL
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pembelian_db", "root", "password"); // Sesuaikan username dan password MySQL Anda
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + e.getMessage());
        }

        // Memuat data yang ada dari basis data
        loadDataFromDatabase();

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String kodePembelian = kodePembelianField.getText();
                    String namaLengkap = namaLengkapField.getText();
                    String jenisBarang = jenisBarangField.getText();
                    String totalBarangStr = totalBarangField.getText();
                    String hargaBarangStr = hargaBarangField.getText();

                    // Verifikasi input
                    if (kodePembelian.isEmpty() || namaLengkap.isEmpty() || jenisBarang.isEmpty() || totalBarangStr.isEmpty() || hargaBarangStr.isEmpty()) {
                        JOptionPane.showMessageDialog(PembelianGUI.this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    int totalBarang;
                    double hargaBarang;
                    try {
                        totalBarang = Integer.parseInt(totalBarangStr);
                        hargaBarang = Double.parseDouble(hargaBarangStr);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(PembelianGUI.this, "Total Barang harus berupa angka dan Harga Barang harus berupa angka desimal!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    double totalHarga = totalBarang * hargaBarang;

                    // Konfirmasi input
                    int confirm = JOptionPane.showConfirmDialog(PembelianGUI.this,
                            "Apakah data yang diisi sudah benar?\n" +
                                    "Kode Pembelian: " + kodePembelian + "\n" +
                                    "Nama Lengkap: " + namaLengkap + "\n" +
                                    "Jenis Barang: " + jenisBarang + "\n" +
                                    "Total Barang: " + totalBarang + "\n" +
                                    "Harga Barang: " + hargaBarang + "\n" +
                                    "Total Harga: " + totalHarga,
                            "Konfirmasi Data",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }

                    String sql = "INSERT INTO pembelian (kode_pembelian, nama_lengkap, jenis_barang, total_barang, harga_barang, total_harga) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, kodePembelian);
                    statement.setString(2, namaLengkap);
                    statement.setString(3, jenisBarang);
                    statement.setInt(4, totalBarang);
                    statement.setDouble(5, hargaBarang);
                    statement.setDouble(6, totalHarga);
                    statement.executeUpdate();

                    tableModel.addRow(new Object[]{kodePembelian, namaLengkap, jenisBarang, totalBarang, hargaBarang, totalHarga});

                    // Menghapus fields setelah menambahkan
                    kodePembelianField.setText("");
                    namaLengkapField.setText("");
                    jenisBarangField.setText("");
                    totalBarangField.setText("");
                    hargaBarangField.setText("");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(PembelianGUI.this, "Error: " + ex.getMessage());
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(PembelianGUI.this, "Pilih data yang ingin dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String kodePembelian = tableModel.getValueAt(selectedRow, 0).toString();

                try {
                    String sql = "DELETE FROM pembelian WHERE kode_pembelian = ?";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, kodePembelian);
                    statement.executeUpdate();

                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(PembelianGUI.this, "Data berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(PembelianGUI.this, "Error: " + ex.getMessage());
                }
            }
        });
    }

    private void loadDataFromDatabase() {
        try {
            String sql = "SELECT * FROM pembelian";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                String kodePembelian = resultSet.getString("kode_pembelian");
                String namaLengkap = resultSet.getString("nama_lengkap");
                String jenisBarang = resultSet.getString("jenis_barang");
                int totalBarang = resultSet.getInt("total_barang");
                double hargaBarang = resultSet.getDouble("harga_barang");
                double totalHarga = resultSet.getDouble("total_harga");

                tableModel.addRow(new Object[]{kodePembelian, namaLengkap, jenisBarang, totalBarang, hargaBarang, totalHarga});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data from database: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PembelianGUI().setVisible(true);
            }
        });
    }
}
