# Ekran Yansıt (Android Auto)

Telefon ekranını Android Auto'lu araç ekranına yansıtan deneme amaçlı uygulama.

## Kurulum
1. Android Studio'da bu klasörü aç (File > Open). Gradle senkronizasyonunu bekle.
2. Telefonu USB ile bağla, Run ile yükle (veya Build > Build APK ile APK al).

## Android Auto ayarları (bir kere)
1. Telefon > Android Auto ayarları > en alttaki "Sürüm" yazısına 10 kez dokun, geliştirici modunu aç.
2. Sağ üst ⋮ > "Geliştirici ayarları" > "Bilinmeyen kaynaklar"ı aç.

## Kullanım
1. Telefonda uygulamayı aç, "Yansıtmayı Başlat"a bas, ekran yakalama iznini ver.
2. Telefonu araca bağla. Araç ekranında uygulama listesinden "Ekran Yansıt"ı aç.
3. Videoyu telefonda yatay modda oynat.

## Bilinen sınırlar
- Netflix, Disney+ gibi DRM korumalı uygulamalar yansıtmada siyah görünür.
- Google Play'e yüklenemez (video yansıtma politikaya aykırı); sadece yan yükleme.
- Android Auto güncellemeleri çalışmayı bozabilir.
- Ses telefondan gelir; araç hoparlörü için Bluetooth/USB ses ayrıca bağlı olmalı.
- Sadece park halinde kullan. Sürüş sırasında video izlemek tehlikeli ve çoğu yerde yasaktır.
- Kod burada derlenip test edilemedi (bu ortamda Android SDK ve internet yok). Derleme hatası çıkarsa hata mesajını yaz, düzeltelim.
