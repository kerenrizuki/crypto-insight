# Product Requirements Document (PRD): Aplikasi Android Screening Token Kripto

**Nama Produk:** Crypto Insight Screener (CIS)
**Versi:** 1.0
**Tanggal:** 22 Mei 2026
**Penulis:** Manus AI

## 1. Pendahuluan

Dokumen ini menguraikan persyaratan produk untuk pengembangan aplikasi Android "Crypto Insight Screener" (CIS). Aplikasi ini bertujuan untuk menyediakan alat *screening* token kripto yang canggih bagi investor dan analis, dengan fokus pada data on-chain real-time dan metrik fundamental yang diperbarui setiap jam. CIS akan memungkinkan pengguna untuk mengidentifikasi token yang berpotensi *undervalued* berdasarkan profitabilitas protokol, aktivitas jaringan, dan rasio valuasi, mirip dengan analisis fundamental di pasar saham tradisional.

## 2. Tujuan Produk

*   Memberdayakan investor kripto dengan akses mudah ke data on-chain dan metrik fundamental yang relevan.
*   Memungkinkan identifikasi token kripto yang *undervalued* secara efisien melalui kriteria *screening* yang dapat disesuaikan.
*   Menyediakan pembaruan data yang sering (setiap jam) untuk mencerminkan dinamika pasar kripto yang cepat.
*   Menawarkan antarmuka pengguna yang intuitif dan visualisasi data yang jelas untuk pengambilan keputusan yang lebih baik.

## 3. Target Pengguna

*   Investor kripto individu dan institusional.
*   Trader yang mencari peluang jangka menengah hingga panjang.
*   Analis pasar kripto.
*   Pengguna yang memiliki pemahaman dasar tentang metrik on-chain dan valuasi kripto.

## 4. Fitur Utama

### 4.1. Dashboard Screening Kustomisasi
*   **Daftar Token:** Menampilkan daftar token kripto yang dapat difilter dan diurutkan.
*   **Metrik Tampilan:** Pengguna dapat memilih metrik yang ingin ditampilkan di dashboard, seperti:
    *   Market Cap (Kapitalisasi Pasar)
    *   Fully Diluted Valuation (FDV)
    *   Annualized Revenue (Pendapatan Tahunan Protokol)
    *   P/S Ratio (FDV) (Price-to-Sales Ratio berdasarkan FDV)
    *   Total Value Locked (TVL) (khusus DeFi)
    *   Perubahan Revenue (24 jam, 7 hari, 30 hari)
    *   Jumlah Alamat Aktif (Daily Active Users)
    *   Volume Transaksi (24 jam)
*   **Filter & Sorting:** Kemampuan untuk memfilter token berdasarkan sektor (DEX, Lending, L1/L2, dll.), P/S Ratio, pertumbuhan pendapatan, dan metrik lainnya. Pengguna juga dapat mengurutkan daftar berdasarkan metrik apa pun.

### 4.2. Halaman Detail Token
*   **Ringkasan:** Informasi dasar token (harga, market cap, FDV, volume).
*   **Metrik On-Chain:** Grafik dan data historis untuk alamat aktif, transaksi, TVL, dll.
*   **Metrik Finansial Protokol:** Grafik dan data historis untuk Annualized Revenue, P/S Ratio, Profit, dll.
*   **Tokenomics:** Informasi tentang suplai token, jadwal *unlock*, mekanisme *burn*, dan *fee sharing*.
*   **Deskripsi Proyek:** Ringkasan singkat tentang tujuan dan fungsi proyek.

### 4.3. Pembaruan Data Real-time (Setiap Jam)
*   Data metrik on-chain dan finansial akan diperbarui secara otomatis setiap 1 jam.
*   Indikator visual akan menunjukkan kapan data terakhir diperbarui.

### 4.4. Notifikasi (Opsional untuk Versi Awal)
*   Pengguna dapat mengatur notifikasi untuk token tertentu jika P/S Ratio turun di bawah ambang batas tertentu atau jika ada perubahan signifikan pada metrik kunci lainnya.

### 4.5. Daftar Pantau (Watchlist)
*   Pengguna dapat menambahkan token favorit ke daftar pantau pribadi untuk pemantauan yang lebih mudah.

## 5. Sumber Data & Integrasi API

Untuk memastikan data yang akurat dan diperbarui secara berkala, aplikasi akan mengintegrasikan dengan API dari penyedia data on-chain dan finansial terkemuka:

*   **Token Terminal API:** Akan digunakan sebagai sumber utama untuk metrik finansial protokol seperti Annualized Revenue, P/S Ratio (FDV), dan data terkait profitabilitas. Token Terminal menyediakan data terstandardisasi yang penting untuk analisis fundamental [1].
    *   **Catatan:** Token Terminal API memerlukan kunci API dan mungkin memiliki biaya berlangganan. Batas permintaan adalah 1000 permintaan per menit.
*   **DefiLlama API:** Akan digunakan untuk data Total Value Locked (TVL), fees, volume, dan informasi protokol DeFi lainnya. DefiLlama menawarkan API gratis dan berbayar dengan batasan yang berbeda [2].
*   **API Data Harga & Market Cap:** Integrasi dengan API seperti CoinGecko API atau CoinMarketCap API untuk data harga token, market cap, dan volume perdagangan dasar.
*   **Strategi Pembaruan Data:**
    *   Aplikasi akan menggunakan *background service* di Android untuk melakukan *fetching* data dari API setiap jam.
    *   Data yang di-*fetch* akan disimpan secara lokal (misalnya dalam database SQLite atau Room) untuk akses cepat dan mengurangi ketergantungan pada koneksi internet yang konstan.
    *   Mekanisme *caching* akan diimplementasikan untuk mengelola data dan meminimalkan panggilan API yang berlebihan.

## 6. Arsitektur Teknis (High-Level)

*   **Platform:** Android Native (Kotlin direkomendasikan).
*   **UI Framework:** Jetpack Compose atau XML Layouts.
*   **Manajemen State:** Jetpack ViewModel, LiveData/Flow.
*   **Networking:** Retrofit untuk panggilan API.
*   **Database Lokal:** Room Persistence Library untuk caching data.
*   **Background Processing:** WorkManager untuk penjadwalan pembaruan data setiap jam.
*   **Grafik Data:** Library grafik Android (misalnya MPAndroidChart atau Compose Chart Library) untuk visualisasi metrik.
*   **Backend (Opsional, untuk Skalabilitas):** Jika volume pengguna dan data menjadi sangat besar, atau jika fitur notifikasi yang kompleks diperlukan, sebuah *backend service* (misalnya di AWS Lambda, Google Cloud Functions) dapat dikembangkan untuk mengagregasi data dari berbagai API, melakukan perhitungan P/S Ratio, dan mengelola notifikasi, sehingga mengurangi beban pada aplikasi klien dan biaya API.

## 7. Desain UI/UX (High-Level)

*   **Desain Bersih & Minimalis:** Fokus pada keterbacaan data dan navigasi yang mudah.
*   **Visualisasi Data:** Penggunaan grafik garis, batang, dan tabel yang interaktif untuk menyajikan tren metrik.
*   **Responsif:** Tata letak yang adaptif untuk berbagai ukuran layar perangkat Android.
*   **Mode Gelap/Terang:** Pilihan tema untuk kenyamanan pengguna.

## 8. Monetisasi (Pertimbangan)

*   **Model Freemium:** Fitur dasar gratis, fitur premium (misalnya notifikasi lanjutan, akses ke metrik yang lebih dalam, tanpa iklan) berbayar.
*   **Langganan API:** Pengguna mungkin perlu berlangganan API pihak ketiga (misalnya Token Terminal) secara terpisah untuk mengakses data premium.

## 9. Risiko & Tantangan

*   **Biaya API:** Integrasi dengan API berbayar seperti Token Terminal akan menimbulkan biaya operasional yang perlu dipertimbangkan dalam model bisnis.
*   **Rate Limiting:** Perlu manajemen *rate limiting* yang cermat untuk menghindari pemblokiran API.
*   **Kompleksitas Data On-Chain:** Data on-chain bisa sangat besar dan kompleks; perlu strategi efisien untuk memproses dan menyajikannya di perangkat mobile.
*   **Perubahan API:** API pihak ketiga dapat berubah, memerlukan pemeliharaan dan pembaruan aplikasi secara berkala.
*   **Keterbatasan Perangkat Mobile:** Kinerja dan konsumsi baterai perlu dioptimalkan untuk pembaruan data setiap jam.

## 10. Metrik Keberhasilan

*   Jumlah unduhan dan pengguna aktif bulanan.
*   Tingkat retensi pengguna.
*   Tingkat konversi ke fitur premium (jika ada model monetisasi).
*   Umpan balik positif dari pengguna mengenai akurasi dan kegunaan data.

---

### Referensi

[1] Token Terminal. "API Reference." https://tokenterminal.com/docs/api-reference/introduction
[2] DefiLlama. "API Docs." https://api-docs.defillama.com/
