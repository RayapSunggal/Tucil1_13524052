import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class programGUI {

    private JFrame frame;
    private JPanel boardContainer;
    private JLabel nama_file, ukuran, waktu_komputasi, jumlah_percobaan, status;
    private JButton uploadButton1, uploadButton2, saveButton, clearButton;

    private char[][] lastPapan = null;
    private char[][] lastSolusi = null;
    private JPanel lastVisualPanel = null;
    private File lastInputFile = null;

    static final Color[] warna = new Color[] {
        new Color(31, 119, 180),
        new Color(255, 127, 14),
        new Color(44, 160, 44),
        new Color(214, 39, 40),
        new Color(148, 103, 189),
        new Color(140, 86, 75),
        new Color(227, 119, 194),
        new Color(127, 127, 127),
        new Color(188, 189, 34),
        new Color(23, 190, 207),
        new Color(0, 102, 204),
        new Color(255, 153, 0),
        new Color(0, 153, 51),
        new Color(204, 0, 0),
        new Color(102, 0, 204),
        new Color(153, 102, 51),
        new Color(204, 51, 153),
        new Color(90, 90, 90),
        new Color(153, 153, 0),
        new Color(0, 153, 153),
        new Color(51, 204, 255),
        new Color(255, 51, 102),
        new Color(153, 204, 0),
        new Color(255, 204, 0),
        new Color(0, 153, 255),
        new Color(255, 102, 0)
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new programGUI().start());
    }

    private void start() {
        frame=new JFrame("Penyelesaian Permainan Queens Linkedin ");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,600);

        uploadButton1=new JButton("Brute Force Murni");
        uploadButton1.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        uploadButton1.setPreferredSize(new Dimension(260,55));

        uploadButton2=new JButton("Brute Force Optimal");
        uploadButton2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        uploadButton2.setPreferredSize(new Dimension(260,55));

        boardContainer=new JPanel(new BorderLayout());

        frame.setLayout(new BorderLayout());

        JPanel topPanel=new JPanel(new FlowLayout(FlowLayout.CENTER,10,5));
        topPanel.add(uploadButton1);
        topPanel.add(uploadButton2);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(boardContainer, BorderLayout.CENTER);

        JPanel statsPanel = new JPanel(new GridLayout(5,2,10,2));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));


        statsPanel.add(new JLabel("File:"));
        nama_file=new JLabel("-");
        statsPanel.add(nama_file);

        statsPanel.add(new JLabel("Ukuran:"));
        ukuran=new JLabel("-");
        statsPanel.add(ukuran);

        statsPanel.add(new JLabel("Waktu (detik):"));
        waktu_komputasi=new JLabel("-");
        statsPanel.add(waktu_komputasi);

        statsPanel.add(new JLabel("Konfigurasi:"));
        jumlah_percobaan=new JLabel("-");
        statsPanel.add(jumlah_percobaan);

        statsPanel.add(new JLabel("Status:"));
        status=new JLabel("Pilih file input untuk mulai");
        statsPanel.add(status);

        Font f=new Font(Font.SANS_SERIF,Font.PLAIN,20);

        for (Component comp : statsPanel.getComponents()) {
            if (comp instanceof JLabel) {
                comp.setFont(f);
            }
        }

        saveButton=new JButton("Simpan");
        saveButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        saveButton.setPreferredSize(new Dimension(260, 55));
        saveButton.setEnabled(false);

        clearButton=new JButton("Kosongkan");
        clearButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        clearButton.setPreferredSize(new Dimension(260, 55));
        clearButton.setEnabled(false);

        JPanel bottomButtonsPanel=new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        bottomButtonsPanel.add(saveButton);
        bottomButtonsPanel.add(clearButton);

        JPanel bottomContainer=new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.add(statsPanel);
        bottomContainer.add(bottomButtonsPanel);

        frame.add(bottomContainer, BorderLayout.SOUTH);

        uploadButton1.addActionListener(e -> onUpload1());
        uploadButton2.addActionListener(e -> onUpload2());
        
        saveButton.addActionListener(e -> doSave());
        clearButton.addActionListener(e -> doClear());

        frame.setVisible(true);
    }

    private void onUpload1() {
        JFileChooser chooser=new JFileChooser();
        int res=chooser.showOpenDialog(frame);
        if (res!=JFileChooser.APPROVE_OPTION){
            return;
        }

        File file=chooser.getSelectedFile();
        String nama=file.getName().toLowerCase();

        try (Scanner sc=new Scanner(file, StandardCharsets.UTF_8)) {
            char[][] papan;

            papan=program.baca_masukan_txt(sc, nama);

            uploadButton1.setEnabled(false);
            uploadButton2.setEnabled(false);

            char[][] copy_papan=new char[papan.length][];
            for (int i=0; i<papan.length; i++){
                copy_papan[i]=papan[i].clone();
            }

            int n=papan.length;
            int[] letak=new int[n];

            program.konfigurasi_ditinjau=0;
            program.preview=null;
            program.previewCount=0;
            program.update_terakhir=System.nanoTime();

            status.setText("Memproses (Murni)");
            nama_file.setText(file.getName());
            ukuran.setText(String.format("%d x %d", n, n));
            waktu_komputasi.setText("-");
            jumlah_percobaan.setText("0");

            Timer timer=new Timer(500, ev -> {
                char[][] snap=program.preview;
                if (snap!=null) {
                    boardContainer.removeAll();
                    boardContainer.add(visualisasi_papan(copy_papan, snap), BorderLayout.CENTER);
                    boardContainer.revalidate();
                    boardContainer.repaint();

                    jumlah_percobaan.setText(String.valueOf(program.previewCount));
                }
            });
            timer.start();

            SwingWorker<char[][], Void> worker = new SwingWorker<>() {
                long mulai;

                @Override
                protected char[][] doInBackground() {
                    mulai=System.nanoTime();
                    return program.hasilkan_dan_cek_susunan(0, 0, n, letak, papan);
                }

                @Override
                protected void done() {
                    timer.stop();

                    try {
                        char[][] solusi=get();
                        long selesai=System.nanoTime();
                        double detik=(selesai-mulai)/1_000_000.0;

                        boardContainer.removeAll();
                        if (solusi==null) {
                            boardContainer.add(new JLabel("Tidak ada solusi", SwingConstants.CENTER), BorderLayout.CENTER);
                            status.setText("Tidak ada solusi");
                        }
                        else {
                            boardContainer.add(visualisasi_papan(copy_papan, solusi), BorderLayout.CENTER);
                            status.setText("Solusi ditemukan");
                        }
                        boardContainer.revalidate();
                        boardContainer.repaint();

                        waktu_komputasi.setText(String.format("%.6f ms", detik));
                        jumlah_percobaan.setText(String.valueOf(program.konfigurasi_ditinjau));

                        lastPapan=copy_papan;
                        lastSolusi=solusi;
                        lastInputFile=file;

                        Component c=(boardContainer.getComponentCount() > 0) ? boardContainer.getComponent(0) : null;
                        lastVisualPanel=(c instanceof JPanel) ? (JPanel) c : null;

                        saveButton.setEnabled(lastVisualPanel != null);
                        clearButton.setEnabled(true);

                    }
                    catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame,ex.toString(),"Error",JOptionPane.ERROR_MESSAGE);
                        status.setText("Error");
                    }
                }
            };

            worker.execute();
        } 
        catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frame,ex.getMessage(),"Masukan tidak valid",JOptionPane.ERROR_MESSAGE);
        } 
        catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,ex.toString(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpload2() {
        JFileChooser chooser=new JFileChooser();
        int res=chooser.showOpenDialog(frame);
        if (res!=JFileChooser.APPROVE_OPTION){
            return;
        }

        File file=chooser.getSelectedFile();

        String nama=file.getName().toLowerCase();

        try (Scanner sc=new Scanner(file, StandardCharsets.UTF_8)) {
            char[][] papan;

            papan=program.baca_masukan_txt(sc, nama);

            uploadButton1.setEnabled(false);
            uploadButton2.setEnabled(false);

            char[][] copy_papan = new char[papan.length][];
            for (int i=0; i<papan.length; i++){
                copy_papan[i] = papan[i].clone();
            }

            int n = papan.length;
            int[] letak = new int[n];

            programOptimal.konfigurasi_ditinjau = 0;
            programOptimal.preview = null;
            programOptimal.previewCount = 0;
            programOptimal.update_terakhir = System.nanoTime();

            status.setText("Memproses (Optimal)");
            nama_file.setText(file.getName());
            ukuran.setText(String.format("%d x %d", n, n));
            waktu_komputasi.setText("-");
            jumlah_percobaan.setText("0");

            Timer timer = new Timer(500, ev -> {
                char[][] snap = programOptimal.preview;
                if (snap != null) {
                    boardContainer.removeAll();
                    boardContainer.add(visualisasi_papan(copy_papan, snap), BorderLayout.CENTER);
                    boardContainer.revalidate();
                    boardContainer.repaint();

                    jumlah_percobaan.setText(String.valueOf(programOptimal.previewCount));
                }
            });
            timer.start();


            SwingWorker<char[][], Void> worker = new SwingWorker<>() {
                long mulai;

                @Override
                protected char[][] doInBackground() {
                    mulai=System.nanoTime();
                    return programOptimal.hasilkan_dan_cek_susunan(0, 0, n, letak, papan);
                }

                @Override
                protected void done() {
                    timer.stop();

                    try {
                        char[][] solusi=get();
                        long selesai=System.nanoTime();
                        double detik=(selesai-mulai)/1_000_000.0;

                        boardContainer.removeAll();
                        if (solusi==null) {
                            boardContainer.add(new JLabel("Tidak ada solusi", SwingConstants.CENTER), BorderLayout.CENTER);
                            status.setText("Tidak ada solusi");
                        }
                        else {
                            boardContainer.add(visualisasi_papan(copy_papan, solusi), BorderLayout.CENTER);
                            status.setText("Solusi ditemukan");
                        }
                        boardContainer.revalidate();
                        boardContainer.repaint();

                        waktu_komputasi.setText(String.format("%.6f ms", detik));
                        jumlah_percobaan.setText(String.valueOf(programOptimal.konfigurasi_ditinjau));

                        lastPapan=copy_papan;
                        lastSolusi=solusi;
                        lastInputFile=file;

                        Component c=(boardContainer.getComponentCount() > 0) ? boardContainer.getComponent(0) : null;
                        lastVisualPanel=(c instanceof JPanel) ? (JPanel) c : null;

                        saveButton.setEnabled(lastVisualPanel != null);
                        clearButton.setEnabled(true);

                    }
                    catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame,ex.toString(),"Error",JOptionPane.ERROR_MESSAGE);
                        status.setText("Error");
                    }
                }
            };

            worker.execute();

        } 
        catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frame,ex.getMessage(),"Masukan tidak valid",JOptionPane.ERROR_MESSAGE);
        } 
        catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,ex.toString(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    static char[] daftar_warna(char[][] papan) {
        ArrayList<Character> unik=new ArrayList<>();
        int n=papan.length;

        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                char ch=papan[i][j];
                if (!unik.contains(ch)) {
                    unik.add(ch);
                }
            }
        }

        char[] hasil=new char[unik.size()];
        for (int i=0; i<unik.size(); i++) {
            hasil[i]=unik.get(i);
        }

        return hasil;
    }

    static int warnai(char[] arr, char target) {
        for (int i=0; i<arr.length; i++) {
            if (arr[i]==target){
                return i;
            }
        }
        return -1; //ga akan kepakai
    }

    static JPanel visualisasi_papan(char[][] papan, char[][] solusi) {
        int n=papan.length;

        char[] warnaUnik=daftar_warna(papan);

        JPanel grid=new JPanel(new GridLayout(n,n,1,1));
        grid.setBackground(Color.DARK_GRAY);

        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                char ch=papan[i][j];

                JLabel cell=new JLabel("", SwingConstants.CENTER);
                cell.setOpaque(true);
                cell.setBorder(new LineBorder(Color.GRAY, 1));
                cell.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
                
                int idx=warnai(warnaUnik,ch);
                Color bg=warna[idx];
                cell.setBackground(bg);
        
                if (solusi[i][j]=='#') {
                    cell.setText("♛");
                    cell.setFont(new Font(Font.SANS_SERIF,Font.BOLD,36));
                }         

                grid.add(cell);
            }
        }

        return grid;
    }

    private void doClear() {
        boardContainer.removeAll();
        boardContainer.revalidate();
        boardContainer.repaint();

        nama_file.setText("-");
        ukuran.setText("-");
        waktu_komputasi.setText("-");
        jumlah_percobaan.setText("-");
        status.setText("Pilih file input untuk mulai");

        lastPapan=null;
        lastSolusi=null;
        lastVisualPanel=null;
        lastInputFile=null;

        uploadButton1.setEnabled(true);
        uploadButton2.setEnabled(true);
        saveButton.setEnabled(false);
        clearButton.setEnabled(false);
    }

    private void doSave() {
        if (lastVisualPanel==null || lastPapan==null || lastSolusi==null) {
            JOptionPane.showMessageDialog(frame, "Tidak ada solusi untuk disimpan", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser=new JFileChooser();
        chooser.setDialogTitle("Pilih folder untuk menyimpan");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int res=chooser.showSaveDialog(frame);
        if (res!=JFileChooser.APPROVE_OPTION) return;

        File dir=chooser.getSelectedFile();

        String baseName=(lastInputFile!=null) ? lastInputFile.getName() : "hasil";

        int dot=baseName.lastIndexOf('.');
        if (dot!=-1) baseName = baseName.substring(0, dot);

        File outPng=new File(dir, baseName + "_solusi_gambar.png");
        File outTxt=new File(dir, baseName + "_solusi_dokumen.txt");

        try {
            program.simpan_gambar_solusi(lastVisualPanel, outPng);
            program.simpan_solusi_txt(lastPapan, lastSolusi, outTxt);

            JOptionPane.showMessageDialog(frame,
                    "Tersimpan:\n- " + outPng.getName() + "\n- " + outTxt.getName(),
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);

            doClear();

        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}