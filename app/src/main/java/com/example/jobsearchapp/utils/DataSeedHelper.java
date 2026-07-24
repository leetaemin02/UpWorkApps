package com.example.jobsearchapp.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.jobsearchapp.data.models.Job;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataSeedHelper {

    public static void pushMockJobsToFirebase(Context context) {
        String currentUserId = new SessionManager(context).getUserId();
        if (currentUserId.isEmpty()) {
            Toast.makeText(context, "Vui lòng đăng nhập trước khi thêm dữ liệu mẫu", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        List<Job> jobs = new ArrayList<>();
        
        // Dữ liệu từ JSON của bạn - Sử dụng currentUserId làm chủ sở hữu bài đăng
        jobs.add(createJob("JOB001", "COMP001", "Java Android Developer", "Phát triển và bảo trì ứng dụng Android bằng Java...", "Biết Java, Android Studio, Firebase, Git.", "Thưởng dự án, BHXH, du lịch hằng năm.", 12000000, 18000000, "TP. Hồ Chí Minh", "Toàn thời gian", "IT", "1 năm", "2026-08-30T23:59:59", "2026-07-08T09:00:00", 145, 3, currentUserId));
        jobs.add(createJob("JOB002", "COMP002", "Frontend Developer", "Phát triển giao diện web ReactJS.", "ReactJS, HTML, CSS, JavaScript.", "Laptop, thưởng tháng 13.", 14000000, 22000000, "Đà Nẵng", "Toàn thời gian", "IT", "2 năm", "2026-08-20T23:59:59", "2026-07-07T08:00:00", 210, 2, currentUserId));
        jobs.add(createJob("JOB003", "COMP003", "Backend Java Developer", "Xây dựng REST API bằng Spring Boot.", "Java, Spring Boot, MySQL.", "Hybrid, thưởng KPI.", 18000000, 30000000, "Hà Nội", "Toàn thời gian", "IT", "2 năm", "2026-08-25T23:59:59", "2026-07-06T10:30:00", 320, 4, currentUserId));
        jobs.add(createJob("JOB004", "COMP004", "UI/UX Designer", "Thiết kế giao diện Mobile App.", "Figma, Adobe XD.", "Lương tháng 13.", 10000000, 18000000, "TP. Hồ Chí Minh", "Toàn thời gian", "Thiết kế", "1 năm", "2026-08-15T23:59:59", "2026-07-05T09:30:00", 97, 2, currentUserId));
        jobs.add(createJob("JOB005", "COMP005", "Digital Marketing Executive", "Lập kế hoạch quảng cáo Facebook và Google.", "Facebook Ads, Google Ads.", "Hoa hồng theo KPI.", 9000000, 15000000, "Cần Thơ", "Toàn thời gian", "Marketing", "1 năm", "2026-08-18T23:59:59", "2026-07-04T11:00:00", 132, 5, currentUserId));
        jobs.add(createJob("JOB006", "COMP006", "Kế toán tổng hợp", "Theo dõi công nợ và báo cáo tài chính.", "Tốt nghiệp Kế toán.", "BHXH đầy đủ.", 9000000, 14000000, "Bình Dương", "Toàn thời gian", "Kế toán", "2 năm", "2026-08-22T23:59:59", "2026-07-03T08:00:00", 156, 1, currentUserId));
        jobs.add(createJob("JOB007", "COMP007", "HR Executive", "Tuyển dụng và đào tạo nhân sự.", "Kỹ năng giao tiếp.", "Thưởng quý.", 8000000, 13000000, "Hà Nội", "Toàn thời gian", "Nhân sự", "1 năm", "2026-08-21T23:59:59", "2026-07-03T09:00:00", 88, 2, currentUserId));
        jobs.add(createJob("JOB008", "COMP008", "Tester (QA/QC)", "Kiểm thử phần mềm Web và Mobile.", "Có kiến thức Testing.", "Đào tạo miễn phí.", 10000000, 17000000, "TP. Hồ Chí Minh", "Toàn thời gian", "IT", "1 năm", "2026-08-25T23:59:59", "2026-07-02T10:00:00", 176, 3, currentUserId));
        jobs.add(createJob("JOB009", "COMP009", "Data Analyst", "Phân tích dữ liệu doanh nghiệp.", "SQL, Excel, Power BI.", "Làm việc Hybrid.", 15000000, 25000000, "Đà Nẵng", "Toàn thời gian", "Phân tích dữ liệu", "2 năm", "2026-08-28T23:59:59", "2026-07-02T08:30:00", 190, 2, currentUserId));
        jobs.add(createJob("JOB010", "COMP010", "AI Engineer", "Xây dựng mô hình Machine Learning.", "Python, TensorFlow.", "Remote 3 ngày/tuần.", 25000000, 40000000, "Hà Nội", "Toàn thời gian", "AI", "3 năm", "2026-08-30T23:59:59", "2026-07-01T08:00:00", 340, 2, currentUserId));
        jobs.add(createJob("JOB011", "COMP011", "Content Marketing", "Viết nội dung cho website và mạng xã hội.", "Kỹ năng viết tốt.", "Làm việc linh hoạt.", 7000000, 12000000, "Huế", "Toàn thời gian", "Marketing", "Không yêu cầu", "2026-08-18T23:59:59", "2026-07-01T10:00:00", 90, 3, currentUserId));
        jobs.add(createJob("JOB012", "COMP012", "Sales Executive", "Tìm kiếm khách hàng doanh nghiệp.", "Kỹ năng giao tiếp.", "Hoa hồng hấp dẫn.", 8000000, 20000000, "TP. Hồ Chí Minh", "Toàn thời gian", "Kinh doanh", "Không yêu cầu", "2026-08-30T23:59:59", "2026-06-30T09:00:00", 240, 10, currentUserId));
        jobs.add(createJob("JOB013", "COMP013", "Business Analyst", "Phân tích yêu cầu khách hàng.", "SQL, UML.", "Hybrid.", 18000000, 30000000, "Hà Nội", "Toàn thời gian", "IT", "2 năm", "2026-08-29T23:59:59", "2026-06-29T08:00:00", 168, 2, currentUserId));
        jobs.add(createJob("JOB014", "COMP014", "Mobile Flutter Developer", "Phát triển ứng dụng Flutter.", "Flutter, Dart.", "Laptop.", 16000000, 28000000, "Đà Nẵng", "Toàn thời gian", "IT", "2 năm", "2026-08-26T23:59:59", "2026-06-29T09:00:00", 189, 2, currentUserId));
        jobs.add(createJob("JOB015", "COMP015", "Graphic Designer", "Thiết kế banner, poster.", "Photoshop, Illustrator.", "Thưởng KPI.", 9000000, 15000000, "Bình Dương", "Toàn thời gian", "Thiết kế", "1 năm", "2026-08-25T23:59:59", "2026-06-28T09:30:00", 111, 2, currentUserId));
        jobs.add(createJob("JOB016", "COMP016", "DevOps Engineer", "Quản trị CI/CD.", "Docker, Kubernetes.", "Remote.", 22000000, 38000000, "Hà Nội", "Làm việc từ xa", "IT", "3 năm", "2026-08-30T23:59:59", "2026-06-28T08:00:00", 276, 1, currentUserId));
        jobs.add(createJob("JOB017", "COMP017", "Customer Support", "Hỗ trợ khách hàng qua điện thoại.", "Giao tiếp tốt.", "Đào tạo miễn phí.", 7000000, 11000000, "Hải Phòng", "Toàn thời gian", "Chăm sóc khách hàng", "Không yêu cầu", "2026-08-20T23:59:59", "2026-06-27T08:00:00", 80, 5, currentUserId));
        jobs.add(createJob("JOB018", "COMP018", "Intern Java Developer", "Thực tập Java Backend.", "Biết Java cơ bản.", "Có hỗ trợ thực tập.", 3000000, 5000000, "TP. Hồ Chí Minh", "Thực tập", "IT", "Sinh viên", "2026-08-15T23:59:59", "2026-06-26T08:00:00", 421, 6, currentUserId));
        jobs.add(createJob("JOB019", "COMP019", "Network Engineer", "Quản trị hệ thống mạng doanh nghiệp.", "CCNA là lợi thế.", "BHXH đầy đủ.", 12000000, 20000000, "Đồng Nai", "Toàn thời gian", "Mạng máy tính", "2 năm", "2026-08-24T23:59:59", "2026-06-25T08:00:00", 105, 2, currentUserId));
        jobs.add(createJob("JOB020", "COMP020", "Cyber Security Engineer", "Giám sát an toàn thông tin.", "CEH là lợi thế.", "Thưởng bảo mật.", 22000000, 35000000, "Hà Nội", "Toàn thời gian", "An toàn thông tin", "3 năm", "2026-08-31T23:59:59", "2026-06-24T08:00:00", 355, 2, currentUserId));

        for (Job job : jobs) {
            batch.set(db.collection("jobs").document(job.getId()), job);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(context, "Đã thêm 20 công việc thành công!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("DataSeed", "Lỗi: " + e.getMessage());
        });
    }

    private static Job createJob(String id, String compId, String title, String desc, String req, String ben, 
                                 long sMin, long sMax, String loc, String type, String cat, 
                                 String exp, String deadline, String postedAtStr, int views, int qty, String ownerId) {
        Job job = new Job();
        job.setId(id);
        job.setCompanyId(ownerId); // Sử dụng ownerId cho cả companyId và employerId để đồng bộ
        job.setEmployerId(ownerId); // Thêm trường này để ManageJobsActivity có thể truy vấn
        job.setTitle(title);
        job.setDescription(desc);
        job.setRequirements(req);
        job.setBenefits(ben);
        job.setSalaryMin(sMin);
        job.setSalaryMax(sMax);
        job.setLocation(loc);
        job.setJobType(type);
        job.setCategory(cat);
        job.setExperienceRequired(exp);
        job.setQuantity(qty);
        job.setViews(views);
        job.setStatus("active");
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            job.setPostedAt(sdf.parse(postedAtStr).getTime());
            job.setDeadline(sdf.parse(deadline).getTime());
        } catch (Exception e) {
            job.setPostedAt(System.currentTimeMillis());
            job.setDeadline(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000); // Mặc định 30 ngày
        }
        
        return job;
    }
}
