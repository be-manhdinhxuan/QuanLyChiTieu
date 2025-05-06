# Quản Lý Chi Tiêu - Expense Manager App

![App Logo](app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.webp)

## Giới thiệu

Quản Lý Chi Tiêu là ứng dụng Android giúp người dùng theo dõi, quản lý chi tiêu và thu nhập cá nhân một cách hiệu quả. Ứng dụng được phát triển với kiến trúc Clean Architecture, sử dụng Firebase làm backend.

## Tính năng chính

- **Theo dõi chi tiêu**: Ghi lại các khoản chi tiêu hàng ngày với đầy đủ thông tin
- **Lịch chi tiêu**: Xem lại các khoản chi tiêu theo ngày/tháng/năm
- **Phân tích**: Biểu đồ trực quan hóa chi tiêu theo danh mục
- **Ngân sách**: Thiết lập và theo dõi ngân sách hàng tháng
- **Đa ngôn ngữ**: Hỗ trợ tiếng Việt và tiếng Anh
- **Đồng bộ dữ liệu**: Lưu trữ an toàn trên Firebase
- **Chia sẻ**: Chia sẻ thông tin chi tiêu với bạn bè

## Công nghệ sử dụng

- **Ngôn ngữ**: Java
- **Backend**: Firebase (Authentication, Firestore), Cloudinary (lưu trữ ảnh)
- **Kiến trúc**: Clean Architecture (Presentation, Domain, Data)
- **Thư viện**: 
  - MPAndroidChart (biểu đồ)
  - Glide (xử lý ảnh)
  - Material Components (UI)
  - ViewPager2, TabLayout (UI)
  - Retrofit, OkHttp (network)

## Cài đặt

### Yêu cầu hệ thống
- Android Studio Hedgehog (2023.1.1) trở lên
- JDK 8 trở lên
- Android SDK 24 trở lên (Android 7.0+)
- Gradle 8.0+

### Các bước cài đặt
1. Clone repository:
   ```
   git clone https://github.com/be-manhdinhxuan/QuanLyChiTieu.git
   ```

2. Mở project trong Android Studio

3. Tạo project Firebase:
   - Truy cập [Firebase Console](https://console.firebase.google.com/)
   - Tạo project mới
   - Thêm ứng dụng Android với package name `com.example.quanlychitieu`
   - Tải file `google-services.json` và đặt vào thư mục `app/`
   - Bật các dịch vụ: Authentication, Firestore

4. Build và chạy ứng dụng:
   ```
   ./gradlew assembleDebug
   ```

## Cấu trúc project

```
app/
├── src/main/
│   ├── java/com/example/quanlychitieu/
│   │   ├── core/            # Core utilities
│   │   ├── data/            # Data layer
│   │   ├── domain/          # Domain layer
│   │   │   ├── adapter/     # Adapters
│   │   │   ├── model/       # Model classes
│   │   │   └── usecase/     # Use cases
│   │   └── presentation/    # Presentation layer
│   │   │    └── features/    # App features
│   │   └── views/           # Custom views
│   ├── res/                 # Resources
│   └── assets/              # Assets (languages, etc.)
└── build.gradle.kts         # App level build file
```

## Đóng góp

Nếu bạn muốn đóng góp vào dự án, vui lòng:
1. Fork repository
2. Tạo branch mới (`git checkout -b feature/amazing-feature`)
3. Commit thay đổi (`git commit -m 'Add some amazing feature'`)
4. Push lên branch (`git push origin feature/amazing-feature`)
5. Tạo Pull Request

## Liên hệ

Đinh Xuân Mạnh - [Facebook](https://www.facebook.com/XuanManh.Coder) - [Email](mailto:manhbeo.it8@gmail.com)

## Giấy phép

Dự án được phân phối dưới giấy phép MIT. Xem `LICENSE` để biết thêm thông tin.