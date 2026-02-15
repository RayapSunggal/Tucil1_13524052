import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class program {

    static volatile char[][] preview=null;
    static volatile long previewCount=0;

    static long konfigurasi_ditinjau;
    static long update_terakhir=0;
    static final long interval=5_000L;

    static char[][] baca_masukan_txt(Scanner sc, String nama) {

        if (!nama.endsWith(".txt")){
            if (nama.endsWith(".png") || nama.endsWith(".jgeg") || nama.endsWith(".jpg")){
                throw new IllegalArgumentException("Program tidak bisa membaca gambar sayangnya");
            }
            else{
                throw new IllegalArgumentException("Masukan tidak valid");
            }
        }

        ArrayList<String> bacaan = new ArrayList<>();

        while (sc.hasNextLine()) {
            String baris=sc.nextLine().trim().replaceAll("\\s+", "");
            bacaan.add(baris);
        }

        if (bacaan.isEmpty()){
            throw new IllegalArgumentException("Masukan tidak valid");
        }

        int n=bacaan.size();

        for (int i=0;i<n;i++) {
            if (bacaan.get(i).length()!=n) {
                throw new IllegalArgumentException("Masukan tidak valid");
            }
        }

        char[][] papan = new char[n][n];
        for (int r=0; r<n; r++) {
            papan[r]=bacaan.get(r).toCharArray();
        }

        Set<Character> jumlah_warna = new HashSet<>();

        for (int i=0; i<papan.length; i++) {
            for (int j=0; j<papan[i].length; j++) {
                jumlah_warna.add(papan[i][j]);
            }
        }


        if (jumlah_warna.size()!=papan.length) {
            throw new IllegalArgumentException("Warna dan ukuran papan tidak sesuai");
        }

        return papan;
    }

    public static boolean cek_Quuens(int[] letak_bidak, int ukuran, char[][] masukan){
        konfigurasi_ditinjau++;
        for(int k=0; k<ukuran-1; k++){
            int baris_bidak1=letak_bidak[k]/ukuran;
            int kolom_bidak1=letak_bidak[k]%ukuran;
            for (int l=k+1; l<ukuran; l++){
                int baris_bidak2=letak_bidak[l]/ukuran;
                int kolom_bidak2=letak_bidak[l]%ukuran;

                if (masukan[baris_bidak1][kolom_bidak1]==masukan[baris_bidak2][kolom_bidak2] ||  baris_bidak1==baris_bidak2 || kolom_bidak1==kolom_bidak2 || (Math.abs(kolom_bidak1-kolom_bidak2)==1 && Math.abs(baris_bidak1-baris_bidak2)==1)){
                    return false;
                }
            }
        }
        
        return true;
    }

    public static char[][] hasilkan_dan_cek_susunan(int mulai, int jumlah_bidak_tersusun, int ukuran, int[] letak_bidak, char[][] masukan){
        if (jumlah_bidak_tersusun==ukuran){

            long now=System.nanoTime();
            if (now-update_terakhir>=interval){

                char[][] calon=new char[masukan.length][];

                for (int i=0; i<masukan.length; i++) {
                    calon[i]=masukan[i].clone();
                }

                for (int i=0; i<ukuran; i++) {
                    calon[letak_bidak[i]/ukuran][letak_bidak[i]%ukuran]='#';
                }

                preview=calon;
                previewCount=konfigurasi_ditinjau;
                update_terakhir=now;
            }

            if (cek_Quuens(letak_bidak, ukuran, masukan)){
                char[][] a=masukan;

                for (int i=0; i<ukuran; i++){
                    a[letak_bidak[i]/ukuran][letak_bidak[i]%ukuran]='#';
                }

                return a;
            }
            return null;
        }
       
        for (int i=mulai; i<=(ukuran*ukuran)-ukuran+jumlah_bidak_tersusun; i++){ //misal di bidang 2*2 kalau masih 0 yang tersusun dia cuman bisa susun sampai kotak ketiga(2), kalau udah 1 tersusun bisa sampai 3 
            letak_bidak[jumlah_bidak_tersusun]=i;
            char[][] hasil=hasilkan_dan_cek_susunan(i+1, jumlah_bidak_tersusun+1, ukuran, letak_bidak, masukan); //susun bidak berikutnya
            if (hasil!=null){
                return hasil;
            }    
        }
        return null;
    }

    public static void simpan_gambar_solusi(Component comp, File outPng) throws Exception {
        int w=comp.getWidth();
        int h=comp.getHeight();

        if (w<=0 || h<=0) {
            w=comp.getPreferredSize().width;
            h=comp.getPreferredSize().height;
            if (w<=0) w=800;
            if (h<=0) h=800;
            comp.setSize(w, h);
            comp.doLayout();
        }

        BufferedImage img=new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2=img.createGraphics();
        comp.paint(g2);
        g2.dispose();

        ImageIO.write(img, "png", outPng);
    }

    public static void simpan_solusi_txt(char[][] papan, char[][] solusi, File outTxt) throws Exception {
        try (PrintWriter pw=new PrintWriter(outTxt, StandardCharsets.UTF_8)) {
            int n=papan.length;
            pw.println("Ukuran: " + n + " x " + n);
            pw.println();

            pw.println("Konfigurasi Awal:");
            for (int i=0; i<n; i++) {
                pw.println(new String(papan[i]));
            }
            pw.println();

            pw.println("Solusi (# = Queen):");
            for (int i=0; i<n; i++) {
                pw.println(new String(solusi[i]));
            }
        }
    }

}
