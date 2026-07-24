# Hướng Dẫn & Bản Đặc Tả Chức Năng Theo Use Case (JobSearchApp)

> Dựa trên mẫu bảng đặc tả trong hình ảnh bạn gửi, dưới đây là **Khung mẫu chuẩn** và **Bản đặc tả mẫu chi tiết** cho các Use Case chính trong dự án **JobSearchApp**.

---

## 📋 I. Khung Mẫu Bảng Đặc Tả (Template)

| Mục | Nội dung mô tả |
|---|---|
| **Tên chức năng** | Tên ca sử dụng (ví dụ: Nộp hồ sơ ứng tuyển) |
| **ID ca sử dụng** | Mã định danh duy nhất (ví dụ: UC-01, UC-02...) |
| **Mức độ quan trọng** | Cao / Trung bình / Thấp |
| **Tác nhân chính** | Ứng viên / Nhà tuyển dụng / Admin |
| **Loại ca sử dụng** | Ca sử dụng chính / Ca sử dụng phụ |
| **Tóm lược** | Tóm tắt mục đích của chức năng |
| **Trigger** | Sự kiện bắt đầu ca sử dụng (ví dụ: Người dùng nhấn nút X) |
| **Tiền điều kiện** | Điều kiện cần có trước khi thực hiện (ví dụ: Đã đăng nhập) |
| **Luồng sự kiện chính** | Các bước thực hiện theo kịch bản chuẩn thành công (1, 2, 3...) |
| **Luồng sự kiện phụ** | Các luồng phụ bổ sung (ví dụ: AF1 - Lưu việc làm) |
| **Luồng sự kiện ngoại lệ** | Các tình huống lỗi xảy ra và xử lý (ví dụ: EF1 - Sai mật khẩu) |
| **Hậu điều kiện** | Trạng thái hệ thống sau khi hoàn thành |

---

## 📑 II. Bản Đặc Tả Chi Tiết Các Chức Năng Trong Dự Án

### 1. Bảng đặc tả UC-01: Đăng nhập / Đăng xuất tài khoản

**Chức năng: Đăng nhập / Đăng xuất tài khoản**

| Mục | Nội dung đặc tả |
|---|---|
| **Tên chức năng** | Đăng nhập / Đăng xuất tài khoản |
| **ID ca sử dụng** | UC-01 |
| **Mức độ quan trọng** | Cao |
| **Tác nhân chính** | Ứng viên / Nhà tuyển dụng |
| **Loại ca sử dụng** | Ca sử dụng chính |
| **Tóm lược** | Cho phép người dùng xác thực vào hệ thống theo đúng vai trò (Ứng viên hoặc NTD) và đăng xuất khi hoàn thành phiên làm việc |
| **Trigger** | Người dùng mở ứng dụng hoặc nhấn nút "Đăng nhập" |
| **Tiền điều kiện** | Tài khoản đã được đăng ký trên hệ thống Firebase |
| **Luồng sự kiện chính** | 1. Người dùng nhập Email và Mật khẩu.<br>2. Người dùng nhấn nút **"Đăng nhập"**.<br>3. Hệ thống gửi yêu cầu xác thực tới Firebase Auth.<br>4. Hệ thống xác thực thành công, lấy thông tin User & Role từ Firestore.<br>5. Lưu session vào `SessionManager` và điều hướng về trang tương ứng với Role (MainActivity hoặc EmployerActivity). |
| **Luồng sự kiện phụ** | **AF1 - Đăng xuất**<br>- Người dùng chọn nút "Đăng xuất" tại trang cá nhân.<br>- Hệ thống xóa phiên làm việc trong `SessionManager`.<br>- Hệ thống chuyển về màn hình Đăng nhập. |
| **Luồng sự kiện ngoại lệ** | **EF1 - Sai thông tin đăng nhập**<br>- Xảy ra tại bước 3 khi Email hoặc Mật khẩu không chính xác.<br>- Hệ thống hiển thị Toast/Thông báo: *"Sai email hoặc mật khẩu"*. |
| **Hậu điều kiện** | Người dùng đăng nhập/đăng xuất thành công khỏi hệ thống. |

---

### 2. Bảng đặc tả UC-02: Tìm kiếm và Lọc việc làm

**Chức năng: Tìm kiếm và Lọc việc làm**

| Mục | Nội dung đặc tả |
|---|---|
| **Tên chức năng** | Tìm kiếm và Lọc việc làm |
| **ID ca sử dụng** | UC-02 |
| **Mức độ quan trọng** | Cao |
| **Tác nhân chính** | Ứng viên |
| **Loại ca sử dụng** | Ca sử dụng chính |
| **Tóm lược** | Cho phép ứng viên tìm kiếm công việc theo từ khóa, lọc theo danh mục, loại hình công việc và sắp xếp kết quả |
| **Trigger** | Ứng viên nhấn vào ô tìm kiếm hoặc chọn danh mục tại Trang chủ |
| **Tiền điều kiện** | Hệ thống đã có danh sách các bài đăng công việc |
| **Luồng sự kiện chính** | 1. Ứng viên nhập từ khóa tìm kiếm vào ô tìm kiếm.<br>2. Ứng viên chọn các tiêu chí lọc (Danh mục, Loại hình Full-time/Remote...).<br>3. Ứng viên chọn kiểu sắp xếp (Mới nhất/Cũ nhất).<br>4. Hệ thống lọc danh sách Job phù hợp từ dữ liệu.<br>5. Hệ thống hiển thị danh sách kết quả lên màn hình `SearchFragment` và cập nhật số lượng kết quả. |
| **Luồng sự kiện phụ** | **AF1 - Tìm kiếm từ danh mục trang chủ**<br>- Ứng viên nhấn vào một biểu tượng Danh mục tại HomeFragment.<br>- Hệ thống tự động chuyển sang trang tìm kiếm với từ khóa danh mục đã chọn. |
| **Luồng sự kiện ngoại lệ** | **EF1 - Không tìm thấy kết quả**<br>- Xảy ra khi không có công việc nào thỏa mãn tiêu chí lọc.<br>- Hệ thống hiển thị thông báo *"Không tìm thấy công việc phù hợp"*. |
| **Hậu điều kiện** | Danh sách công việc phù hợp được hiển thị cho ứng viên. |

---

### 3. Bảng đặc tả UC-03: Xem chi tiết và Nộp hồ sơ ứng tuyển

**Chức năng: Xem chi tiết và Nộp hồ sơ ứng tuyển**

| Mục | Nội dung đặc tả |
|---|---|
| **Tên chức năng** | Xem chi tiết và Nộp hồ sơ ứng tuyển |
| **ID ca sử dụng** | UC-03 |
| **Mức độ quan trọng** | Cao |
| **Tác nhân chính** | Ứng viên |
| **Loại ca sử dụng** | Ca sử dụng chính |
| **Tóm lược** | Cho phép ứng viên xem thông tin chi tiết một công việc, thực hiện lưu việc làm hoặc nộp hồ sơ ứng tuyển |
| **Trigger** | Ứng viên nhấn vào một item công việc bất kỳ |
| **Tiền điều kiện** | Công việc còn tồn tại trên hệ thống |
| **Luồng sự kiện chính** | 1. Ứng viên nhấn chọn một công việc.<br>2. Hệ thống chuyển sang màn hình `JobDetailActivity` và hiển thị chi tiết (Tiêu đề, Lương, Mô tả, Deadline...).<br>3. Ứng viên nhấn nút **"Nộp hồ sơ ngay"**.<br>4. Hệ thống kiểm tra điều kiện đăng nhập.<br>5. Hệ thống tạo bản ghi `Application` mới và lưu vào Firestore collection `applications`.<br>6. Hệ thống hiển thị thông báo *"Đã gửi yêu cầu ứng tuyển thành công!"* và đóng màn hình. |
| **Luồng sự kiện phụ** | **AF1 - Lưu / Bỏ lưu công việc**<br>- Ứng viên nhấn icon Trái tim (Heart).<br>- Hệ thống thêm/xóa bản ghi trong Firestore `saved_jobs` và cập nhật giao diện. |
| **Luồng sự kiện ngoại lệ** | **EF1 - Chưa đăng nhập**<br>- Xảy ra tại bước 4 khi ứng viên chưa đăng nhập.<br>- Hệ thống thông báo *"Vui lòng đăng nhập để ứng tuyển"*. |
| **Hậu điều kiện** | Đơn ứng tuyển của ứng viên được ghi nhận vào hệ thống. |

---

### 4. Bảng đặc tả UC-04: Đăng tin tuyển dụng (Nhà tuyển dụng)

**Chức năng: Đăng tin tuyển dụng**

| Mục | Nội dung đặc tả |
|---|---|
| **Tên chức năng** | Đăng tin tuyển dụng |
| **ID ca sử dụng** | UC-04 |
| **Mức độ quan trọng** | Cao |
| **Tác nhân chính** | Nhà tuyển dụng |
| **Loại ca sử dụng** | Ca sử dụng chính |
| **Tóm lược** | Cho phép nhà tuyển dụng tạo và đăng bài tuyển dụng mới lên hệ thống |
| **Trigger** | Nhà tuyển dụng nhấn nút "Đăng tin mới" tại màn hình quản lý |
| **Tiền điều kiện** | Nhà tuyển dụng đã đăng nhập thành công với vai trò "employer" |
| **Luồng sự kiện chính** | 1. Nhà tuyển dụng mở màn hình `PostJobActivity`.<br>2. Điền đầy đủ thông tin: Tiêu đề, Mô tả, Yêu cầu, Mức lương, Địa điểm, Loại hình, Deadline.<br>3. Nhấn nút **"Đăng tin"**.<br>4. Hệ thống kiểm tra dữ liệu đầu vào.<br>5. Hệ thống tạo đối tượng `Job` mới và lưu vào Firestore collection `jobs`.<br>6. Hệ thống thông báo *"Đăng tin thành công!"* và quay về màn hình quản lý. |
| **Luồng sự kiện phụ** | **AF1 - Đặt thời gian deadline nộp hồ sơ**<br>- Chọn ngày hết hạn từ bộ chọn ngày (DatePicker). |
| **Luồng sự kiện ngoại lệ** | **EF1 - Thiếu thông tin bắt buộc**<br>- Xảy ra tại bước 4 khi để trống Tiêu đề, Mức lương hoặc Địa điểm.<br>- Hệ thống hiển thị lỗi báo đỏ tại các ô chưa điền. |
| **Hậu điều kiện** | Bài đăng tuyển dụng mới xuất hiện trên trang chủ của ứng viên. |

---

### 5. Bảng đặc tả UC-05: Quản lý ứng viên và Duyệt đơn ứng tuyển

**Chức năng: Quản lý ứng viên và Duyệt đơn ứng tuyển**

| Mục | Nội dung đặc tả |
|---|---|
| **Tên chức năng** | Quản lý ứng viên và Duyệt đơn ứng tuyển |
| **ID ca sử dụng** | UC-05 |
| **Mức độ quan trọng** | Cao |
| **Tác nhân chính** | Nhà tuyển dụng |
| **Loại ca sử dụng** | Ca sử dụng chính |
| **Tóm lược** | Cho phép nhà tuyển dụng xem danh sách ứng viên nộp hồ sơ và cập nhật trạng thái đơn (Đang xem xét, Phỏng vấn, Từ chối) |
| **Trigger** | Nhà tuyển dụng chọn "Xem ứng viên" tại một công việc đã đăng |
| **Tiền điều kiện** | Đã có ứng viên nộp hồ sơ vào công việc đó |
| **Luồng sự kiện chính** | 1. Nhà tuyển dụng mở `ViewApplicantsActivity`.<br>2. Hệ thống tải danh sách `Application` từ Firestore.<br>3. Hiển thị danh sách ứng viên (Tên, Ngày nộp, Trạng thái hiện tại).<br>4. Nhà tuyển dụng chọn ứng viên và đổi trạng thái (ví dụ: `accepted` / `rejected`).<br>5. Hệ thống cập nhật trường `status` trên Firestore và làm mới danh sách. |
| **Luồng sự kiện phụ** | **AF1 - Xem file CV ứng viên**<br>- Nhấn nút "Xem CV" để mở đường dẫn CV của ứng viên. |
| **Luồng sự kiện ngoại lệ** | **EF1 - Chưa có ứng viên**<br>- Xảy ra khi danh sách hồ sơ trống.<br>- Hệ thống hiển thị thông báo *"Chưa có ứng viên nào nộp hồ sơ"*. |
| **Hậu điều kiện** | Trạng thái ứng tuyển được cập nhật và ứng viên có thể theo dõi. |
