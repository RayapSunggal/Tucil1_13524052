import java.util.Scanner;

public class programOptimal {

    static volatile char[][] preview=null;
    static volatile long previewCount=0;

    static long konfigurasi_ditinjau;
    static long update_terakhir=0;
    static final long interval=5_000L;

    public static boolean cek_Quuens(int[] letak_bidak, int ukuran, char[][] masukan){
        konfigurasi_ditinjau++;
        for(int k=0; k<ukuran-1; k++) {
            int baris_bidak1=letak_bidak[k]/ukuran;
            int kolom_bidak1=letak_bidak[k]%ukuran;
            for (int l=k+1; l<ukuran; l++) {
                int baris_bidak2=letak_bidak[l]/ukuran;
                int kolom_bidak2=letak_bidak[l]%ukuran;

                if (masukan[baris_bidak1][kolom_bidak1]==masukan[baris_bidak2][kolom_bidak2] ||  baris_bidak1==baris_bidak2 || kolom_bidak1==kolom_bidak2 || (Math.abs(kolom_bidak1-kolom_bidak2)==1 && Math.abs(baris_bidak1-baris_bidak2)==1)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean petak_sudah_ada (char[][] papan, int[] letak_bidak, int idx, int n, int jumlah_bidak_tersusun){
        for (int i=0; i<jumlah_bidak_tersusun; i++){
            if (letak_bidak[i]%n==idx%n || papan[letak_bidak[i]/n][letak_bidak[i]%n]==papan[idx/n][idx%n]) return true;
        }
        return false;
    }

    public static char[][] hasilkan_dan_cek_susunan(int mulai, int jumlah_bidak_tersusun, int ukuran, int[] letak_bidak, char[][] masukan){
        if (jumlah_bidak_tersusun==ukuran) {


            long now=System.nanoTime();
            if (now-update_terakhir>=interval) {

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

            if (cek_Quuens(letak_bidak, ukuran, masukan)) {
                char[][] a=masukan;

                for (int i=0; i<ukuran; i++) {
                    a[letak_bidak[i]/ukuran][letak_bidak[i]%ukuran]='#';
                }

                return a;
            }
            return null;
        }
       
        for (int i=mulai; i<=(ukuran*ukuran)-ukuran+jumlah_bidak_tersusun; i++) { //misal di bidang 2*2 kalau masih 0 yang tersusun dia cuman bisa susun sampai kotak ketiga(2), kalau udah 1 tersusun bisa sampai 3 
            if(i>=ukuran*jumlah_bidak_tersusun && i<jumlah_bidak_tersusun*ukuran+ukuran && !petak_sudah_ada(masukan, letak_bidak, i, ukuran, jumlah_bidak_tersusun)) { // optimasi
                letak_bidak[jumlah_bidak_tersusun]=i;
                char[][] hasil=hasilkan_dan_cek_susunan(i+1, jumlah_bidak_tersusun+1, ukuran, letak_bidak, masukan); //susun bidak berikutnya
                if (hasil!=null){
                    return hasil;
                }
            }
            
        }
        return null;
    }
}